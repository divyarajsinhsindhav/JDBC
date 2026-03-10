package com.foodapp.model;

public abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected UserType role;

    public User(int id, String name, String email, String password, UserType role) {
        if (id <= 0) {
            throw new IllegalArgumentException("User id must be greater than 0");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserType getRole() {
        return role;
    }
}