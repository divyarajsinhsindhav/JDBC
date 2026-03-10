package com.foodapp.seeder;

import com.foodapp.model.DeliveryPartner;
import com.foodapp.service.AuthService;
import com.foodapp.utils.IdGenerator;

public class DeliveryPartnerSeeder {

    private DeliveryPartnerSeeder() {
        // prevent object creation
    }

    public static void seed(AuthService authService) {

        authService.register(new DeliveryPartner(
                IdGenerator.getNextUserID(),
                "Ravi Patel",
                "ravi@food.com",
                "Password123",
                "9876543210"
        ));

        authService.register(new DeliveryPartner(
                IdGenerator.getNextUserID(),
                "Amit Shah",
                "amit@food.com",
                "Password123",
                "9876543211"
        ));

        authService.register(new DeliveryPartner(
                IdGenerator.getNextUserID(),
                "Dhruv Mehta",
                "dhruv@food.com",
                "Password123",
                "9876543212"
        ));
    }
}