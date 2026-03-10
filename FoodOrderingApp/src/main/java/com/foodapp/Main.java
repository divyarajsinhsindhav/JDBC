package com.foodapp;

import com.foodapp.controller.AdminController;
import com.foodapp.controller.AuthController;
import com.foodapp.controller.CustomerController;
import com.foodapp.controller.DeliveryPartnerController;
import com.foodapp.model.DeliveryPartner;
import com.foodapp.model.UserType;
import com.foodapp.repository.*;
import com.foodapp.seeder.AdminSeeder;
import com.foodapp.seeder.CustomerSeeder;
import com.foodapp.seeder.DeliveryPartnerSeeder;
import com.foodapp.seeder.MenuSeeder;
import com.foodapp.service.*;
import com.foodapp.utils.InputValidation;
import com.foodapp.utils.SessionManager;

import java.util.Scanner;

public class Main {

    private static AdminController adminController;
    private static CustomerController customerController;
    private static DeliveryPartnerController deliveryPartnerController;
    private static AuthController authController;

    public static void main(String[] args) {

        initialize();

        start();
    }

    private static void initialize() {

        UserRepository userRepository = new InMemoryUserRepository();
        InMemoryCartRepository inMemoryCartRepository = new InMemoryCartRepository();
        InMemoryOrderRepository inMemoryOrderRepository = new InMemoryOrderRepository();

        CustomerService customerService = new CustomerService(userRepository);
        CartService cartService = new CartService(inMemoryCartRepository, customerService);
        MenuService menuService = new MenuService(cartService);
        DeliveryPartnerService deliveryPartnerService = new DeliveryPartnerService(userRepository,
                inMemoryOrderRepository);
        DiscountService discountService = new DiscountService();
        InMemoryOrderQueue inMemoryOrderQueue = new InMemoryOrderQueue();
        OrderService orderService = new OrderService(deliveryPartnerService, inMemoryOrderRepository,
                inMemoryCartRepository, discountService, inMemoryOrderQueue);
        deliveryPartnerService.setOrderService(orderService);
        AuthService authService = new AuthService(userRepository);

        SessionManager sessionManager = SessionManager.getSessionManager();

        adminController = new AdminController(menuService, deliveryPartnerService, orderService);

        customerController = new CustomerController(
                customerService,
                menuService,
                orderService,
                cartService,
                sessionManager,
                deliveryPartnerService,
                discountService);

        deliveryPartnerController = new DeliveryPartnerController(deliveryPartnerService, sessionManager);

        authController = new AuthController(userRepository, authService, sessionManager);

        MenuSeeder.seed(menuService);
        AdminSeeder.seed(authService);
        CustomerSeeder.seed(authService);
        DeliveryPartnerSeeder.seed(authService);
    }

    private static void start() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println("\n===== FOOD ORDERING SYSTEM =====");
            // System.out.println("1. Register as Admin");
            System.out.println("1. Register as Customer");
            System.out.println("2. Register as DeliveryPartner");
            System.out.println("3. Login");
            System.out.println("4. Exit");

            int choice = InputValidation.readIntInRange(scanner, "Enter your choice: ", 1, 4);

            try {

                switch (choice) {
                    // case 1 -> registerAdmin();
                    case 1 -> registerCustomer();
                    case 2 -> registerDeliveryPartner();
                    case 3 -> login();
                    case 4 -> {
                        System.out.println("Exiting application...");
                        return;
                    }
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void registerAdmin() {
        authController.registerUser(UserType.ADMIN);
    }

    private static void registerCustomer() {
        authController.registerUser(UserType.CUSTOMER);
    }

    private static void registerDeliveryPartner() {
        authController.registerUser(UserType.DELIVERY_PARTNER);
    }

    private static void login() {
        UserType userType = authController.loginUser().getRole();
        if (userType == null) {
            throw new IllegalArgumentException("Invalid user role");
        }
        try {
            switch (userType) {
                case ADMIN -> admin();
                case CUSTOMER -> customer();
                case DELIVERY_PARTNER -> deliveryPartner();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void admin() {
        adminController.displayOptions();
    }

    private static void customer() {
        customerController.displayOption();
    }

    private static void deliveryPartner() {
        deliveryPartnerController.displayOption();
    }
}