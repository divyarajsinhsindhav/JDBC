package com.foodapp.service;

import com.foodapp.exception.ItemNotFoundException;
import com.foodapp.model.*;
import com.foodapp.repository.MenuRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuService {
    
    private MenuRepository menuRepository;
    private CartService cartService;

    public MenuService(MenuRepository menuRepository, CartService cartService) {
        this.menuRepository = menuRepository;
        this.cartService = cartService;
    }

    public void addCategory(int parentId, String categoryName) {
        MenuCategory menuCategory = new MenuCategory(categoryName);
        menuCategory.setParentId(parentId);
        menuRepository.addCategory(menuCategory);
    }

    public void addFoodItem(int categoryId, String name, double price) {
        FoodItem item = new FoodItem(name, price);
        item.setCategoryId(categoryId);
        menuRepository.addFoodItem(item);
    }

    public void displayMenu() {
        Menu root = menuRepository.getMenu(true);
        if(root != null) {
            root.render(0);
        }
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
        Menu root = menuRepository.getMenu(true);
        MenuCategory parentCategory = null;
        if(parentCategoryId == 0) {
            parentCategory = (MenuCategory) root;
        } else {
            parentCategory = findCategoryById(root, parentCategoryId);
        }

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
        return getFoodItems(true).stream()
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
        return menuRepository.findFoodItemById(id, true);
    }

    public List<FoodItem> getFoodItems() {
        return getFoodItems(true);
    }

    public List<FoodItem> getFoodItems(boolean isAdmin) {
        List<FoodItem> items = new ArrayList<>();
        Menu root = menuRepository.getMenu(isAdmin);
        if(root != null) {
             collectFoodItems(root, items);
        }
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
        return getCategory(true);
    }

    public List<MenuCategory> getCategory(boolean isAdmin) {
        List<MenuCategory> categories = new ArrayList<>();
        Menu root = menuRepository.getMenu(isAdmin);
        if(root != null) {
             collectCategories(root, categories);
        }
        return categories;
    }

    private void collectCategories(Menu menu, List<MenuCategory> categories) {
        if (menu instanceof MenuCategory category) {
            if(category.getId() != 0) {
                 categories.add(category);
            }
            for (Menu child : category.getMenu()) {
                collectCategories(child, categories);
            }
        }
    }

    public void deleteItem(int itemId) {
        FoodItem item = findFoodItem(itemId);

        if (item == null) {
            throw new ItemNotFoundException("Item not found");
        }

        menuRepository.deleteFoodItem(itemId);

        cartService.removeFoodItemFromAllCarts(itemId);
        System.out.println("Food item deleted successfully!");
    }
    
    public void updateCategory(int categoryId, String newName) {
        MenuCategory cat = menuRepository.findCategoryById(categoryId);
        if (cat != null) {
            cat.setCategory(newName);
        }
    }
    
    public void updateFoodItem(FoodItem item) {
        menuRepository.updateFoodItem(item);
    }
}