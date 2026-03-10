package com.foodapp.utils;

import java.util.Random;

public class IdGenerator {

    private static Random random = new Random();

    private static int menuIDCounter = 1000;
    private static int categoryIDCounter = 1000;
    private static int itemIDCounter = 1000;
    private static int deliveryPartnerIDCounter = 1000;
    private static int customerIDCounter = 1000;
    private static int orderIDCounter = 1000;
    private static int orderItemIDCounter = 1000;
    private static int userIDCounter = 1000;

    private IdGenerator() { }

    public static int getRandomID() {
        return random.nextInt(900) + 100;
    }

    public static int getNextMenuID() {
        return ++menuIDCounter;
    }

    public static int getNextCategoryID() {
        return ++categoryIDCounter;
    }

    public static int getNextItemID() {
        return ++itemIDCounter;
    }

    public static int getNextDeliveryPartnerID() {
        return ++deliveryPartnerIDCounter;
    }

    public static int getNextCustomerID() {
        return ++customerIDCounter;
    }

    public static int getNextOrderID() {
        return ++orderIDCounter;
    }

    public static int getNextOrderItemID() {
        return ++orderItemIDCounter;
    }

    public static int getNextUserID() {
        return ++userIDCounter;
    }
}
