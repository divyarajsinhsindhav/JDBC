package com.foodapp.repository;

import com.foodapp.model.Order;

import java.util.ArrayList;
import java.util.List;

public class InMemoryOrderRepository implements OrderRepository {

    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        orders.add(order);
    }

    public void removeOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        orders.remove(order);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }
}
