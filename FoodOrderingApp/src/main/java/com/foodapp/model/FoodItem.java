package com.foodapp.model;

public class FoodItem implements Menu {
    private int id;
    private String name;
    private double price;
    private int categoryId;

    public FoodItem() {

    }

    public FoodItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public FoodItem(int id, String name, double price) {
        this(name, price);
        this.id = id;
    }

    public FoodItem(int id, String name, double price, int categoryId) {
        this(id, name, price);
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setPrice(double newPrice) {
        this.price = newPrice;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public void render(int level) {

        String text = id + "  " + name;

        int width = 40;
        int dots = Math.max(2, width - text.length());

        System.out.printf("%s%s%s ₹%.2f%n",
                indent(level),
                text,
                ".".repeat(dots),
                price);
    }
}