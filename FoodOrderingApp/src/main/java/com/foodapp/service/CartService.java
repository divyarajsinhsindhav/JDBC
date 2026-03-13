package com.foodapp.service;

import com.foodapp.model.*;
import com.foodapp.repository.CartRepository;

import java.util.List;
import java.util.Map;

public class CartService {

    private final CartRepository cartRepository;
    private final CustomerService customerService;

    public CartService(CartRepository cartRepository, CustomerService customerService) {
        this.customerService = customerService;
        this.cartRepository = cartRepository;
    }

    public void addOrderItemToCart(Integer customerId, OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order Item cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer Id cannot be null");
        }
        validateCustomer(customerId);
        cartRepository.addToCart(customerId, orderItem);
    }

    public OrderItem getOrderItemFromCart(Integer customerId, int orderItemId) {
        validateCustomer(customerId);
        OrderItem item = cartRepository.getCartItem(customerId, orderItemId);
        if (item == null) {
            throw new IllegalArgumentException("Order item not found in cart");
        }
        return item;
    }

    public void removeOrderItemFromCart(Integer customerId, OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order Item cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer Id cannot be null");
        }
        validateCustomer(customerId);
        cartRepository.removeCartItem(customerId, orderItem.getId());
    }

    public void updateOrderItemQuantity(Integer customerId, OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order Item cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer Id cannot be null");
        }
        validateCustomer(customerId);

        // Verify item exists in cart before updating
        OrderItem existing = cartRepository.getCartItem(customerId, orderItem.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Order item not found in cart");
        }

        cartRepository.updateCartItemQuantity(customerId, orderItem.getId(), orderItem.getQuantity());
    }

    public List<OrderItem> getCart(Integer customerId) {
        validateCustomer(customerId);
        return cartRepository.getCart(customerId);
    }

    public Map<Integer, List<OrderItem>> getCart() {
        return cartRepository.getCart();
    }

    public void clearCustomerCart(Integer customerId) {
        validateCustomer(customerId);
        cartRepository.clearCart(customerId);
    }

    public OrderItem getFoodItemExisted(Integer customerId, FoodItem foodItem) {
        return cartRepository.getCart(customerId)
                .stream()
                .filter(item -> item.getFoodItem().getId() == foodItem.getId())
                .findFirst()
                .orElse(null);
    }

    public void updateOrderItemQuantityIfAlreadyExist(int customerId, OrderItem existingItem, int additionalQuantity) {
        int newQuantity = existingItem.getQuantity() + additionalQuantity;
        cartRepository.updateCartItemQuantity(customerId, existingItem.getId(), newQuantity);
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────────

    private void validateCustomer(Integer customerId) {
        User customer = customerService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
    }
}