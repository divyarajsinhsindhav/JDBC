package com.foodapp.service;

import com.foodapp.model.*;
import com.foodapp.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CartService cartService;

    private Customer customer;
    private OrderItem item;
    private FoodItem food;

    @BeforeEach
    void setUp() {
        // Constructor: Customer(int id, String name, String email, String password, String phone, String address)
        customer = new Customer(1, "Test User", "test@test.com", "password", "1234567890", "123 Test St");
        
        // Constructor: FoodItem(int id, String name, double price)
        food = new FoodItem(10, "Burger", 50.0);
        
        // Constructor: OrderItem(int id, FoodItem foodItem, int quantity, double price)
        item = new OrderItem(100, food, 2, 50.0);
    }

    @Test
    void addOrderItemToCart_CustomerNotFound_ThrowsException() {
        when(customerService.findCustomerById(1)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> cartService.addOrderItemToCart(1, item));
    }

    @Test
    void addOrderItemToCart_NullItem_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addOrderItemToCart(1, null));
    }

    @Test
    void addOrderItemToCart_ValidItem_Success() {
        when(customerService.findCustomerById(1)).thenReturn(customer);

        cartService.addOrderItemToCart(1, item);

        verify(cartRepository).addToCart(1, item);
    }

    @Test
    void getOrderItemFromCart_ItemExists_ReturnsItem() {
        when(customerService.findCustomerById(1)).thenReturn(customer);
        when(cartRepository.getCartItem(1, 100)).thenReturn(item);

        OrderItem result = cartService.getOrderItemFromCart(1, 100);

        assertNotNull(result);
        assertEquals(100, result.getId());
    }

    @Test
    void getOrderItemFromCart_ItemDoesNotExist_ThrowsException() {
        when(customerService.findCustomerById(1)).thenReturn(customer);
        when(cartRepository.getCartItem(1, 100)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> cartService.getOrderItemFromCart(1, 100));
    }

    @Test
    void removeOrderItemFromCart_ValidItem_Success() {
        when(customerService.findCustomerById(1)).thenReturn(customer);

        cartService.removeOrderItemFromCart(1, item);

        verify(cartRepository).removeCartItem(1, 100);
    }

    @Test
    void updateOrderItemQuantity_ValidItem_Success() {
        when(customerService.findCustomerById(1)).thenReturn(customer);
        when(cartRepository.getCartItem(1, 100)).thenReturn(item);

        item.setQuantity(5);
        cartService.updateOrderItemQuantity(1, item);

        verify(cartRepository).updateCartItemQuantity(1, 100, 5);
    }

    @Test
    void updateOrderItemQuantity_ItemNotFound_ThrowsException() {
        when(customerService.findCustomerById(1)).thenReturn(customer);
        when(cartRepository.getCartItem(1, 100)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> cartService.updateOrderItemQuantity(1, item));
    }

    @Test
    void getCart_ValidCustomer_ReturnsCart() {
        when(customerService.findCustomerById(1)).thenReturn(customer);
        when(cartRepository.getCart(1)).thenReturn(Collections.singletonList(item));

        List<OrderItem> cart = cartService.getCart(1);

        assertNotNull(cart);
        assertEquals(1, cart.size());
    }

    @Test
    void clearCustomerCart_ValidCustomer_Success() {
        when(customerService.findCustomerById(1)).thenReturn(customer);

        cartService.clearCustomerCart(1);

        verify(cartRepository).clearCart(1);
    }

    @Test
    void removeFoodItemFromAllCarts_Success() {
        cartService.removeFoodItemFromAllCarts(10);
        verify(cartRepository).removeFoodItemFromAllCarts(10);
    }
}
