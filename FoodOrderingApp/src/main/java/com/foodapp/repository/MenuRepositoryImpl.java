package com.foodapp.repository;

import com.foodapp.model.FoodItem;
import com.foodapp.model.Menu;
import com.foodapp.model.MenuCategory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MenuRepositoryImpl implements MenuRepository {

    private final Connection connection;

    public MenuRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Menu getMenu(boolean is_admin) {
        Map<Integer, MenuCategory> categoryMap = new HashMap<>();
        MenuCategory root = new MenuCategory(0, "MENU");

        // Admin sees inactive categories too
        String catSql = is_admin
                ? """
              SELECT id, name, parent_category_id
              FROM categories
              WHERE is_deleted = false
              ORDER BY id
              """
                : """
              SELECT id, name, parent_category_id
              FROM categories
              WHERE is_active = true AND is_deleted = false
              ORDER BY id
              """;

        try (PreparedStatement ps = connection.prepareStatement(catSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MenuCategory cat = new MenuCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                categoryMap.put(cat.getId(), cat);
            }

        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }

        // Admin sees inactive parent-child relationships too
        String parentSql = is_admin
                ? """
              SELECT id, parent_category_id
              FROM categories
              WHERE is_deleted = false AND parent_category_id IS NOT NULL
              """
                : """
              SELECT id, parent_category_id
              FROM categories
              WHERE is_active = true AND is_deleted = false AND parent_category_id IS NOT NULL
              """;

        try (PreparedStatement ps = connection.prepareStatement(parentSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int childId  = rs.getInt("id");
                int parentId = rs.getInt("parent_category_id");

                MenuCategory child  = categoryMap.get(childId);
                MenuCategory parent = categoryMap.get(parentId);

                if (child != null && parent != null) {
                    parent.add(child);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error wiring category tree: " + e.getMessage());
        }

        // Admin sees inactive top-level categories too
        String topLevelSql = is_admin
                ? """
              SELECT id FROM categories
              WHERE is_deleted = false AND parent_category_id IS NULL
              """
                : """
              SELECT id FROM categories
              WHERE is_active = true AND is_deleted = false AND parent_category_id IS NULL
              """;

        try (PreparedStatement ps = connection.prepareStatement(topLevelSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MenuCategory cat = categoryMap.get(rs.getInt("id"));
                if (cat != null) root.add(cat);
            }

        } catch (SQLException e) {
            System.err.println("Error attaching top-level categories: " + e.getMessage());
        }

        // Admin sees inactive food items too
        String itemSql = is_admin
                ? """
              SELECT id, name, price, category_id
              FROM food_items
              WHERE is_deleted = false
              """
                : """
              SELECT id, name, price, category_id
              FROM food_items
              WHERE is_active = true AND is_deleted = false
              """;

        try (PreparedStatement ps = connection.prepareStatement(itemSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FoodItem item = new FoodItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price")
                );

                int categoryId = rs.getInt("category_id");
                MenuCategory parent = categoryMap.get(categoryId);

                if (parent != null) {
                    parent.add(item);
                } else {
                    root.add(item);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading food items: " + e.getMessage());
        }

        return root;
    }

    @Override
    public FoodItem findFoodItemById(int id, boolean is_admin) {
        String sql = is_admin
                ? """
              SELECT id, name, price
              FROM food_items
              WHERE id = ? AND is_deleted = false
              """
                : """
              SELECT id, name, price
              FROM food_items
              WHERE id = ? AND is_deleted = false AND is_active = true
              """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FoodItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding food item by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public MenuCategory findCategoryById(int id) {
        String sql = """
                SELECT id, name
                FROM categories
                WHERE id = ? AND is_deleted = false AND is_active = true;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MenuCategory(
                            rs.getInt("id"),
                            rs.getString("name")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding category by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public FoodItem addFoodItem(FoodItem foodItem) {
        String sql = """
                INSERT INTO food_items (name, price, category_id)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, foodItem.getName());
            ps.setDouble(2, foodItem.getPrice());

            if (foodItem.getCategoryId() > 0) {
                ps.setInt(3, foodItem.getCategoryId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Insert failed, no rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new FoodItem(keys.getInt(1), foodItem.getName(), foodItem.getPrice());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding food item: " + e.getMessage());
        }
        return null;
    }

    @Override
    public FoodItem toggleFoodItemAvailability(int id) {
        String sql = """
        UPDATE food_items
        SET is_active = NOT is_active
        WHERE id = ? AND is_deleted = false
    """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Toggle failed: no food item found with id " + id);
                return null;
            }
            return findFoodItemById(id, true);
        } catch (SQLException e) {
            System.err.println("Error toggling food item availability: " + e.getMessage());
        }
        return null;
    }

    @Override
    public FoodItem updateFoodItem(FoodItem foodItem) {
        String sql = """
                UPDATE food_items
                SET name = ?, price = ?,
                WHERE id = ? AND is_deleted = false AND is_active = true;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, foodItem.getName());
            ps.setDouble(2, foodItem.getPrice());
            ps.setInt(3, foodItem.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Update failed: no food item found with id " + foodItem.getId());
                return null;
            }
            return foodItem;

        } catch (SQLException e) {
            System.err.println("Error updating food item: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteFoodItem(int id) {
        // Soft delete — preserves history
        String sql = "UPDATE food_items SET is_deleted = true WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Delete failed: no food item found with id " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting food item: " + e.getMessage());
        }
    }

    @Override
    public void addCategory(MenuCategory category) {
        String sql = """
                INSERT INTO categories (name, parent_category_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategory());

            if (category.getParentId() > 0) {
                ps.setInt(2, category.getParentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Insert failed, no rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
        }
    }

}