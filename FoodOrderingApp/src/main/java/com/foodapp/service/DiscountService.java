package com.foodapp.service;

import com.foodapp.model.FlatDiscount;

public class DiscountService {
    public double applyFlatDiscount(double amount) {
        if (amount > FlatDiscount.getInstance().getFlatDiscountOn()) {
            return FlatDiscount.getInstance().getDiscount();
        }
        return 0;
    }
}
