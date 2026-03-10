package com.foodapp.repository;

import com.foodapp.model.Order;

import java.util.List;

public interface OrderRepository {

    void addOrder(Order order);

    void removeOrder(Order order);

    List<Order> getAllOrders();
}