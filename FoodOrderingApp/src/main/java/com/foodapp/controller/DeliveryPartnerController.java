package com.foodapp.controller;

import com.foodapp.model.DeliveryPartner;
import com.foodapp.model.Order;
import com.foodapp.model.User;
import com.foodapp.service.DeliveryPartnerService;
import com.foodapp.utils.InputValidation;
import com.foodapp.utils.SessionManager;

import java.util.Scanner;

public class DeliveryPartnerController {

    private DeliveryPartnerService deliveryPartnerService;
    private SessionManager sessionManager;
    private Scanner scanner = new Scanner(System.in);

    public DeliveryPartnerController(DeliveryPartnerService deliveryPartnerService, SessionManager sessionManager) {
        this.deliveryPartnerService = deliveryPartnerService;
        this.sessionManager = sessionManager;
    }

    public void displayOption() {
        while (sessionManager.isLoggedIn()) {
            System.out.println("\n================ Delivery Partner Menu =========");
            System.out.println("1. Deliver Order");
            System.out.println("2. Order History");
            System.out.println("3. Log out");
            int choice = InputValidation.readIntInRange(scanner, "Enter your choice: ", 1, 3);
            try {
                switch (choice) {
                    case 1 -> deliverOrder();
                    case 2 -> getDeliveryPartnersOrder();
                    case 3 -> logout();
                }
            } catch (Exception e) {
                System.out.println("Error in getting delivery partner data");
            }
        }
    }

    private void logout() {

        sessionManager.logout();

        System.out.println("Logged out successfully.");
    }

    private void deliverOrder() {
        User deliveryPartner = sessionManager.getCurrentCustomer();
        deliveryPartnerService.deliverOrder(deliveryPartner.getEmail());
    }

    public void getDeliveryPartnersOrder() {
        User deliveryPartner = sessionManager.getCurrentCustomer();
        deliveryPartnerService.getOrdersByDeliveryPartner(deliveryPartner.getEmail())
                .forEach(order -> {

                    System.out.println("\n+-------------------------------------------+");
                    System.out.println(" Order ID : " + order.getId());
                    System.out.println(" Customer : " + order.getCustomer().getName());
                    System.out.println(" Items    :");
                    System.out.println("---------------------------------------------");

                    order.getOrderItems().forEach(item -> {
                        System.out.printf("   %-25s ₹%-8.2f%n",
                                item.getFoodItem().getName(),
                                item.getPrice());
                    });

                    System.out.println("---------------------------------------------");
                    System.out.printf(" Final Amount : ₹%.2f%n", order.getFinalAmount());
                    System.out.println("+-------------------------------------------+");
                });
    }

}
