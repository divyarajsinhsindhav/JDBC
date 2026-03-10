package com.foodapp.repository;

import com.foodapp.model.OrderItem;

import java.util.List;

public interface CartRepository {
    void addToCart(Integer customerId, OrderItem orderItem);
    void removeFromCart(Integer customerId, OrderItem orderItem);
    void clearCart(Integer customerId);
    List<OrderItem> getCart(Integer customerId);
}
