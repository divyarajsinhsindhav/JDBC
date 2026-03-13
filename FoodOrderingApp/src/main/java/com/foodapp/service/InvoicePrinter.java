package com.foodapp.service;

import com.foodapp.model.Order;

import com.foodapp.model.*;

public class InvoicePrinter {

    public static void printInvoice(Order order) {
        Customer customer = (Customer) order.getCustomer();
        DeliveryPartner partner = (DeliveryPartner) order.getDeliveryPartner();

        String line  = "┌─────────────────────────────────────────────────────┐";
        String mid   = "├─────────────────────────────────────────────────────┤";
        String end   = "└─────────────────────────────────────────────────────┘";

        System.out.println("\n" + line);
        System.out.printf( "│  %-50s │%n", "FOOD APP  —  Invoice #ORD-" + String.format("%05d", order.getId()));
        System.out.println(mid);
        System.out.printf( "│  Customer : %-38s │%n", customer.getName());
        System.out.printf( "│  Email    : %-38s │%n", customer.getEmail());
        System.out.printf( "│  Phone    : %-38s │%n", customer.getPhone());
        if (partner != null)
            System.out.printf("│  Delivery : %-38s │%n", partner.getName());
        System.out.printf( "│  Payment  : %-38s │%n", order.getPaymentMode());
        System.out.println(mid);
        System.out.printf( "│  %-4s  %-22s  %-4s  %-7s  %-7s │%n",
                "#", "Item", "Qty", "Price", "Total");
        System.out.println(mid);

        int i = 1;
        for (OrderItem item : order.getOrderItems()) {
            double total = item.getFoodItem().getPrice() * item.getQuantity();
            System.out.printf("│  %-4d  %-22s  %-4d  %-7.2f  %-7.2f │%n",
                    i++,
                    item.getFoodItem().getName(),
                    item.getQuantity(),
                    item.getFoodItem().getPrice(),
                    total);
        }

        double discount   = order.getDiscountRate();
        double subtotal   = order.getTotal();
        double finalAmt   = order.getFinalAmount();

        System.out.println(mid);
        System.out.printf( "│  %-40s %-9.2f │%n", "Subtotal:",  subtotal);
        System.out.printf( "│  %-40s %-9.2f │%n", "Discount (" + (int) discount + "%):", subtotal - finalAmt);
        System.out.printf( "│  %-40s %-9.2f │%n", "Total:", finalAmt);
        System.out.println(mid);
        System.out.printf( "│  %-50s │%n", "Thank you for ordering with us!");
        System.out.println(end + "\n");
    }
}
