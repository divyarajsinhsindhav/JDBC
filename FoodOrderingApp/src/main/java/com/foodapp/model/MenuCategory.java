package com.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class MenuCategory implements Menu {
    private int id;
    private String category;
    private List<Menu> items = new ArrayList<>();

    public MenuCategory(int id, String category) {
        this.id = id;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public void add(Menu item) {
        items.add(item);
    }

    @Override
    public void remove(Menu item) {
        items.remove(item);
    }

    @Override
    public List<Menu> getMenu() {
        return items;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public void render(int level) {

        String indent = indent(level);

        if (level == 0) {
            System.out.println("\n==================================================");
            System.out.printf("%s%s%n", indent, category.toUpperCase());
            System.out.println("==================================================");
        }
        else {
            System.out.printf("\n%s[ %s ]%n", indent, category.toUpperCase());
            System.out.printf("%s--------------------------------------------------%n", indent);
        }

        for (Menu item : items) {
            item.render(level + 1);
        }
    }
}
