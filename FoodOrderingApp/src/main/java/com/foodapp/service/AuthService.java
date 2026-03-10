package com.foodapp.service;

import com.foodapp.model.User;
import com.foodapp.repository.UserRepository;

public class AuthService {

    private UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        userRepository.addUser(user);
    }

    public User login(String email, String password) {
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Incorrect password");
        }

        return user;
    }
}
