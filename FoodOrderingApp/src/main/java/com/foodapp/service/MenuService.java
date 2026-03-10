package com.foodapp.service;

import com.foodapp.exception.ItemNotFoundException;
import com.foodapp.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuService {

    private final Menu root;
    private CartService cartService;

    public MenuService(CartService cartService) {
        this.root = new MenuCategory(0, "FOOD MENU");
        this.cartService = cartService;
    }

    public Menu getRoot() {
        return root;
    }

    public void addCategory(int parentId, int id, String categoryName) {

        Menu parent = findCategoryById(root, parentId);

        if (parent == null) {
            throw new RuntimeException("Parent category not found");
        }

        parent.add(new MenuCategory(id, categoryName));
    }

    public void addFoodItem(int categoryId, int id, String name, double price) {

        MenuCategory category = findCategoryById(root, categoryId);

        if (category == null) {
            throw new RuntimeException("Category not found");
        }

        category.add(new FoodItem(id, name, price));
    }

    public void displayMenu() {
        root.render(0);
    }

    private MenuCategory findCategoryById(Menu menu, int id) {

        if (!(menu instanceof MenuCategory category)) {
            return null;
        }

        if (category.getId() == id) {
            return category;
        }

        return category.getMenu()
                .stream()
                .map(child -> findCategoryById(child, id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public MenuCategory findCategoryByName(int parentCategoryId, String name) {
        MenuCategory parentCategory = findCategoryById(root, parentCategoryId);
        if (parentCategory == null || name == null) {
            return null;
        }

        return parentCategory.getMenu().stream()
                .filter(item -> item instanceof MenuCategory)
                .map(item -> (MenuCategory) item)
                .filter(c -> c.getCategory().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    public FoodItem findFoodItemByName(String name) {
        if (name == null) {
            return null;
        }
        return getFoodItems().stream()
                .filter(n -> n.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    public List<FoodItem> findFoodItemsByCategory(int categoryId, String name, Menu menu) {
        if (name == null) {
            return null;
        }
        return findCategoryById(menu, categoryId)
                .getMenu()
                .stream()
                .filter(item -> item instanceof FoodItem)
                .map(item -> (FoodItem) item)
                .filter(foodItem -> foodItem.getName().equalsIgnoreCase(name))
                .toList();
    }

    public FoodItem findFoodItem(int id) {
        return findFoodItemById(root, id);
    }

    private FoodItem findFoodItemById(Menu menu, int id) {

        if (menu instanceof FoodItem item) {
            if (item.getId() == id) {
                return item;
            }
        }

        if (menu instanceof MenuCategory category) {
            for (Menu child : category.getMenu()) {
                FoodItem result = findFoodItemById(child, id);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public Menu getMenu()  {
        return root;
    }

    public List<FoodItem> getFoodItems() {
        List<FoodItem> items = new ArrayList<>();
        collectFoodItems(root, items);
        return items;
    }

    private void collectFoodItems(Menu menu, List<FoodItem> items) {

        if (menu instanceof FoodItem foodItem) {
            items.add(foodItem);
            return;
        }

        if (menu instanceof MenuCategory category) {
            for (Menu child : category.getMenu()) {
                collectFoodItems(child, items);
            }
        }
    }

    public List<MenuCategory> getCategory() {
        List<MenuCategory> categories = new ArrayList<>();
        collectCategories(root, categories);
        return categories;
    }

    private void collectCategories(Menu menu, List<MenuCategory> categories) {

        if (menu instanceof MenuCategory category) {
            categories.add(category);

            for (Menu child : category.getMenu()) {
                collectCategories(child, categories);
            }
        }
    }

    public MenuCategory findParentCategoryOfFoodItem(int itemId) {
        return findParentCategoryOfFoodItem(root, itemId);
    }

    public void deleteItem(int itemId) {
        FoodItem item = findFoodItem(itemId);

        if (item == null) {
            throw new ItemNotFoundException("Item not found");
        }

        MenuCategory parent = findParentCategoryOfFoodItem(itemId);

        if (parent != null) {
            parent.getMenu().remove(item);

            cartService.getCart()
                    .values()
                    .forEach(orderItems ->
                            orderItems.removeIf(orderItem ->
                                    orderItem.getFoodItem().getId() == itemId
                            )
                    );
            System.out.println("Food item deleted successfully!");
        }
    }

    private MenuCategory findParentCategoryOfFoodItem(Menu menu, int itemId) {

        if (menu instanceof MenuCategory category) {

            for (Menu child : category.getMenu()) {

                if (child instanceof FoodItem foodItem && foodItem.getId() == itemId) {
                    return category; // parent found
                }

                if (child instanceof MenuCategory) {
                    MenuCategory result = findParentCategoryOfFoodItem(child, itemId);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    private void updateCategory(int categoryId, String newName) {
        getCategory().stream()
                .filter(category -> category.getId() == categoryId)
                .findFirst()
                .ifPresentOrElse(
                        category -> {
                            category.setCategory(newName);
                            System.out.println("Category updated successfully!");
                        },
                        () -> System.out.println("Category not found!")
                );
    }

}