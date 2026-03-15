package com.foodapp.repository;

import com.foodapp.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryImpl implements OrderRepository {

    private final Connection connection;
    private final PaymentRepository paymentRepository;

    public OrderRepositoryImpl(Connection connection, PaymentRepository paymentRepository) {
        this.connection = connection;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void addOrder(Order order) {

        // 1. Persist payment first so we have the generated payment_id
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setMode(order.getPaymentMode());
        paymentRecord.setCustomerId(order.getCustomer().getId());
        paymentRecord.setAmount(order.getFinalAmount());
        PaymentRecord savedPayment = paymentRepository.save(paymentRecord);

        String orderSql = """
                INSERT INTO orders (customer_id, delivery_partner_id, total_amount, discount_rate,
                                    final_amount, address, status, payment_id)
                VALUES (?, ?, ?, ?, ?, ?, ?::order_status, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomer().getId());

            if (order.getDeliveryPartner() != null) {
                ps.setInt(2, ((DeliveryPartner) order.getDeliveryPartner()).getId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDouble(3, order.getTotal());
            ps.setDouble(4, order.getDiscountRate());
            ps.setDouble(5, order.getFinalAmount());

            // Use customer's address if available
            String address = (order.getCustomer() instanceof Customer c) ? c.getAddress() : "";
            ps.setString(6, address);

            ps.setString(7, order.getOrderStatus().name());

            // 2. Link the saved payment row
            if (savedPayment != null && savedPayment.getId() > 0) {
                ps.setInt(8, savedPayment.getId());
                order.setPaymentId(savedPayment.getId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.executeUpdate();

            int generatedId;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Failed to retrieve generated order id.");
                generatedId = keys.getInt(1);
            }

            order.setId(generatedId);

            // Insert order items
            insertOrderItems(generatedId, order.getOrderItems());

            // Insert initial status history
            insertStatusHistory(generatedId, order.getOrderStatus());

        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
        }
    }

    @Override
    public void removeOrder(Order order) {
        String sql = "UPDATE orders SET is_deleted = true WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, order.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Remove failed: no order found with id " + order.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error removing order: " + e.getMessage());
        }
    }

    @Override
    public void updateOrderStatus(Order order) {
        String sql = """
                UPDATE orders SET status = ?::order_status, delivery_partner_id = ?
                WHERE id = ? AND is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, order.getOrderStatus().name());

            if (order.getDeliveryPartner() != null) {
                ps.setInt(2, order.getDeliveryPartner().getId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setInt(3, order.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Update failed: no order found with id " + order.getId());
                return;
            }

            // Record status change in history
            insertStatusHistory(order.getId(), order.getOrderStatus());

        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
    }

    @Override
    public List<Order> getAllOrders() {
        String sql = """
                SELECT o.id, o.total_amount, o.discount_rate, o.final_amount,
                       o.address, o.status, o.payment_id,
                       u.id AS customer_id, u.name AS customer_name,
                       u.email AS customer_email,
                       c.phone AS customer_phone, c.address AS customer_address,
                       dp.id AS dp_id, dpu.name AS dp_name, dpu.email AS dp_email,
                       dpu.password AS dp_password, dp.phone AS dp_phone, dp.status AS dp_status,
                       p.mode AS payment_mode
                FROM orders o
                JOIN users u ON o.customer_id = u.id
                JOIN customer c ON u.id = c.id
                LEFT JOIN delivery_partner dp ON o.delivery_partner_id = dp.id
                LEFT JOIN users dpu ON dp.id = dpu.id
                LEFT JOIN payment p ON o.payment_id = p.id
                WHERE o.is_deleted = false
                ORDER BY o.created_at DESC
                """;

        return queryOrders(sql, null, null);
    }

    @Override
    public List<Order> getAllOrdersByCustomerId(int id) {
        String sql = """
                SELECT o.id, o.total_amount, o.discount_rate, o.final_amount,
                       o.address, o.status, o.payment_id,
                       u.id AS customer_id, u.name AS customer_name,
                       u.email AS customer_email,
                       c.phone AS customer_phone, c.address AS customer_address,
                       dp.id AS dp_id, dpu.name AS dp_name, dpu.email AS dp_email,
                       dpu.password AS dp_password, dp.phone AS dp_phone, dp.status AS dp_status,
                       p.mode AS payment_mode
                FROM orders o
                JOIN users u ON o.customer_id = u.id
                JOIN customer c ON u.id = c.id
                LEFT JOIN delivery_partner dp ON o.delivery_partner_id = dp.id
                LEFT JOIN users dpu ON dp.id = dpu.id
                LEFT JOIN payment p ON o.payment_id = p.id
                WHERE o.customer_id = ? AND o.is_deleted = false
                ORDER BY o.created_at DESC
                """;

        return queryOrders(sql, id, null);
    }

    @Override
    public List<Order> getAllOrdersByDeliveryPartnerId(int id) {
        String sql = """
                SELECT o.id, o.total_amount, o.discount_rate, o.final_amount,
                       o.address, o.status, o.payment_id,
                       u.id AS customer_id, u.name AS customer_name,
                       u.email AS customer_email,
                       c.phone AS customer_phone, c.address AS customer_address,
                       dp.id AS dp_id, dpu.name AS dp_name, dpu.email AS dp_email,
                       dpu.password AS dp_password, dp.phone AS dp_phone, dp.status AS dp_status,
                       p.mode AS payment_mode
                FROM orders o
                JOIN users u ON o.customer_id = u.id
                JOIN customer c ON u.id = c.id
                LEFT JOIN delivery_partner dp ON o.delivery_partner_id = dp.id
                LEFT JOIN users dpu ON dp.id = dpu.id
                LEFT JOIN payment p ON o.payment_id = p.id
                WHERE o.delivery_partner_id = ? AND o.is_deleted = false
                ORDER BY o.created_at DESC
                """;

        return queryOrders(sql, id, null);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        String sql = """
                SELECT o.id, o.total_amount, o.discount_rate, o.final_amount,
                       o.address, o.status, o.payment_id,
                       u.id AS customer_id, u.name AS customer_name,
                       u.email AS customer_email,
                       c.phone AS customer_phone, c.address AS customer_address,
                       dp.id AS dp_id, dpu.name AS dp_name, dpu.email AS dp_email,
                       dpu.password AS dp_password, dp.phone AS dp_phone, dp.status AS dp_status,
                       p.mode AS payment_mode
                FROM orders o
                JOIN users u ON o.customer_id = u.id
                JOIN customer c ON u.id = c.id
                LEFT JOIN delivery_partner dp ON o.delivery_partner_id = dp.id
                LEFT JOIN users dpu ON dp.id = dpu.id
                LEFT JOIN payment p ON o.payment_id = p.id
                WHERE o.status = ?::order_status AND o.is_deleted = false
                ORDER BY o.created_at DESC
                """;

        return queryOrders(sql, null, status);
    }

    /**
     * Shared query executor — pass an int param, a status param, or neither (null).
     */
    private List<Order> queryOrders(String sql, Integer intParam, OrderStatus statusParam) {
        List<Order> orders = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (intParam != null)    ps.setInt(1, intParam);
            if (statusParam != null) ps.setString(1, statusParam.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapToOrder(rs);
                    order.getOrderItems().addAll(fetchOrderItems(order.getId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }

        return orders;
    }

    private Order mapToOrder(ResultSet rs) throws SQLException {
        // Map customer
        Customer customer = new Customer(
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                null,  // password not needed here
                rs.getString("customer_phone"),
                rs.getString("customer_address")
        );

        // Map delivery partner (nullable)
        DeliveryPartner deliveryPartner = null;
        int dpId = rs.getInt("dp_id");
        if (!rs.wasNull()) {
            deliveryPartner = new DeliveryPartner(
                    dpId,
                    rs.getString("dp_name"),
                    rs.getString("dp_email"),
                    rs.getString("dp_password"),
                    rs.getString("dp_phone")
            );
            deliveryPartner.setStatus(DeliveryPartnerStatus.valueOf(rs.getString("dp_status")));
        }

        // Map payment mode (nullable — payment row may not exist for very old orders)
        PaymentMode paymentMode = null;
        String modeStr = rs.getString("payment_mode");
        if (modeStr != null) {
            paymentMode = PaymentMode.valueOf(modeStr);
        }

        Order order = new Order(
                rs.getInt("id"),
                new ArrayList<>(),   // items fetched separately
                customer,
                rs.getDouble("total_amount"),
                rs.getDouble("discount_rate"),
                paymentMode,
                rs.getDouble("final_amount")
        );

        order.setOrderStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setDeliveryPartner(deliveryPartner);

        int paymentId = rs.getInt("payment_id");
        if (!rs.wasNull()) order.setPaymentId(paymentId);

        return order;
    }

    private List<OrderItem> fetchOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT oi.id, oi.food_item_id, oi.food_item_name, oi.price, oi.quantity
                FROM order_items oi
                WHERE oi.order_id = ? AND oi.is_deleted = false
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodItem foodItem = new FoodItem(
                            rs.getInt("food_item_id"),
                            rs.getString("food_item_name"),
                            rs.getDouble("price")
                    );
                    items.add(new OrderItem(
                            rs.getInt("id"),
                            foodItem,
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order items for order " + orderId + ": " + e.getMessage());
        }

        return items;
    }

    private void insertOrderItems(int orderId, List<OrderItem> items) throws SQLException {
        String sql = """
                INSERT INTO order_items (order_id, food_item_id, food_item_name, price, quantity, subtotal)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (OrderItem item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getFoodItem().getId());
                ps.setString(3, item.getFoodItem().getName());
                ps.setDouble(4, item.getPrice());
                ps.setInt(5, item.getQuantity());
                ps.setDouble(6, item.getPrice() * item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertStatusHistory(int orderId, OrderStatus status) throws SQLException {
        String sql = "INSERT INTO order_status_history (order_id, status) VALUES (?, ?::order_status)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, status.name());
            ps.executeUpdate();
        }
    }
}