package com.foodapp.repository;

import com.foodapp.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {
    private Map<Integer, User> users = new HashMap<>();


    @Override
    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void removeUser(User user) {
        users.remove(user.getId());
    }

    @Override
    public List<Customer> getCustomers() {
        return users.values().stream()
                .filter(user -> user instanceof Customer)
                .map(user -> (Customer) user)
                .toList();
    }

    @Override
    public List<User> getUsers() {
        return users.values().stream().toList();
    }

    @Override
    public List<Admin> getAdmins() {
        return users.values().stream()
                .filter(user -> user instanceof Admin)
                .map(user -> (Admin) user)
                .toList();
    }

    @Override
    public List<DeliveryPartner> getDeliveryPartners() {
        return users.values()
                .stream()
                .filter(user -> user instanceof DeliveryPartner)
                .map(user -> (DeliveryPartner) user)
                .toList();
    }

    @Override
    public User getUserById(int id) {
        User user = users.get(id);
        if (user == null) {
            return null;
        }
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        return users.values()
                .stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Customer getCustomerById(int id) {
        return users.values()
                .stream()
                .filter(user -> user.getRole().equals(UserType.CUSTOMER) && user.getId() == id)
                .findFirst()
                .map(user -> (Customer) user)
                .orElse(null);

    }

    @Override
    public DeliveryPartner getDeliveryPartnerById(int id) {
        return users.values()
                .stream()
                .filter(user -> user.getRole().equals(UserType.DELIVERY_PARTNER) && user.getId() == id)
                .findFirst()
                .map(user -> (DeliveryPartner) user)
                .orElse(null);
    }

    @Override
    public Admin getAdminById(int id) {
        return users.values()
                .stream()
                .filter(user -> user.getRole().equals(UserType.ADMIN) && user.getId() == id)
                .findFirst()
                .map(user -> (Admin) user)
                .orElse(null);
    }
}
