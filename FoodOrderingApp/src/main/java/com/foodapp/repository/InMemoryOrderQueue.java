package com.foodapp.repository;

import com.foodapp.model.Order;

import java.util.LinkedList;
import java.util.Queue;

public class InMemoryOrderQueue {
    private Queue<Order> queue;

    public InMemoryOrderQueue() {
        queue = new LinkedList<>();
    }

    public Queue<Order> getQueue() {
        return queue;
    }

    public Order getNext() {
        return queue.poll();
    }

    public void put(Order order) {
        queue.add(order);
    }
}
