package com.foodapp.service;

import com.foodapp.model.Customer;
import com.foodapp.model.User;
import com.foodapp.model.UserType;
import com.foodapp.repository.UserRepository;

import javax.management.relation.Role;

public class CustomerService {
    private UserRepository userRepository;

    public CustomerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findCustomerById(int id) {
        return userRepository.getCustomerById(id);
    }

    public User findCustomerByEmail(String email) {
        Customer customer = (Customer) userRepository.getUserByEmail(email);
        if (customer == null) {
            throw new IllegalArgumentException("User with email " + email + " not found");
        }
        if (customer.getRole() == UserType.CUSTOMER) {
            return customer;
        }
        return null;
    }
}
