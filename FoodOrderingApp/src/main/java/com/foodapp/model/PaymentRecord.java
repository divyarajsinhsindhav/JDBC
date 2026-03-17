package com.foodapp.model;

import java.time.LocalDateTime;

public class PaymentRecord {

    private int id;
    private PaymentMode mode;
    private int customerId;
    private double amount;
    private LocalDateTime createdAt;

    public PaymentRecord() {}

    public PaymentRecord(int id, PaymentMode mode, int customerId,
                         double amount, LocalDateTime createdAt) {
        this.id = id;
        this.mode = mode;
        this.customerId = customerId;
        this.amount = amount;
        this.createdAt = createdAt;
    }



    public int getId() {
        return id;
    }

    public PaymentMode getMode() {
        return mode;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }



    public void setId(int id) {
        this.id = id;
    }

    public void setMode(PaymentMode mode) {
        this.mode = mode;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
