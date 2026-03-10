package com.foodapp.seeder;

import com.foodapp.model.Admin;
import com.foodapp.model.User;
import com.foodapp.service.AdminService;
import com.foodapp.service.AuthService;
import com.foodapp.utils.IdGenerator;

public class AdminSeeder {
    private AdminSeeder() {

    }

    public static void seed(AuthService authService) {
        int adminId = IdGenerator.getNextUserID();
        String name = "Admin";
        String email = "admin@food.com";
        String password = "Admin123";
        User admin = new Admin(adminId, name, email, password);
        authService.register(admin);
    }
}
