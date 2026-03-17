package com.foodapp.service;

import com.foodapp.exception.EmptyCartException;
import com.foodapp.model.*;
import com.foodapp.repository.CartRepository;
import com.foodapp.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private DeliveryPartnerService deliveryPartnerService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private OrderItem item;
    private FoodItem food;

    @BeforeEach
    void setUp() {
        customer = new Customer(1, "Test User", "test@test.com", "password", "1234567890", "123 Test St");
        food = new FoodItem(10, "Burger", 50.0);
        item = new OrderItem(100, food, 2, 50.0);
    }

    @Test
    void placeOrder_EmptyCart_ThrowsException() {
        assertThrows(EmptyCartException.class, () -> orderService.placeOrder(customer, PaymentMode.CASH, Collections.emptyList()));
    }

    @Test
    void placeOrder_NullCustomer_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(null, PaymentMode.CASH, Collections.singletonList(item)));
    }

    @Test
    void placeOrder_PartnerAssigned_Success() {
        List<OrderItem> cart = Collections.singletonList(item);

        when(discountService.applyDiscount(100.0)).thenReturn(10.0); // 10% discount -> 90.0

        DeliveryPartner partner = new DeliveryPartner(2, "Partner 1", "partner@test.com", "pass", "123");
        when(deliveryPartnerService.getFreeDeliveryPartner()).thenReturn(partner);

        Order placedOrder = orderService.placeOrder(customer, PaymentMode.CASH, cart);

        assertNotNull(placedOrder);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, placedOrder.getOrderStatus());
        assertEquals(partner, placedOrder.getDeliveryPartner());
        
        verify(deliveryPartnerService).changeDeliveryPartnerStatus(partner, DeliveryPartnerStatus.BUSY);
        verify(orderRepository).addOrder(placedOrder);
    }

    @Test
    void placeOrder_NoFreePartner_QueuedOrder() {
        List<OrderItem> cart = Collections.singletonList(item);

        when(discountService.applyDiscount(100.0)).thenReturn(0.0);

        when(deliveryPartnerService.getFreeDeliveryPartner()).thenReturn(null);
        when(orderRepository.getOrdersByStatus(OrderStatus.QUEUED)).thenReturn(Collections.emptyList());

        Order placedOrder = orderService.placeOrder(customer, PaymentMode.CASH, cart);

        assertNotNull(placedOrder);
        assertEquals(OrderStatus.QUEUED, placedOrder.getOrderStatus());
        assertNull(placedOrder.getDeliveryPartner());
        
        verify(orderRepository).addOrder(placedOrder);
    }

    @Test
    void deliverOrder_ValidOrder_Success() {
        Order order = new Order(1, Collections.singletonList(item), customer, 100.0, 0.0, PaymentMode.CASH, 100.0);
        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        
        DeliveryPartner partner = new DeliveryPartner(2, "Partner", "partner@test.com", "pass", "123");

        orderService.deliverOrder(order, partner);

        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
        verify(orderRepository).updateOrderStatus(order);
        verify(deliveryPartnerService).changeDeliveryPartnerStatus(partner, DeliveryPartnerStatus.ACTIVE);
    }

    @Test
    void deliverOrder_InvalidStatus_ThrowsException() {
        Order order = new Order(1, Collections.singletonList(item), customer, 100.0, 0.0, PaymentMode.CASH, 100.0);
        order.setOrderStatus(OrderStatus.QUEUED);
        
        DeliveryPartner partner = new DeliveryPartner(2, "Partner", "partner@test.com", "pass", "123");

        assertThrows(IllegalStateException.class, () -> orderService.deliverOrder(order, partner));
    }

    @Test
    void assignQueuedOrderIfAny_PartnerActive_AssignsNextOrder() {
        DeliveryPartner partner = new DeliveryPartner(2, "Partner", "partner@test.com", "pass", "123");
        partner.setStatus(DeliveryPartnerStatus.ACTIVE);

        Order queuedOrder1 = new Order(1, Collections.singletonList(item), customer, 100.0, 0.0, PaymentMode.CASH, 100.0);
        queuedOrder1.setOrderStatus(OrderStatus.QUEUED);

        Order queuedOrder2 = new Order(2, Collections.singletonList(item), customer, 100.0, 0.0, PaymentMode.CASH, 100.0);
        queuedOrder2.setOrderStatus(OrderStatus.QUEUED);
        
        // List is returned in DESC order of queue wait, so last element is oldest
        when(orderRepository.getOrdersByStatus(OrderStatus.QUEUED)).thenReturn(Arrays.asList(queuedOrder1, queuedOrder2));

        orderService.assignQueuedOrderIfAny(partner);

        // Next order should be queuedOrder2
        assertEquals(partner, queuedOrder2.getDeliveryPartner());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, queuedOrder2.getOrderStatus());
        verify(orderRepository).updateOrderStatus(queuedOrder2);
        verify(deliveryPartnerService).changeDeliveryPartnerStatus(partner, DeliveryPartnerStatus.BUSY);
        
        // Ensure queuedOrder1 is not assigned
        assertEquals(OrderStatus.QUEUED, queuedOrder1.getOrderStatus());
    }

    @Test
    void assignQueuedOrderIfAny_NoQueuedOrders_DoesNothing() {
        DeliveryPartner partner = new DeliveryPartner(2, "Partner", "partner@test.com", "pass", "123");
        partner.setStatus(DeliveryPartnerStatus.ACTIVE);

        when(orderRepository.getOrdersByStatus(OrderStatus.QUEUED)).thenReturn(Collections.emptyList());

        orderService.assignQueuedOrderIfAny(partner);

        verify(deliveryPartnerService, never()).changeDeliveryPartnerStatus(any(), any());
    }
}
