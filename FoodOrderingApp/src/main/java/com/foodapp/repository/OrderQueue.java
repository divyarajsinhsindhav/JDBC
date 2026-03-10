package com.foodapp.repository;

import com.foodapp.model.Order;

import java.util.Queue;

public interface OrderQueue {
    Queue<Order> getQueue();
    Order getNext();
    void put(Order order);
}
