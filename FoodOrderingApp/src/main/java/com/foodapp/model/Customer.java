package com.foodapp.model;

public class Customer extends User {

    private String phone;
    private String address;

    public Customer(int id, String name, String email, String password,
                    String phone, String address) {

        super(id, name, email, password, UserType.CUSTOMER);

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be null or empty");
        }

        this.phone = phone;
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getEmail() {
        return super.getEmail();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public UserType getRole() {
        return super.getRole();
    }


    @Override
    public String toString() {
        return String.format(
                "+----+----------------+----------------------+------------+\n" +
                        "| ID | Name           | Email                | Phone      |\n" +
                        "+----+----------------+----------------------+------------+\n" +
                        "| %-2d | %-14s | %-20s | %-10s |\n" +
                        "+----+----------------+----------------------+------------+",
                id, name, email, phone
        );
    }
}