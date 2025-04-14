package org.example.java1.utils;

import org.example.java1.entity.User;

public class Session {
    private static Session instance;
    private User loggedInUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUser(User user) {
        this.loggedInUser = user;
    }

    public User getUser() {
        return loggedInUser;
    }

    public void clearSession() {
        loggedInUser = null;
    }

    public boolean isAdmin() {
        return loggedInUser != null && loggedInUser.getRoles().contains("ROLE_ADMIN");
    }

    public boolean isUser() {
        return loggedInUser != null && loggedInUser.getRoles().contains("ROLE_USER");
    }
}
