package com.foodapp.model;

import java.time.LocalDateTime;

public class Discount {

    private int id;
    private String name;
    private double discountRate;
    private double discountOn;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    private boolean isDeleted;
    private LocalDateTime createdAt;

    public Discount() {}

    public Discount(int id, String name, double discountRate, double discountOn,
                    LocalDateTime startDate, LocalDateTime endDate,
                    boolean isActive, boolean isDeleted, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.discountRate = discountRate;
        this.discountOn = discountOn;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    // ---- Getters ----

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public double getDiscountOn() {
        return discountOn;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ---- Setters ----

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public void setDiscountOn(double discountOn) {
        this.discountOn = discountOn;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
