package com.foodapp.repository;

import com.foodapp.model.*;

import java.util.List;

public interface UserRepository {
    void addUser(User user);

    void removeUser(User user);

    List<User> getUsers();

    User getUserById(int id);

    User getUserByEmail(String email);

    List<DeliveryPartner> getDeliveryPartners();

    List<Admin> getAdmins();

    List<Customer> getCustomers();

    Customer getCustomerById(int id);

    Admin getAdminById(int id);

    DeliveryPartner getDeliveryPartnerById(int id);

    void updateDeliveryPartnerStatus(int id, DeliveryPartnerStatus status);
}
