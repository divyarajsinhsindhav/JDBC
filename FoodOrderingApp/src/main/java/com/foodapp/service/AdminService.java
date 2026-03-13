package com.foodapp.service;

import com.foodapp.model.*;
import com.foodapp.repository.UserRepository;

import javax.management.relation.Role;

public class AdminService {
    private UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Admin getAdminByEmail(String email) {
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is empty");
        }
        Admin admin = (Admin)userRepository.getUserByEmail(email);
        if (admin.getRole() ==  UserType.ADMIN) {
            return admin;
        }
        return null;
    }

}
