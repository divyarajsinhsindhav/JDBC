package com.foodapp.controller;

import com.foodapp.model.*;
import com.foodapp.service.DeliveryPartnerService;
import com.foodapp.service.MenuService;
import com.foodapp.service.OrderService;
import com.foodapp.utils.IdGenerator;
import com.foodapp.utils.InputValidation;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminController {
    private final Scanner scanner;
    private final MenuService menuService;
    private final DeliveryPartnerService deliveryPartnerService;
    private final OrderService orderService;
    private MenuController menuController;

    public AdminController(MenuService menuService,
            DeliveryPartnerService deliveryPartnerService,
            OrderService orderService) {
        this.scanner = new Scanner(System.in);
        this.menuService = menuService;
        this.deliveryPartnerService = deliveryPartnerService;
        this.orderService = orderService;
        this.menuController = new MenuController(menuService);
    }

    private static final int MANAGE_MENU = 1;
    private static final int MANAGE_DISCOUNT = 2;
    private static final int MANAGE_DELIVERY_PARTNER = 3;
    private static final int MANAGE_ORDER = 4;
    private static final int BACK = 5;

    public void displayOptions() {
        while (true) {

            System.out.println("\n===== Admin Menu =====");
            System.out.println("1. Manage Menu");
            System.out.println("2. Manage Discount");
            System.out.println("3. Manage Delivery Partners");
            System.out.println("4. Manage Order");
            System.out.println("5. Logout");

            int choice = InputValidation.readIntInRange(scanner,
                    "Enter your choice: ", MANAGE_MENU, BACK);

            switch (choice) {
                case MANAGE_MENU -> manageMenu();
                case MANAGE_DISCOUNT -> manageDiscount();
                case MANAGE_DELIVERY_PARTNER -> manageDeliveryPartners();
                case MANAGE_ORDER -> manageOrder();
                case BACK -> {
                    System.out.println("Returning to previous menu...");
                    return;
                }
            }
        }
    }

    private static final int SHOW_MENU = 1;
    private static final int ADD_ITEM = 2;
    private static final int ADD_CATEGORY = 3;
    private static final int EDIT_ITEM = 4;
    private static final int DELETE_ITEM = 5;
    // private static final int EDIT_CATEGORY = 6;
    // private static final int DELETE_CATEGORY = 7;
    private static final int MENU_BACK = 6;

    private void manageMenu() {

        while (true) {
            System.out.println("\n--- Manage Menu ---");
            System.out.println("1. Show current menu");
            System.out.println("2. Add Item");
            System.out.println("3. Add Category");
            System.out.println("4. Update item");
            System.out.println("5. Delete Item");
            // System.out.println("6. Update Category");
            // System.out.println("7. Delete Category");
            System.out.println("6. Back");

            int choice = InputValidation.readIntInRange(scanner,
                    "Enter your choice: ", SHOW_MENU, MENU_BACK);

            switch (choice) {
                case SHOW_MENU -> menuController.displayMenu();
                case ADD_ITEM -> addItemInMenu();
                case EDIT_ITEM -> updateItem();
                case DELETE_ITEM -> deleteItem();
                // case EDIT_CATEGORY -> updateCategory();
                // case DELETE_CATEGORY -> deleteCategory();
                case ADD_CATEGORY -> addMenuCategory();
                case MENU_BACK -> {
                    return;
                }
            }
        }
    }

    private void manageDiscount() {

        FlatDiscount discount = FlatDiscount.getInstance();

        while (true) {

            double rate = discount.getDiscount();
            double minAmount = discount.getFlatDiscountOn();

            System.out.println("\n====== Discount Policy ======");

            if (rate == 0 && minAmount == 0) {
                System.out.println("No discount policy configured.");
            } else {
                System.out.println("Current Policy:");
                System.out.println("Discount Rate        : " + rate + "%");
                System.out.println("Minimum Order Amount : " + minAmount);
            }

            System.out.println("\n1. Set / Update Discount Rate");
            System.out.println("2. Set / Update Minimum Order Amount");
            System.out.println("3. Back");

            int choice = InputValidation.readIntInRange(
                    scanner, "Enter your choice: ", 1, 3);

            switch (choice) {
                case 1 -> setDiscountRate();
                case 2 -> setMinimumOrderAmountForDiscount();
                case 3 -> {
                    return;
                }
            }
        }
    }

    private static final int SET_DELIVERY_PARTNER_STATUS = 1;
    private static final int DELIVERY_BACK = 2;

    private void manageDeliveryPartners() {

        while (true) {
            System.out.println("\n--- Manage Delivery Partners ---");
            System.out.println("1. Change Delivery Partner Status");
            System.out.println("2. Back");

            int choice = InputValidation.readIntInRange(scanner,
                    "Enter your choice: ", SET_DELIVERY_PARTNER_STATUS, DELIVERY_BACK);

            switch (choice) {
                case SET_DELIVERY_PARTNER_STATUS -> setStatusOfDeliveryPartner();
                case DELIVERY_BACK -> {
                    return;
                }
            }
        }
    }

    private static final int ORDER_HISTORY = 1;
    private static final int ORDER_HISTORY_SUMMARY = 2;
    private static final int ORDER_HISTORY_BACK = 3;

    private void manageOrder() {
        while (true) {
            System.out.println("\n--- Manage Order ---");
            System.out.println("1. Order History");
            System.out.println("2. Summary");
            System.out.println("3. Back");

            int choice = InputValidation.readIntInRange(scanner,
                    "Enter your choice: ", ORDER_HISTORY, ORDER_HISTORY_BACK);
            switch (choice) {
                case ORDER_HISTORY -> orderHistory();
                case ORDER_HISTORY_SUMMARY -> summaryOfOrderHistory();
                case ORDER_HISTORY_BACK -> {
                    return;
                }
            }
        }
    }

    private void addMenuCategory() {

        System.out.println("\n========== Add Menu Category ==========");

        do {
            System.out.println("\nAvailable Categories:");
            menuController.displayCategory();

            int parentCategoryId = InputValidation.readPositiveZeroInt(
                    scanner, "Enter Parent Category ID (0 for root): ");

            int categoryId = IdGenerator.getNextCategoryID();

            String categoryName;

            while (true) {
                categoryName = InputValidation.readValidName(
                        scanner, "Enter new category name: ");
                MenuCategory menuCategory = menuService.findCategoryByName(parentCategoryId, categoryName);
                if (menuCategory == null) {
                    break;
                }
                System.out.println("Category with " + categoryName + " already exists for parent category!");
            }
            ;

            try {
                menuService.addCategory(parentCategoryId, categoryName);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            System.out.println("Category '" + categoryName + "' added successfully!");

        } while (InputValidation.doUserWantToContinue(scanner,
                "Do you want to add another category?"));

    }

    private void addItemInMenu() {

        System.out.println("\n========== Add Food Item ==========");

        do {
            System.out.println("\nAvailable Categories:");
            menuController.displayCategory();

            int categoryId = InputValidation.readPositiveZeroInt(
                    scanner, "Enter category ID to add item: ");

            int foodItemId = IdGenerator.getNextItemID();

            String itemName;

            while (true) {

                itemName = InputValidation.readValidName(
                        scanner, "Enter food item name: ");

                FoodItem existingItem = menuService.findFoodItemByName(itemName);

                if (existingItem == null) {
                    break;
                }

                System.out.println("Food item '" + itemName + "' already exists!");
            }

            double itemPrice = InputValidation.readPositiveDouble(
                    scanner, "Enter food item price: ");

            try {
                menuService.addFoodItem(categoryId, foodItemId, itemName, itemPrice);
                System.out.println("Food item '" + itemName + "' added successfully!");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (InputValidation.doUserWantToContinue(scanner,
                "Do you want to add another item?"));

    }

    private void updateItem() {

        System.out.println("\n========== Update Food Item ==========");

        do {
            menuService.displayMenu();

            int itemId = InputValidation.readPositiveZeroInt(
                    scanner, "Enter Food Item ID to update: ");

            FoodItem item = menuService.findFoodItem(itemId);

            if (item == null) {
                System.out.println("Food item not found!");
                return;
            }

            String newName;
            while (true) {
                newName = InputValidation.readValidName(
                        scanner, "Enter new item name: ");

                FoodItem existing = menuService.findFoodItemByName(newName);

                if (existing == null || existing.getId() == itemId) {
                    break;
                }

                System.out.println("Food item with this name already exists!");
            }

            double newPrice = InputValidation.readPositiveDouble(
                    scanner, "Enter new price: ");

            item.setName(newName);
            item.setPrice(newPrice);

            System.out.println("Food item updated successfully!");

        } while (InputValidation.doUserWantToContinue(
                scanner, "Do you want to update another item?"));
    }

    private void deleteItem() {

        System.out.println("\n========== Delete Food Item ==========");

        do {

            menuService.displayMenu();

            int itemId = InputValidation.readPositiveZeroInt(
                    scanner, "Enter Food Item ID to delete: ");

            try {
                menuService.deleteItem(itemId);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (InputValidation.doUserWantToContinue(
                scanner, "Do you want to delete another item?"));
    }

    private void setDiscountRate() {
        FlatDiscount discount = FlatDiscount.getInstance();
        double currentRate = discount.getDiscount();

        System.out.println("\n--- Discount Rate Settings ---");

        if (currentRate == 0) {
            System.out.println("No discount rate configured.");
        } else {
            System.out.println("Current Discount Rate: " + currentRate + "%");
        }
        int newRate = InputValidation.readIntInRange(
                scanner, "Enter new discount rate (%): ", 1, 100);

        discount.setDiscount(newRate);

        System.out.println("Discount rate is now set to " + newRate + "%.");
    }

    private void setMinimumOrderAmountForDiscount() {
        FlatDiscount discount = FlatDiscount.getInstance();
        double currentAmount = discount.getFlatDiscountOn();

        System.out.println("\n--- Minimum Order Amount Settings ---");

        if (currentAmount == 0) {
            System.out.println("No minimum order amount configured.");
        } else {
            System.out.println("Current Minimum Order Amount: " + currentAmount);
        }

        double newAmount = InputValidation.readPositiveDouble(
                scanner, "Enter minimum order amount required for discount: ");

        discount.setFlatDiscountOn(newAmount);

        System.out.println("Discount will now apply on orders above " + newAmount + ".");
    }

    private void setStatusOfDeliveryPartner() {
        System.out.println("\n--- Change Delivery Partner Status ---");

        List<DeliveryPartner> deliveryPartners = deliveryPartnerService.getDeliveryPartners();

        if (deliveryPartners.isEmpty()) {
            System.out.println("No delivery partners available.");
            return;
        }

        System.out.printf("\n%-20s %-20s %-20s%n", "Delivery Partner Id", "Name", "Status");
        System.out.println("------------------------------------------------------------");

        deliveryPartners.forEach((deliveryPartner) -> {
            System.out.printf("%-20d %-20s %-20s%n",
                    deliveryPartner.getId(),
                    deliveryPartner.getName(),
                    deliveryPartner.getStatus());
        });

        int id = InputValidation.readPositiveInt(scanner, "Enter Delivery Partner Id: ");

        DeliveryPartner selectedPartner = null;

        try {
            selectedPartner = deliveryPartnerService.getDeliveryPartnerById(id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (selectedPartner == null) {
            System.out.println("Delivery Partner not found.");
            return;
        }

        DeliveryPartnerStatus currentStatus = selectedPartner.getStatus();
        DeliveryPartnerStatus newStatus;

        if (currentStatus == DeliveryPartnerStatus.INACTIVE) {
            System.out.println("1. Change to ACTIVE");
        } else {
            System.out.println("1. Change to INACTIVE");
        }
        System.out.println("0. Back");

        int choice = InputValidation.readIntInRange(scanner, "Enter choice: ", 0, 1);

        if (choice == 0) {
            return;
        }

        newStatus = (currentStatus == DeliveryPartnerStatus.INACTIVE)
                ? DeliveryPartnerStatus.ACTIVE
                : DeliveryPartnerStatus.INACTIVE;

        try {
            deliveryPartnerService.changeDeliveryPartnerStatus(selectedPartner, newStatus);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Delivery Partner status updated successfully.");
    }

    private void orderHistory() {
        List<Order> orders = orderService.getOrders();

        if (orders == null || orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        orders.forEach(order -> {

            System.out.println("\n=================================================");
            System.out.println("                  ORDER DETAILS                  ");
            System.out.println("=================================================");

            System.out.println("Order ID      : " + order.getId());
            System.out.println("Status        : " + order.getOrderStatus());

            // Customer Details
            User customer = order.getCustomer();
            System.out.println("\nCustomer Details:");
            System.out.println("  ID          : " + customer.getId());
            System.out.println("  Name        : " + customer.getName());

            // Delivery Partner Details
            User partner = order.getDeliveryPartner();
            if (partner != null) {
                System.out.println("\nDelivery Partner:");
                System.out.println("  ID          : " + partner.getId());
                System.out.println("  Name        : " + partner.getName());
            } else {
                System.out.println("\nDelivery Partner : Not Assigned");
            }

            // Order Items
            System.out.println("\n---------------- ORDER ITEMS ----------------");
            order.getOrderItems().forEach(item -> {
                String itemName = item.getFoodItem().getName();
                int qty = item.getQuantity();
                double price = item.getFoodItem().getPrice();
                double total = qty * price;
                System.out.printf("  %-20s | Qty: %-3d | Price: %-8.2f | Total: %-8.2f%n",
                        itemName, qty, price, total);
            });

            // Bill Summary
            System.out.println("\n---------------- BILL SUMMARY ----------------");
            System.out.printf("  Subtotal      : %.2f%n", order.getTotal());
            System.out.printf("  Discount      : %.1f%%%n", order.getDiscountRate());
            System.out.printf("  Final Amount  : %.2f%n", order.getFinalAmount());
            System.out.println("  Payment       : " + order.getPaymentMode());

            System.out.println("=================================================\n");
        });
    }

    private void summaryOfOrderHistory() {
        Map<String, Object> stats =  orderService.orderStats();

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║         ORDER SUMMARY DASHBOARD      ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf("║  %-20s : %-10d  ║%n", "Total Orders", stats.get("Total Orders"));
        System.out.printf("║  %-20s : %-10d  ║%n", "Out For Delivery", stats.get("Out For Delivery"));
        System.out.printf("║  %-20s : %-10d  ║%n", "Delivered", stats.get("Delivered"));
        System.out.printf("║  %-20s : %-10d  ║%n", "Queued", stats.get("Queued"));
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf("║  %-20s : %-10.2f  ║%n", "Total Earnings (₹)", stats.get("Total Revenue (Delivered)"));
        System.out.println("╚══════════════════════════════════════╝");
    }
}