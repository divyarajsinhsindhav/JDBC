package com.foodapp.service;

import com.foodapp.model.Discount;
import com.foodapp.repository.DiscountRepository;

import java.util.List;

public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN — Add new discount
    // ─────────────────────────────────────────────────────────────
    public Discount addDiscount(Discount discount) {
        return discountRepository.addDiscount(discount);
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN — Update discount details
    // ─────────────────────────────────────────────────────────────
    public Discount updateDiscount(Discount discount) {
        return discountRepository.updateDiscount(discount);
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN — Toggle active / inactive
    // ─────────────────────────────────────────────────────────────
    public Discount toggleActive(int id) {
        Discount discount = discountRepository.findById(id);
        if (discount == null) {
            throw new RuntimeException("Discount not found with id: " + id);
        }
        return discountRepository.toggleActive(id);
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN — Soft-delete a discount
    // ─────────────────────────────────────────────────────────────
    public void deleteDiscount(int id) {
        Discount discount = discountRepository.findById(id);
        if (discount == null) {
            throw new RuntimeException("Discount not found with id: " + id);
        }
        discountRepository.deleteDiscount(id);
    }

    // ─────────────────────────────────────────────────────────────
    //  Query helpers
    // ─────────────────────────────────────────────────────────────
    public Discount findById(int id) {
        return discountRepository.findById(id);
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.getAllDiscounts();
    }

    // ─────────────────────────────────────────────────────────────
    //  ORDER — Apply active discount to an order amount
    // ─────────────────────────────────────────────────────────────
    /**
     * Returns the discount rate (%) to apply on the given order amount.
     * Returns 0 if no active discount exists or is found.
     */
    public double applyDiscount(double orderAmount) {
        List<Discount> activeDiscounts = discountRepository.getActiveDiscounts();
        if (activeDiscounts == null || activeDiscounts.isEmpty()) {
            return 0;
        }

        Discount mostProfitableForAdmin = null;
        double minDiscountAmount = Double.MAX_VALUE;

        for (Discount d : activeDiscounts) {
            if (orderAmount >= d.getDiscountOn()) {
                double discountAmount = orderAmount * (d.getDiscountRate() / 100.0);
                if (discountAmount < minDiscountAmount) {
                    minDiscountAmount = discountAmount;
                    mostProfitableForAdmin = d;
                }
            }
        }

        return mostProfitableForAdmin != null ? mostProfitableForAdmin.getDiscountRate() : 0;
    }

    /**
     * Convenience: returns the active Discounts list.
     */
    public List<Discount> getActiveDiscounts() {
        return discountRepository.getActiveDiscounts();
    }
}
