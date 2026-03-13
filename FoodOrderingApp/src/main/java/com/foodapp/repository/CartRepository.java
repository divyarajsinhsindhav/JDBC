package com.foodapp.repository;

import com.foodapp.model.OrderItem;

import java.util.List;
import java.util.Map;

public interface CartRepository {
    Map<Integer, List<OrderItem>> getCart();
    void addToCart(Integer customerId, OrderItem orderItem);
    void removeFromCart(Integer customerId, OrderItem orderItem);
    void clearCart(Integer customerId);
    List<OrderItem> getCart(Integer customerId);
    OrderItem getCartItem(Integer customerId, Integer orderId);
    void removeCartItem(Integer customerId, Integer orderId);
    void updateCartItemQuantity(Integer customerId, Integer orderId, Integer quantity);

}
