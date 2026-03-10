package com.foodapp.model;

import java.util.List;

public class Order {
    private int  id;
    private User customer;
    private List<OrderItem> orderItems;
    private double total;
    private double discountRate;
    private PaymentMode mode;
    private DeliveryPartner deliveryPartner;
    private double finalAmount;
    private OrderStatus orderStatus;

    public Order(int id, List<OrderItem> orderItems, User customer,
                 double total, double discountRate, PaymentMode mode,
                double finalAmount) {
        this.id = id;
        this.orderItems = orderItems;
        this.customer = customer;
        this.total = total;
        this.discountRate = discountRate;
        this.mode = mode;
        this.finalAmount = finalAmount;
        this.orderStatus = OrderStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public User getCustomer() {
        return customer;
    }

    public double getTotal() {
        return total;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public PaymentMode getPaymentMode() {
        return mode;
    }

    public User getDeliveryPartner() {
        return deliveryPartner;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public void setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + customer +
                ", orderItems=" + orderItems +
                ", total=" + total +
                ", discountRate=" + discountRate +
                ", mode=" + mode +
                ", deliveryPartner=" + deliveryPartner +
                ", finalAmount=" + finalAmount +
                '}';
    }
}
