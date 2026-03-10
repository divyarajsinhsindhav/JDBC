package com.foodapp.service;

import com.foodapp.exception.EmptyCartException;
import com.foodapp.exception.EmptyOrderException;
import com.foodapp.model.*;
import com.foodapp.repository.InMemoryCartRepository;
import com.foodapp.repository.InMemoryOrderQueue;
import com.foodapp.repository.InMemoryOrderRepository;
import com.foodapp.utils.IdGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    private final DeliveryPartnerService deliveryPartnerService;
    private final InMemoryOrderRepository inMemoryOrderRepository;
    private final InMemoryCartRepository inMemoryCartRepository;
    private final DiscountService discountService;
    private final InMemoryOrderQueue orderQueue;

    public OrderService(DeliveryPartnerService deliveryPartnerService,
            InMemoryOrderRepository inMemoryOrderRepository,
            InMemoryCartRepository inMemoryCartRepository,
            DiscountService discountService,
            InMemoryOrderQueue orderQueue) {
        this.deliveryPartnerService = deliveryPartnerService;
        this.inMemoryOrderRepository = inMemoryOrderRepository;
        this.inMemoryCartRepository = inMemoryCartRepository;
        this.discountService = discountService;
        this.orderQueue = orderQueue;
    }

    public List<Order> getOrders() {
        return inMemoryOrderRepository.getAllOrders();
    }

    /**
     * Places an order and immediately tries to assign a free delivery partner.
     * Logic:
     * - If all partners are INACTIVE → Customer unable to place order.
     * - If a free ACTIVE partner exists → assign randomly, status =
     * OUT_FOR_DELIVERY, partner = BUSY.
     * - If all ACTIVE partners are BUSY → status = QUEUED, order added to queue.
     */
    public Order placeOrder(User customer, PaymentMode mode, List<OrderItem> cart) {
        if (cart == null || cart.isEmpty()) {
            throw new EmptyCartException("There is nothing to place order!");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null!");
        }

        int id = IdGenerator.getNextOrderID();
        double total = cart.stream().mapToDouble(OrderItem::getPrice).sum();
        double discountRate = discountService.applyFlatDiscount(total);
        double finalAmount = total - (total * discountRate / 100);

        Payment payment = PaymentFactory.getPaymentMethod(mode);
        if (payment == null) {
            throw new IllegalArgumentException("Something went wrong while paying!");
        }
        payment.pay(finalAmount);

        Order order = new Order(id, cart, customer, total, discountRate, mode, finalAmount);
        inMemoryOrderRepository.addOrder(order);

        // Try to assign a free (ACTIVE, not BUSY) delivery partner
        DeliveryPartner freePartner = deliveryPartnerService.getFreeDeliveryPartner();

        if (freePartner != null) {
            // Assign immediately
            order.setDeliveryPartner(freePartner);
            order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
            freePartner.setStatus(DeliveryPartnerStatus.BUSY);
            System.out.println("Delivery partner assigned: " + freePartner.getName());
        } else {
            // All active partners are busy — put in queue
            order.setOrderStatus(OrderStatus.QUEUED);
            orderQueue.put(order);
            System.out.println("All delivery partners are busy. Order added to queue (position: "
                    + orderQueue.getQueue().size() + ")");
        }

        return order;
    }

    /**
     * Delivery partner marks their current order as DELIVERED.
     * After delivery:
     * - Partner becomes ACTIVE again.
     * - If there are queued orders, the next one is automatically assigned to this
     * partner.
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

        // Partner is now free
        partner.setStatus(DeliveryPartnerStatus.ACTIVE);
        System.out.println("Order #" + order.getId() + " delivered.");

        // Auto-assign next queued order if any
        Order nextOrder = orderQueue.getNext();
        if (nextOrder != null) {
            nextOrder.setDeliveryPartner(partner);
            nextOrder.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
            partner.setStatus(DeliveryPartnerStatus.BUSY);
            System.out.println("Auto-assigned queued order #" + nextOrder.getId()
                    + " to " + partner.getName());
        }
    }

    public Order getOrderById(int orderId) {
        return inMemoryOrderRepository.getAllOrders()
                .stream()
                .filter(o -> o.getId() == orderId)
                .findFirst()
                .orElse(null);
    }

    public List<Order> getOrdersByCustomer(User customer) {
        int customerId = customer.getId();
        return inMemoryOrderRepository.getAllOrders()
                .stream()
                .filter(order -> order.getCustomer().getId() == customerId)
                .toList();
    }

    public Map<String, Object> orderStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Order> orders = inMemoryOrderRepository.getAllOrders();

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
