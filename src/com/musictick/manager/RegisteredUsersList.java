package com.musictick.manager;

import com.musictick.dao.UserDAO;
import models.User;

import java.sql.SQLException;

public class RegisteredUsersList {
    private static final UserDAO userDAO = new UserDAO();

    public static User findUser(String recipientEmail) {
        System.out.println("RegisteredUsersList: findUser() called for email=" + recipientEmail);
        try {
            User user = userDAO.findRecipient(recipientEmail);
            System.out.println("RegisteredUsersList: returnUser() -> " + (user != null ? "userExists" : "userNotFound"));
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
