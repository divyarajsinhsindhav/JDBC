package com.foodapp.controller;

import com.foodapp.model.*;
import com.foodapp.repository.UserRepository;
import com.foodapp.service.AuthService;
import com.foodapp.utils.IdGenerator;
import com.foodapp.utils.InputValidation;
import com.foodapp.utils.SessionManager;

import java.util.Scanner;

public class AuthController {
    private UserRepository userRepository;
    private AuthService authService;
    private SessionManager sessionManager;
    private Scanner scanner = new Scanner(System.in);

    public AuthController(UserRepository userRepository,  AuthService authService, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    public User registerUser(UserType role) {

        String name = InputValidation.readValidName(scanner, "Enter your name: ");

        String email;
        User user;

        while(true) {
            email = InputValidation.readValidEmail(scanner, "Enter your email: ");
            user = userRepository.getUserByEmail(email);
            if (user != null) {
                throw new IllegalArgumentException("User already exists");
            }
            break;
        }

        String password = InputValidation.readValidPassword(scanner, "Enter password: ");
        int id = IdGenerator.getNextUserID();

        try {
            user = switch (role) {
                case ADMIN -> registerAdmin(id, name, email, password);
                case CUSTOMER -> registerCustomer(id, name, email, password);
                case DELIVERY_PARTNER -> registerDeliveryPartner(id, name,email, password);
            };
        } catch (Exception e) {
            System.out.println("Error in registering user");
        }
        authService.register(user);
        return user;
    }

    private User registerCustomer(int id, String name, String email, String password) {
        String phone = InputValidation.readValidPhone(scanner, "Enter your phone number: ");
        String address = InputValidation.readNonEmptyString(scanner, "Enter your address: ");

        User customer = null;
        try {
            customer = new Customer(id, name, email, password, phone, address);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return customer;
    }

    private User registerAdmin(int id, String name, String email, String password) {
        User admin = null;
        try {
            admin = new Admin(id, name, email, password);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return admin;
    }

    private User registerDeliveryPartner(int id, String name, String email, String password) {
        String phone = InputValidation.readValidPhone(scanner, "Enter your phone number: ");
        User deliveryPartner = null;
        try {
            deliveryPartner = new DeliveryPartner(id, name, email, password, phone);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return deliveryPartner;
    }

    public User loginUser() {
        String email = InputValidation.readValidEmail(scanner, "Enter your email: ");
        String password = InputValidation.readValidPassword(scanner, "Enter your password: ");
        User user = null;
        try {
            user = authService.login(email, password);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        sessionManager.login(user);
        return user;
    }
}
