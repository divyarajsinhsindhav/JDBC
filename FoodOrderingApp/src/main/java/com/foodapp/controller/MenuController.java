package com.foodapp.controller;

import com.foodapp.service.MenuService;

public class MenuController {
    private MenuService menuService;
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    public void displayMenu() {

        menuService.displayMenu();

    }

    public void displayItems() {

        System.out.println("\n+--------------------------------------------------+");
        System.out.println("|                    FOOD ITEMS                    |");
        System.out.println("+------+------------------------------+------------+");
        System.out.printf("| %-4s | %-28s | %-10s |%n", "ID", "ITEM NAME", "PRICE");
        System.out.println("+------+------------------------------+------------+");

        menuService.getFoodItems().forEach(foodItem -> {
            System.out.printf("| %-4d | %-28s | %-10.2f |%n",
                    foodItem.getId(),
                    foodItem.getName(),
                    foodItem.getPrice());
        });

        System.out.println("+------+------------------------------+------------+");
    }

    public void displayCategory() {

        System.out.println("\n=========== MENU CATEGORIES ===========");

        System.out.printf("%-10s %-30s%n", "ID", "CATEGORY");
        System.out.println("---------------------------------------");

        menuService.getCategory()
                .forEach(category -> {
                    System.out.printf("%-10d %-30s%n",
                            category.getId(),
                            category.getCategory());
                });

        System.out.println("---------------------------------------");
    }
}
