package com.foodapp.service;

import com.foodapp.model.Customer;
import com.foodapp.model.User;
import com.foodapp.repository.UserRepository;

public class CustomerService {
    private UserRepository userRepository;

    public CustomerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findCustomerById(int id) {
        return userRepository.getCustomerById(id);
    }

    public User findCustomerByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }
}
