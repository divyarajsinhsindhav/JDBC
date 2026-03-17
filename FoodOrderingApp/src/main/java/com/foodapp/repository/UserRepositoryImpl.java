package com.foodapp.repository;

import com.foodapp.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    private final Connection connection;

    public UserRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addUser(User user) {
        String userSql = """
                INSERT INTO users (name, email, password, role)
                VALUES (?, ?, ?, ?::user_role)
                """;

        try (PreparedStatement ps = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());

            ps.executeUpdate();

            int generatedId;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to retrieve generated user id.");
                }
                generatedId = keys.getInt(1);
            }

            // Insert into the role-specific table
            if (user instanceof Customer c) {
                String sql = "INSERT INTO customer (id, phone, address) VALUES (?, ?, ?)";
                try (PreparedStatement cps = connection.prepareStatement(sql)) {
                    cps.setInt(1, generatedId);
                    cps.setString(2, c.getPhone());
                    cps.setString(3, c.getAddress());
                    cps.executeUpdate();
                }

            } else if (user instanceof DeliveryPartner dp) {
                String sql = "INSERT INTO delivery_partner (id, phone) VALUES (?, ?)";
                try (PreparedStatement dps = connection.prepareStatement(sql)) {
                    dps.setInt(1, generatedId);
                    dps.setString(2, dp.getPhone());
                    dps.executeUpdate();
                }

            } else if (user instanceof Admin) {
                String sql = "INSERT INTO admin (id) VALUES (?)";
                try (PreparedStatement aps = connection.prepareStatement(sql)) {
                    aps.setInt(1, generatedId);
                    aps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    @Override
    public void removeUser(User user) {
        // Soft delete — cascades to role-specific table via ON DELETE CASCADE
        String sql = "UPDATE users SET is_deleted = true WHERE id = ? AND is_deleted = false";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Remove failed: no user found with id " + user.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error removing user: " + e.getMessage());
        }
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.role,
                       c.phone AS c_phone, c.address,
                       dp.phone AS dp_phone, dp.status
                FROM users u
                LEFT JOIN customer c ON u.id = c.id
                LEFT JOIN delivery_partner dp ON u.id = dp.id
                WHERE u.is_deleted = false AND u.is_active = true
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }

        return users;
    }

    @Override
    public List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();

        String sql = """
                SELECT u.id, u.name, u.email, u.password,
                       c.phone AS c_phone, c.address
                FROM users u
                JOIN customer c ON u.id = c.id
                WHERE u.is_deleted = false AND u.is_active = true
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("c_phone"),
                        rs.getString("address")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
        }

        return customers;
    }

    @Override
    public List<Admin> getAdmins() {
        List<Admin> admins = new ArrayList<>();

        String sql = """
                SELECT u.id, u.name, u.email, u.password
                FROM users u
                JOIN admin a ON u.id = a.id
                WHERE u.is_deleted = false AND u.is_active = true
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                admins.add(new Admin(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching admins: " + e.getMessage());
        }

        return admins;
    }

    @Override
    public List<DeliveryPartner> getDeliveryPartners() {
        List<DeliveryPartner> partners = new ArrayList<>();

        String sql = """
                SELECT u.id, u.name, u.email, u.password,
                       dp.phone AS dp_phone, dp.status
                FROM users u
                JOIN delivery_partner dp ON u.id = dp.id
                WHERE u.is_deleted = false AND u.is_active = true
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DeliveryPartner dp = new DeliveryPartner(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("dp_phone")
                );
                dp.setStatus(DeliveryPartnerStatus.valueOf(rs.getString("status")));
                partners.add(dp);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching delivery partners: " + e.getMessage());
        }

        return partners;
    }

    @Override
    public User getUserById(int id) {
        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.role,
                       c.phone AS c_phone, c.address,
                       dp.phone AS dp_phone, dp.status
                FROM users u
                LEFT JOIN customer c ON u.id = c.id
                LEFT JOIN delivery_partner dp ON u.id = dp.id
                WHERE u.id = ? AND u.is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by id: " + e.getMessage());
        }

        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.role,
                       c.phone AS c_phone, c.address,
                       dp.phone AS dp_phone, dp.status
                FROM users u
                LEFT JOIN customer c ON u.id = c.id
                LEFT JOIN delivery_partner dp ON u.id = dp.id
                WHERE u.email = ? AND u.is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Customer getCustomerById(int id) {
        String sql = """
                SELECT u.id, u.name, u.email, u.password,
                       c.phone AS c_phone, c.address
                FROM users u
                JOIN customer c ON u.id = c.id
                WHERE u.id = ? AND u.is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("c_phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer by id: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Admin getAdminById(int id) {
        String sql = """
                SELECT u.id, u.name, u.email, u.password
                FROM users u
                JOIN admin a ON u.id = a.id
                WHERE u.id = ? AND u.is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching admin by id: " + e.getMessage());
        }

        return null;
    }

    @Override
    public DeliveryPartner getDeliveryPartnerById(int id) {
        String sql = """
                SELECT u.id, u.name, u.email, u.password,
                       dp.phone AS dp_phone, dp.status
                FROM users u
                JOIN delivery_partner dp ON u.id = dp.id
                WHERE u.id = ? AND u.is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DeliveryPartner dp = new DeliveryPartner(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("dp_phone")
                    );
                    dp.setStatus(DeliveryPartnerStatus.valueOf(rs.getString("status")));
                    return dp;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching delivery partner by id: " + e.getMessage());
        }

        return null;
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        UserType role = UserType.valueOf(rs.getString("role"));

        return switch (role) {
            case CUSTOMER -> new Customer(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("c_phone"),
                    rs.getString("address")
            );
            case ADMIN -> new Admin(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password")
            );
            case DELIVERY_PARTNER -> {
                DeliveryPartner dp = new DeliveryPartner(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("dp_phone")
                );
                dp.setStatus(DeliveryPartnerStatus.valueOf(rs.getString("status")));
                yield dp;
            }
        };
    }

    @Override
    public void updateDeliveryPartnerStatus(int id, DeliveryPartnerStatus status) {
        String sql = "UPDATE delivery_partner SET status = ?::delivery_partner_status WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("updateDeliveryPartnerStatus: no partner found with id " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error updating delivery partner status: " + e.getMessage());
        }
    }
}