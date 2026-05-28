package com.musictick.manager;

import models.Notification;

public class AdminAlertScreen {
    public static void display(Notification alert) {
        System.out.println("AdminAlertScreen: display() called for Admin Alert: " + alert.getTitle() + " | " + alert.getMessage());
    }
}
