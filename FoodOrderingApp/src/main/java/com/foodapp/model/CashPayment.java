package com.foodapp.model;

public class CashPayment implements Payment{

    @Override
    public void pay(double amount) {
        System.out.println("Payment done using cash\n" +
                "Amount: " + amount);
    }
}
