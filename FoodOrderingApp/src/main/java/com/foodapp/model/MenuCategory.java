package com.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class MenuCategory implements Menu {
    private int id;
    private String category;
    private int parentId;
    private List<Menu> items = new ArrayList<>();

    public MenuCategory() {

    }

    public MenuCategory(String category) {
        this.category = category;
    }

    public MenuCategory(int id, String category) {
        this(category);
        this.id = id;
    }

    public MenuCategory(int id, String category, int parentId) {
        this(id, category);
        this.parentId = parentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
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

    @Override
    public void render(int level) {

        String indent = indent(level);

        if (level == 0) {
            System.out.println("\n==================================================");
            System.out.printf("%s%s%n", indent, category.toUpperCase());
            System.out.println("==================================================");
        } else {
            System.out.printf("\n%s[ %s ]%n", indent, category.toUpperCase());
            System.out.printf("%s--------------------------------------------------%n", indent);
        }

        for (Menu item : items) {
            item.render(level + 1);
        }
    }
}