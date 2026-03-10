package com.foodapp.seeder;
import com.foodapp.service.MenuService;

public class MenuSeeder {

    private MenuSeeder() {
    }

    public static void seed(MenuService menuService) {
        // Root Categories
        menuService.addCategory(0, 1, "Pizza");
        menuService.addCategory(0, 2, "Snacks");
        menuService.addCategory(0, 3, "Beverages");

        // Pizza
        menuService.addFoodItem(1, 1, "Margherita Pizza", 180);
        menuService.addFoodItem(1, 2, "Farmhouse Pizza", 220);
        menuService.addFoodItem(1, 3, "Veggie Delight Pizza", 240);

        // Snacks
        menuService.addFoodItem(2, 4, "Garlic Bread", 120);
        menuService.addFoodItem(2, 5, "Cheese Garlic Bread", 150);
        menuService.addFoodItem(2, 6, "Veg Nuggets", 140);

        // Beverages
        menuService.addFoodItem(3, 7, "Coke", 40);
        menuService.addFoodItem(3, 8, "Cold Coffee", 90);
    }
}