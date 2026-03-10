package com.foodapp.model;

public class FlatDiscount implements Discount {

    private double discountRate;
    private double flatDiscountOn;

    private FlatDiscount() {}

    private static class SingletonHelper {
        private static final FlatDiscount INSTANCE = new FlatDiscount();
    }

    @Override
    public double getDiscount() {
        return discountRate;
    }

    @Override
    public void setDiscount(double rate) {
        discountRate = rate;
    }

    public double getFlatDiscountOn() {
        return flatDiscountOn;
    }

    public static FlatDiscount getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public void setFlatDiscountOn(double flatDiscountOn) {
        FlatDiscount.getInstance().flatDiscountOn = flatDiscountOn;
    }
}
