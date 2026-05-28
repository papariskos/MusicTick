package com.musictick.manager;

import models.Notification;

public class UserAlertScreen {
    public static void display(Notification alert) {
        System.out.println("UserAlertScreen: display() called for Customer Alert: " + alert.getTitle() + " | " + alert.getMessage());
    }
}
