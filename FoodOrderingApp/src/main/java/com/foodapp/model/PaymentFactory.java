package com.foodapp.model;

public class PaymentFactory {
    public static Payment getPaymentMethod(PaymentMode mode) {

        return switch (mode) {
            case UPI -> new UPIPayment();
            case CASH -> new CashPayment();
            default -> null;
        };

    }
}
