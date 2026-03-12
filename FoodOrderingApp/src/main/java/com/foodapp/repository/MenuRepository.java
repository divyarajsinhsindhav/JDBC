package com.foodapp.repository;

import com.foodapp.model.FoodItem;
import com.foodapp.model.Menu;
import com.foodapp.model.MenuCategory;

public interface MenuRepository {
    Menu getMenu(boolean is_admin);
    FoodItem addFoodItem(FoodItem foodItem);

    FoodItem toggleFoodItemAvailability(int id);

    FoodItem updateFoodItem(FoodItem foodItem);
    void deleteFoodItem(int id);
    void addCategory(MenuCategory category);

    FoodItem findFoodItemById(int id, boolean is_admin);

    MenuCategory findCategoryById(int id);
}