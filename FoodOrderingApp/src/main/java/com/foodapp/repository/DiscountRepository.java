package com.foodapp.repository;

import com.foodapp.model.Discount;

import java.util.List;

public interface DiscountRepository {

    /**
     * Insert a new discount into the database.
     * Returns the saved Discount with the generated database ID.
     */
    Discount addDiscount(Discount discount);

    /**
     * Update the name, discount rate, start date, and end date of an existing discount.
     */
    Discount updateDiscount(Discount discount);

    /**
     * Toggle is_active status for a discount (ACTIVE ↔ INACTIVE).
     */
    Discount toggleActive(int id);

    /**
     * Soft-delete a discount (sets is_deleted = true).
     */
    void deleteDiscount(int id);

    /**
     * Find a discount by its primary key.
     */
    Discount findById(int id);

    /**
     * Return all non-deleted discounts.
     */
    List<Discount> getAllDiscounts();

    /**
     * Return all currently active discounts that cover today's date.
     */
    List<Discount> getActiveDiscounts();
}
