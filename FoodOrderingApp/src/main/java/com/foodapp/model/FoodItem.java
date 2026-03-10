package com.foodapp.model;

import java.util.List;

public class FoodItem implements Menu {
    private int id;
    private String name;
    private double price;

    public FoodItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
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

    @Override
    public void add(Menu item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Menu item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Menu> getMenu() {
        throw new UnsupportedOperationException();
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

    public void setName(String newName) {
        this.name = newName;
    }

    public void setPrice(double newPrice) {
        this.price = newPrice;
    }
}
