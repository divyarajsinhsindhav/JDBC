package com.foodapp.repository;

import com.foodapp.model.PaymentRecord;

import java.util.List;

public interface PaymentRepository {

    /**
     * Insert a new payment row and return the saved record with the DB-generated ID.
     */
    PaymentRecord save(PaymentRecord payment);

    /**
     * Find a payment by its primary key.
     */
    PaymentRecord findById(int id);

    /**
     * Return all payments made by a specific customer.
     */
    List<PaymentRecord> findByCustomerId(int customerId);

    /**
     * Return every payment ever recorded.
     */
    List<PaymentRecord> findAll();
}
