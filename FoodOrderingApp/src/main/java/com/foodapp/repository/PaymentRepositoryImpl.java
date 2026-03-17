package com.foodapp.repository;

import com.foodapp.model.PaymentMode;
import com.foodapp.model.PaymentRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepositoryImpl implements PaymentRepository {

    private final Connection connection;

    public PaymentRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    private PaymentRecord mapRow(ResultSet rs) throws SQLException {
        PaymentRecord p = new PaymentRecord();
        p.setId(rs.getInt("id"));
        p.setMode(PaymentMode.valueOf(rs.getString("mode")));
        p.setCustomerId(rs.getInt("customer_id"));
        p.setAmount(rs.getDouble("amount"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toLocalDateTime());
        }
        return p;
    }

    @Override
    public PaymentRecord save(PaymentRecord payment) {
        String sql = """
                INSERT INTO payment (mode, customer_id, amount)
                VALUES (?::payment_mode, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, payment.getMode().name());
            ps.setInt(2, payment.getCustomerId());
            ps.setDouble(3, payment.getAmount());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Payment insert failed — no rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving payment: " + e.getMessage());
        }

        return payment;
    }

    @Override
    public PaymentRecord findById(int id) {
        String sql = """
                SELECT id, mode, customer_id, amount, created_at
                FROM payment
                WHERE id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding payment by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<PaymentRecord> findByCustomerId(int customerId) {
        List<PaymentRecord> list = new ArrayList<>();
        String sql = """
                SELECT id, mode, customer_id, amount, created_at
                FROM payment
                WHERE customer_id = ?
                ORDER BY created_at DESC
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding payments by customer: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<PaymentRecord> findAll() {
        List<PaymentRecord> list = new ArrayList<>();
        String sql = """
                SELECT id, mode, customer_id, amount, created_at
                FROM payment
                ORDER BY created_at DESC
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching all payments: " + e.getMessage());
        }
        return list;
    }
}
