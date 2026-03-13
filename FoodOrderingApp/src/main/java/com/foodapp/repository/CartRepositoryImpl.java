package com.foodapp.repository;

import com.foodapp.model.FoodItem;
import com.foodapp.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartRepositoryImpl implements CartRepository {

    private final Connection connection;

    public CartRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    /**
     * Returns the cart id for a customer, creating one if it doesn't exist.
     */
    private int getOrCreateCartId(Integer customerId) throws SQLException {
        String selectSql = "SELECT id FROM cart WHERE customer_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }

        // Cart doesn't exist yet — create one
        String insertSql = "INSERT INTO cart (customer_id) VALUES (?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }

        throw new SQLException("Failed to get or create cart for customer " + customerId);
    }

    private List<OrderItem> fetchCartItems(int cartId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT ci.id, ci.quantity, ci.food_item_id,
                       f.name AS food_name, f.price AS food_price
                FROM cart_items ci
                JOIN food_items f ON ci.food_item_id = f.id
                WHERE ci.cart_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodItem foodItem = new FoodItem(
                            rs.getInt("food_item_id"),
                            rs.getString("food_name"),
                            rs.getDouble("food_price")
                    );
                    items.add(new OrderItem(
                            rs.getInt("id"),
                            foodItem,
                            rs.getInt("quantity"),
                            foodItem.getPrice() * rs.getInt("quantity")
                    ));
                }
            }
        }

        return items;
    }

    @Override
    public List<OrderItem> getCart(Integer customerId) {
        try {
            int cartId = getOrCreateCartId(customerId);
            return fetchCartItems(cartId);
        } catch (SQLException e) {
            System.err.println("Error fetching cart for customer " + customerId + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public Map<Integer, List<OrderItem>> getCart() {
        Map<Integer, List<OrderItem>> allCarts = new HashMap<>();

        String sql = "SELECT id, customer_id FROM cart";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                int cartId = rs.getInt("id");
                allCarts.put(customerId, fetchCartItems(cartId));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all carts: " + e.getMessage());
        }

        return allCarts;
    }

    @Override
    public OrderItem getCartItem(Integer customerId, Integer orderItemId) {
        try {
            int cartId = getOrCreateCartId(customerId);

            String sql = """
                    SELECT ci.id, ci.quantity, ci.food_item_id,
                           f.name AS food_name, f.price AS food_price
                    FROM cart_items ci
                    JOIN food_items f ON ci.food_item_id = f.id
                    WHERE ci.cart_id = ? AND ci.id = ?
                    """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, cartId);
                ps.setInt(2, orderItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        FoodItem foodItem = new FoodItem(
                                rs.getInt("food_item_id"),
                                rs.getString("food_name"),
                                rs.getDouble("food_price")
                        );
                        return new OrderItem(
                                rs.getInt("id"),
                                foodItem,
                                rs.getInt("quantity"),
                                foodItem.getPrice() * rs.getInt("quantity")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cart item: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addToCart(Integer customerId, OrderItem orderItem) {
        try {
            int cartId = getOrCreateCartId(customerId);

            // If food item already exists in cart, increment quantity
            String checkSql = "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND food_item_id = ?";

            try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
                ps.setInt(1, cartId);
                ps.setInt(2, orderItem.getFoodItem().getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int existingId = rs.getInt("id");
                        int newQuantity = rs.getInt("quantity") + orderItem.getQuantity();
                        updateCartItemQuantity(customerId, existingId, newQuantity);
                        return;
                    }
                }
            }

            // New item — insert
            String insertSql = """
                    INSERT INTO cart_items (cart_id, food_item_id, quantity)
                    VALUES (?, ?, ?)
                    """;

            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setInt(1, cartId);
                ps.setInt(2, orderItem.getFoodItem().getId());
                ps.setInt(3, orderItem.getQuantity());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error adding to cart: " + e.getMessage());
        }
    }

    @Override
    public void removeFromCart(Integer customerId, OrderItem orderItem) {
        removeCartItem(customerId, orderItem.getId());
    }

    @Override
    public void removeCartItem(Integer customerId, Integer orderItemId) {
        try {
            int cartId = getOrCreateCartId(customerId);

            String sql = "DELETE FROM cart_items WHERE id = ? AND cart_id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, orderItemId);
                ps.setInt(2, cartId);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    System.err.println("Remove failed: cart item " + orderItemId + " not found.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error removing cart item: " + e.getMessage());
        }
    }

    @Override
    public void updateCartItemQuantity(Integer customerId, Integer orderItemId, Integer quantity) {
        try {
            int cartId = getOrCreateCartId(customerId);

            String sql = "UPDATE cart_items SET quantity = ? WHERE id = ? AND cart_id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, orderItemId);
                ps.setInt(3, cartId);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    System.err.println("Update failed: cart item " + orderItemId + " not found.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error updating cart item quantity: " + e.getMessage());
        }
    }

    @Override
    public void clearCart(Integer customerId) {
        try {
            int cartId = getOrCreateCartId(customerId);

            String sql = "DELETE FROM cart_items WHERE cart_id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, cartId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
        }
    }
}