package com.foodapp.model;

public class OrderItem {
    private int id;
    private FoodItem foodItem;
    private int quantity;
    private double price;

    public OrderItem(int id, FoodItem foodItem, int quantity,  double price) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }

        if (foodItem == null) {
            throw new IllegalArgumentException("foodItem must not be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        this.id = id;
        this.foodItem = foodItem;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
