package com.foodapp.repository;

import com.foodapp.model.Discount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountRepositoryImpl implements DiscountRepository {

    private final Connection connection;

    public DiscountRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    private Discount mapRow(ResultSet rs) throws SQLException {
        Discount d = new Discount();

        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setDiscountRate(rs.getDouble("discount_rate"));
        d.setDiscountOn(rs.getDouble("discount_on"));
        Timestamp startTs = rs.getTimestamp("start_date");

        if (startTs != null) {
            d.setStartDate(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("end_date");

        if (endTs != null) {
            d.setEndDate(endTs.toLocalDateTime());
        }

        d.setActive(rs.getBoolean("is_active"));
        d.setDeleted(rs.getBoolean("is_deleted"));
        Timestamp createdAt = rs.getTimestamp("created_at");

        if (createdAt != null) {
            d.setCreatedAt(createdAt.toLocalDateTime());
        }

        return d;
    }

    @Override
    public Discount addDiscount(Discount discount) {
        String sql = """
                INSERT INTO discount (name, discount_rate, discount_on, start_date, end_date, is_active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, discount.getName());
            ps.setDouble(2, discount.getDiscountRate());
            ps.setDouble(3, discount.getDiscountOn());
            ps.setTimestamp(4, Timestamp.valueOf(discount.getStartDate()));
            ps.setTimestamp(5, Timestamp.valueOf(discount.getEndDate()));
            ps.setBoolean(6, discount.isActive());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Insert failed, no rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    discount.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding discount: " + e.getMessage());
        }
        return discount;
    }

    @Override
    public Discount updateDiscount(Discount discount) {
        String sql = """
                UPDATE discount
                SET name = ?, discount_rate = ?, discount_on = ?, start_date = ?, end_date = ?
                WHERE id = ? AND is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, discount.getName());
            ps.setDouble(2, discount.getDiscountRate());
            ps.setDouble(3, discount.getDiscountOn());
            ps.setTimestamp(4, Timestamp.valueOf(discount.getStartDate()));
            ps.setTimestamp(5, Timestamp.valueOf(discount.getEndDate()));
            ps.setInt(6, discount.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Update failed: discount not found or deleted (id=" + discount.getId() + ")");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error updating discount: " + e.getMessage());
        }
        return discount;
    }

    @Override
    public Discount toggleActive(int id) {
        String sql = """
                UPDATE discount
                SET is_active = NOT is_active
                WHERE id = ? AND is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Toggle failed: discount not found or deleted (id=" + id + ")");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error toggling discount status: " + e.getMessage());
        }
        return findById(id);
    }

    @Override
    public void deleteDiscount(int id) {
        String sql = "UPDATE discount SET is_deleted = true WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Delete failed: discount not found (id=" + id + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting discount: " + e.getMessage());
        }
    }

    @Override
    public Discount findById(int id) {
        String sql = """
                SELECT id, name, discount_rate, discount_on, start_date, end_date, is_active, is_deleted, created_at
                FROM discount
                WHERE id = ? AND is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding discount: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Discount> getAllDiscounts() {
        List<Discount> list = new ArrayList<>();
        String sql = """
                SELECT id, name, discount_rate, discount_on, start_date, end_date, is_active, is_deleted, created_at
                FROM discount
                WHERE is_deleted = false
                ORDER BY id
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching discounts: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Discount> getActiveDiscounts() {
        List<Discount> list = new ArrayList<>();
        String sql = """
                SELECT id, name, discount_rate, discount_on, start_date, end_date, is_active, is_deleted, created_at
                FROM discount
                WHERE is_active = true
                  AND is_deleted = false
                  AND start_date <= CURRENT_TIMESTAMP
                  AND end_date   >= CURRENT_TIMESTAMP
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching active discounts: " + e.getMessage());
        }
        return list;
    }
}
