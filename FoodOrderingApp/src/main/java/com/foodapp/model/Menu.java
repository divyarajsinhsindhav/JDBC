package com.foodapp.model;

import java.util.List;

public interface Menu {

    default void add(Menu item) {
        throw new UnsupportedOperationException("Not supported.");
    }

    default void remove(Menu item) {
        throw new UnsupportedOperationException("Not supported.");
    }

    default List<Menu> getMenu() {
        throw new UnsupportedOperationException("Not supported.");
    }

    void render(int level);

    default String indent(int level) {
        return "  ".repeat(level);
    }
}