package com.foodapp.model;

public class UPIPayment implements Payment {

    @Override
    public void pay(double amount) {
        System.out.println("Payment done using UPI\n" +
                "Amount: " + amount);
    }
}
