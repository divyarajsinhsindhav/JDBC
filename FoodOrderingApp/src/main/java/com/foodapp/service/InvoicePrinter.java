package com.foodapp.service;

import com.foodapp.model.Order;

import com.foodapp.model.*;

public class InvoicePrinter {

    public static void printInvoice(Order order) {

        System.out.println("\n==============================================");
        System.out.println("                FOOD APP INVOICE              ");
        System.out.println("==============================================");

        Customer customer = (Customer) order.getCustomer();
        DeliveryPartner partner = (DeliveryPartner) order.getDeliveryPartner();

        System.out.println("Order ID      : " + order.getId());
        System.out.println("Customer Name : " + customer.getName());
        System.out.println("Email         : " + customer.getEmail());
        System.out.println("Phone         : " + customer.getPhone());

        if (partner != null) {
            System.out.println("Delivery By   : " + partner.getName());
        }

        System.out.println("Payment Mode  : " + order.getPaymentMode());

        System.out.println("\n--------------------------------------------------------------");
        System.out.printf("%-5s %-20s %-10s %-10s %-10s%n",
                "ID", "Item", "Qty", "Price", "Total");
        System.out.println("--------------------------------------------------------------");

        double subtotal = 0;

        for (OrderItem item : order.getOrderItems()) {

            double total = item.getFoodItem().getPrice() * item.getQuantity();
            subtotal += total;

            System.out.printf("%-5d %-20s %-10d %-10.2f %-10.2f%n",
                    item.getId(),
                    item.getFoodItem().getName(),
                    item.getQuantity(),
                    item.getFoodItem().getPrice(),
                    total
            );
        }

        System.out.println("--------------------------------------------------------------");

        double discount = order.getDiscountRate();
        double finalAmount = order.getFinalAmount();

        System.out.printf("%-40s %.2f%n", "Subtotal:", subtotal);
        System.out.printf("%-40s %.2f%n", "Discount Rate:", discount);
        System.out.printf("%-40s %.2f%n", "Final Amount:", finalAmount);

        System.out.println("==============================================================");
        System.out.println("            Thank you for ordering with us!                   ");
        System.out.println("==============================================================");
    }
}
