package com.foodapp.service;

import com.foodapp.model.*;
import com.foodapp.repository.UserRepository;

public class AdminService {
    private UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Admin getAdminByEmail(String email) {
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is empty");
        }
        return userRepository.getAdmins()
                .stream()
                .filter(admin -> admin.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

}
