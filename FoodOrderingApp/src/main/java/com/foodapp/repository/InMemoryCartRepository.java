package com.foodapp.repository;

import com.foodapp.model.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryCartRepository implements CartRepository {
    private Map<Integer, List<OrderItem>> cart = new HashMap<>();

    public void addToCart(Integer customerId, OrderItem item) {
        cart.putIfAbsent(customerId, new ArrayList<>());
        cart.get(customerId).add(item);
    }

    public void removeFromCart(Integer customerId, OrderItem item) {
        if (cart.containsKey(customerId)) {
            cart.get(customerId).remove(item);
        }
    }

    public void clearCart(Integer customerId) {
        cart.remove(customerId);
    }

    public List<OrderItem> getCart(Integer customerId) {
        return cart.getOrDefault(customerId, new ArrayList<>());
    }

    public Map<Integer, List<OrderItem>> getCart() {
        return cart;
    }
}
