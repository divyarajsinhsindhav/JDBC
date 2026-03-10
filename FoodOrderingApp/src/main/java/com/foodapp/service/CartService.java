package com.foodapp.service;

import com.foodapp.model.*;
import com.foodapp.repository.InMemoryCartRepository;

import java.util.List;
import java.util.Map;

public class CartService {

    private InMemoryCartRepository inMemoryCartRepository;
    private CustomerService customerService;

    public CartService(InMemoryCartRepository inMemoryCartRepository, CustomerService customerService) {
        this.customerService = customerService;
        this.inMemoryCartRepository = inMemoryCartRepository;
    }

    public void addOrderItemToCart(Integer customerId, OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order Item cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer Id cannot be null");
        }
        User customer = customerService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        inMemoryCartRepository.addToCart(customerId, orderItem);
    }

    public OrderItem getOrderItemFromCart(Integer customerId, int orderItemId) {

        User customer = customerService.findCustomerById(customerId);

        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        return inMemoryCartRepository.getCart(customerId)
                .stream()
                .filter(item -> item.getId() == orderItemId)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Order item not found in cart"));
    }

    public void removeOrderItemFromCart(Integer customerId, OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order Item cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer Id cannot be null");
        }
        User customer = customerService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        inMemoryCartRepository.removeFromCart(customerId, orderItem);
    }

    public void updateOrderItemQuantity(Integer customerId, OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order Item cannot be null");
        }

        if (customerId == null) {
            throw new IllegalArgumentException("Customer Id cannot be null");
        }

        User customer = customerService.findCustomerById(customerId);

        OrderItem itemFromCart = inMemoryCartRepository.getCart(customerId)
                .stream()
                .filter(item -> item.getId() == orderItem.getId())
                .findFirst()
                .orElse(null);
        if (itemFromCart == null) {
            throw new IllegalArgumentException("Order item not found in cart");
        }

        itemFromCart.setQuantity(orderItem.getQuantity());
        itemFromCart.setPrice(orderItem.getFoodItem().getPrice() *  orderItem.getQuantity());

        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
    }

    public List<OrderItem> getCart(Integer customerId) {
        User customer = customerService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        return inMemoryCartRepository.getCart(customerId);
    }

    public Map<Integer, List<OrderItem>> getCart() {
        return inMemoryCartRepository.getCart();
    }

    public void clearCustomerCart(Integer customerId) {
        User customer = customerService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        inMemoryCartRepository.clearCart(customerId);
    }

    public OrderItem getFoodItemExisted(Integer customerId, FoodItem foodItem) {
        return inMemoryCartRepository.getCart(customerId).
                stream()
                .filter(item -> item.getFoodItem().getId() == foodItem.getId())
                .findFirst()
                .orElse(null);
    }

    public void updateOrderItemQuantityIfAlreadyExist(int customerId, OrderItem foodItemExisted, int quantity) {
        inMemoryCartRepository.getCart(customerId).stream()
                .filter(item -> item.getId() == foodItemExisted.getId())
                .findFirst()
                .ifPresent(orderItem -> {
                    orderItem.setQuantity(foodItemExisted.getQuantity() + quantity);
                    orderItem.setPrice(foodItemExisted.getFoodItem().getPrice() * foodItemExisted.getQuantity());
                });

    }
}
