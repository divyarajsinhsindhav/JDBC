package com.foodapp.repository;

import com.foodapp.model.Order;
import com.foodapp.model.OrderStatus;

import java.util.List;

public interface OrderRepository {

    void addOrder(Order order);

    void removeOrder(Order order);

    List<Order> getAllOrders();

    List<Order> getAllOrdersByCustomerId(int id);

    List<Order> getAllOrdersByDeliveryPartnerId(int id);

    List<Order> getOrdersByStatus(OrderStatus status);

    void updateOrderStatus(Order order);


}