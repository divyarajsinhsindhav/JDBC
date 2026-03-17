package com.foodapp.service;

import com.foodapp.exception.EmptyCartException;
import com.foodapp.model.*;
import com.foodapp.repository.CartRepository;
import com.foodapp.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    private final DeliveryPartnerService deliveryPartnerService;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final DiscountService discountService;

    public OrderService(DeliveryPartnerService deliveryPartnerService,
                        OrderRepository orderRepository,
                        CartRepository cartRepository,
                        DiscountService discountService) {
        this.deliveryPartnerService = deliveryPartnerService;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.discountService = discountService;
    }

    public List<Order> getOrders() {
        return orderRepository.getAllOrders();
    }

    /**
     * Places an order and immediately tries to assign a free delivery partner.
     * Logic:
     * - If all partners are INACTIVE → Customer unable to place order.
     * - If a free ACTIVE partner exists → assign randomly, status = OUT_FOR_DELIVERY, partner = BUSY.
     * - If all ACTIVE partners are BUSY → status = QUEUED, persisted in orders table.
     */
    public Order placeOrder(User customer, PaymentMode mode, List<OrderItem> cart) {
        if (cart == null || cart.isEmpty()) {
            throw new EmptyCartException("There is nothing to place order!");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null!");
        }

        double total = cart.stream().mapToDouble(OrderItem::getSubtotal).sum();
        double discountRate = discountService.applyDiscount(total);
        double finalAmount = total - (total * discountRate / 100);

        Payment payment = PaymentFactory.getPaymentMethod(mode);
        if (payment == null) {
            throw new IllegalArgumentException("Something went wrong while paying!");
        }
        payment.pay(finalAmount);

        Order order = new Order(0, cart, customer, total, discountRate, mode, finalAmount);

        DeliveryPartner freePartner = deliveryPartnerService.getFreeDeliveryPartner();

        if (freePartner != null) {
            order.setDeliveryPartner(freePartner);
            order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
            deliveryPartnerService.changeDeliveryPartnerStatus(freePartner, DeliveryPartnerStatus.BUSY);
            orderRepository.addOrder(order);
            System.out.println("Delivery partner assigned: " + freePartner.getName());
        } else {
            // All active partners are busy — persist as QUEUED in DB
            order.setOrderStatus(OrderStatus.QUEUED);
            orderRepository.addOrder(order);
            long queuedCount = orderRepository.getOrdersByStatus(OrderStatus.QUEUED).size();
            System.out.println("All delivery partners are busy. Order queued (position: " + queuedCount + ")");
        }

        return order;
    }

    /**
     * Delivery partner marks their current order as DELIVERED.
     * After delivery:
     * - Partner becomes ACTIVE again.
     * - If there are QUEUED orders in the DB, the oldest one is auto-assigned.
     */
    public void deliverOrder(Order order, DeliveryPartner partner) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (order.getOrderStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException(
                    "Only OUT_FOR_DELIVERY orders can be marked delivered. Current status: "
                            + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        orderRepository.updateOrderStatus(order);
        System.out.println("Order #" + order.getId() + " delivered.");

        // Partner is now free, this will trigger assigning the next queued order if any
        deliveryPartnerService.changeDeliveryPartnerStatus(partner, DeliveryPartnerStatus.ACTIVE);
    }

    /**
     * Called when a delivery partner becomes ACTIVE.
     * Assigns the oldest QUEUED order from DB if any exists.
     */
    public void assignQueuedOrderIfAny(DeliveryPartner partner) {
        if (partner.getStatus() != DeliveryPartnerStatus.ACTIVE) {
            return;
        }

        List<Order> queuedOrders = orderRepository.getOrdersByStatus(OrderStatus.QUEUED);
        if (!queuedOrders.isEmpty()) {
            // Pick the oldest queued order (last element, because query returns DESC order)
            Order nextOrder = queuedOrders.get(queuedOrders.size() - 1);
            nextOrder.setDeliveryPartner(partner);
            nextOrder.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
            orderRepository.updateOrderStatus(nextOrder);
            deliveryPartnerService.changeDeliveryPartnerStatus(partner, DeliveryPartnerStatus.BUSY);
            System.out.println("Auto-assigned queued order #" + nextOrder.getId()
                    + " to " + partner.getName());
        }
    }

    public Order getOrderById(int orderId) {
        return orderRepository.getAllOrders()
                .stream()
                .filter(o -> o.getId() == orderId)
                .findFirst()
                .orElse(null);
    }

    public List<Order> getOrdersByCustomer(User customer) {
        return orderRepository.getAllOrdersByCustomerId(customer.getId());
    }

    public Map<String, Object> orderStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Order> orders = orderRepository.getAllOrders();

        long totalOrders = orders.size();
        long delivered = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED).count();
        long queued = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.QUEUED).count();
        long outForDelivery = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.OUT_FOR_DELIVERY).count();
        double totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getFinalAmount)
                .sum();

        stats.put("Total Orders", totalOrders);
        stats.put("Delivered", delivered);
        stats.put("Out For Delivery", outForDelivery);
        stats.put("Queued", queued);
        stats.put("Total Revenue (Delivered)", totalRevenue);
        return stats;
    }
}