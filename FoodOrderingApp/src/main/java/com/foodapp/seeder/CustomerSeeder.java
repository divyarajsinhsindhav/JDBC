package com.foodapp.seeder;

import com.foodapp.model.Customer;
import com.foodapp.service.AuthService;
import com.foodapp.utils.IdGenerator;

public class CustomerSeeder {
    private CustomerSeeder() {

    }

    public static void seed(AuthService authService) {
        authService.register(new Customer(
                IdGenerator.getNextUserID(),
                "Rahul Patel",
                "rahul@food.com",
                "Password123",
                "9876500001",
                "Rajkot"
        ));

        authService.register(new Customer(
                IdGenerator.getNextUserID(),
                "Priya Shah",
                "priya@food.com",
                "Password123",
                "9876500002",
                "Ahmedabad"
        ));

        authService.register(new Customer(
                IdGenerator.getNextUserID(),
                "Kunal Mehta",
                "kunal@food.com",
                "Password123",
                "9876500003",
                "Surat"
        ));
    }
}
