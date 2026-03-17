package com.foodapp.service;

import com.foodapp.model.DeliveryPartner;
import com.foodapp.model.DeliveryPartnerStatus;
import com.foodapp.model.Order;
import com.foodapp.model.OrderStatus;
import com.foodapp.repository.OrderRepository;
import com.foodapp.repository.UserRepository;
import com.foodapp.utils.InputValidation;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DeliveryPartnerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final Random random;
    private final Scanner scanner = new Scanner(System.in);
    private OrderService orderService;

    public DeliveryPartnerService(UserRepository userRepository,
            OrderRepository orderRepository) {
        if (userRepository == null || orderRepository == null) {
            throw new IllegalArgumentException("Repositories cannot be null");
        }
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.random = new Random();
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public List<DeliveryPartner> getDeliveryPartners() {
        return userRepository.getDeliveryPartners();
    }

    public boolean checkDeliveryPartnerAvailable() {
        return userRepository.getDeliveryPartners()
                .stream()
                .anyMatch(dp -> dp.getStatus() != DeliveryPartnerStatus.INACTIVE);
    }

    public DeliveryPartner getFreeDeliveryPartner() {
        List<DeliveryPartner> freePartners = userRepository.getDeliveryPartners()
                .stream()
                .filter(dp -> dp.getStatus() == DeliveryPartnerStatus.ACTIVE)
                .collect(Collectors.toList());

        if (freePartners.isEmpty()) {
            return null;
        }
        return freePartners.get(random.nextInt(freePartners.size()));
    }

    public DeliveryPartner getDeliveryPartnerById(int id) {
        DeliveryPartner deliveryPartner = userRepository.getDeliveryPartnerById(id);
        if (deliveryPartner == null) {
            throw new IllegalArgumentException("DeliveryPartner with id " + id + " not found");
        }
        return deliveryPartner;
    }

    public DeliveryPartner getDeliveryPartnerByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return userRepository.getDeliveryPartners()
                .stream()
                .filter(partner -> email.equals(partner.getEmail()))
                .findFirst()
                .orElse(null);
    }

    public List<Order> getOrdersByDeliveryPartner(int id) {
        DeliveryPartner partner = getDeliveryPartnerById(id);
        List<Order> orders = orderRepository.getAllOrders()
                .stream()
                .filter(order -> order.getDeliveryPartner() != null
                        && order.getDeliveryPartner().getId() == id)
                .collect(Collectors.toList());
        return orders.isEmpty() ? Collections.emptyList() : orders;
    }

    public List<Order> getOrdersByDeliveryPartner(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        DeliveryPartner partner = getDeliveryPartnerByEmail(email);
        if (partner == null) {
            throw new IllegalArgumentException("Delivery Partner not found with email: " + email);
        }
        List<Order> orders = orderRepository.getAllOrders()
                .stream()
                .filter(order -> order.getDeliveryPartner() != null
                        && email.equals(order.getDeliveryPartner().getEmail()))
                .collect(Collectors.toList());
        return orders.isEmpty() ? Collections.emptyList() : orders;
    }

    public void changeDeliveryPartnerStatus(DeliveryPartner deliveryPartner, DeliveryPartnerStatus status) {
        if (deliveryPartner == null) {
            throw new IllegalArgumentException("DeliveryPartner cannot be null");
        }
        deliveryPartner.setStatus(status);

        userRepository.updateDeliveryPartnerStatus(deliveryPartner.getId(), status);

        if (status == DeliveryPartnerStatus.ACTIVE && orderService != null) {
            orderService.assignQueuedOrderIfAny(deliveryPartner);
        }
    }

    /**
     * Delivery partner marks their OUT_FOR_DELIVERY order as DELIVERED.
     * Then the partner becomes ACTIVE, and the next queued order (if any) is
     * auto-assigned.
     */
    public void deliverOrder(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        DeliveryPartner partner = getDeliveryPartnerByEmail(email);
        if (partner == null) {
            throw new IllegalArgumentException("Delivery Partner not found with email: " + email);
        }

        List<Order> activeOrders = getOrdersByDeliveryPartner(email)
                .stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.OUT_FOR_DELIVERY)
                .collect(Collectors.toList());

        if (activeOrders.isEmpty()) {
            System.out.println("You have no orders currently out for delivery.");
            return;
        }

        System.out.println("\n--- Your Active Deliveries ---");
        activeOrders.forEach(o -> System.out.printf("Order ID: %-5d | Customer: %-20s | Amount: %.2f%n",
                o.getId(), o.getCustomer().getName(), o.getFinalAmount()));

        int orderId = InputValidation.readPositiveInt(scanner, "Enter Order ID to mark as DELIVERED: ");

        Order order = activeOrders.stream()
                .filter(o -> o.getId() == orderId)
                .findFirst()
                .orElse(null);

        if (order == null) {
            System.out.println("Order ID not found in your active deliveries.");
            return;
        }

        // Delegate to OrderService — it handles status, freeing partner, and auto-queue
        orderService.deliverOrder(order, partner);
    }
}