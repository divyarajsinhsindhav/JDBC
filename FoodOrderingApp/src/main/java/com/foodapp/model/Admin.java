package com.foodapp.model;

public class Admin extends User {

    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, UserType.ADMIN);
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
                "+------+----------------+--------------------------+%n" +
                        "| ID   | Name           | Email                    |%n" +
                        "+------+----------------+--------------------------+%n" +
                        "| %-4d | %-14s | %-24s |%n" +
                        "+------+----------------+--------------------------+",
                id, name, email
        );
    }
}