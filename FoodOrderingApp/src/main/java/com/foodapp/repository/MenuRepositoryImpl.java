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
        Map<Integer, Integer> parentIdMap = new HashMap<>();

        MenuCategory root = new MenuCategory(0, "MENU");

        categoryMap.put(0, root);

        String catSql = is_admin
                ? """
              SELECT id, name, parent_category_id, is_active
              FROM categories
              WHERE is_deleted = false
              ORDER BY id
              """
                : """
              SELECT id, name, parent_category_id, is_active
              FROM categories
              WHERE is_active = true AND is_deleted = false
              ORDER BY id
              """;

        try (PreparedStatement ps = connection.prepareStatement(catSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int catId = rs.getInt("id");
                MenuCategory cat = new MenuCategory(catId, rs.getString("name"));
                cat.setActive(rs.getBoolean("is_active"));
                categoryMap.put(catId, cat);

                int parentId = rs.getInt("parent_category_id");
                parentIdMap.put(catId, rs.wasNull() ? -1 : parentId);
            }

        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }

        root = categoryMap.getOrDefault(0, root);

        for (Map.Entry<Integer, Integer> entry : parentIdMap.entrySet()) {
            int childId  = entry.getKey();
            int parentId = entry.getValue();

            if (childId == 0) continue; // root itself — skip

            MenuCategory child  = categoryMap.get(childId);

            MenuCategory parent = (parentId == -1)
                    ? root
                    : categoryMap.getOrDefault(parentId, root);

            if (child != null) {
                parent.add(child);
            }
        }

        String itemSql = is_admin
                ? """
              SELECT id, name, price, category_id, is_active
              FROM food_items
              WHERE is_deleted = false
              """
                : """
              SELECT id, name, price, category_id, is_active
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
                item.setActive(rs.getBoolean("is_active"));

                int categoryId = rs.getInt("category_id");
                item.setCategoryId(categoryId);
                
                MenuCategory parent = categoryMap.get(categoryId);

                if (parent != null) {
                    parent.add(item);
                } else if (categoryId == 0 || root.getId() == categoryId) {
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

            ps.setInt(3, foodItem.getCategoryId());

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
                SET name = ?, price = ?
                WHERE id = ? AND is_deleted = false;
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

            if (category.getParentId() == 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, category.getParentId());
            }

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Insert failed, no rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("violates foreign key constraint")) {
                throw new RuntimeException("Failed to add category: Parent category with ID " + category.getParentId() + " does not exist.");
            }
            throw new RuntimeException("Error adding category: " + e.getMessage());
        }
    }

}