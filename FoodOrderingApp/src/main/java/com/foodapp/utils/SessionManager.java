package com.foodapp.utils;

import com.foodapp.model.FlatDiscount;
import com.foodapp.model.User;

public class SessionManager {

    private User currentUser;

    private SessionManager() {

    }

    private static class SingletonHelper {
        private static final SessionManager INSTANCE = new SessionManager();
    }

    public void login(User user) {
        currentUser = user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentCustomer() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public static SessionManager getSessionManager() {
        return SingletonHelper.INSTANCE;
    }
}
