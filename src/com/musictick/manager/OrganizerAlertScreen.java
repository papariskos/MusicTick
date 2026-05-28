package com.musictick.manager;

import models.Notification;

public class OrganizerAlertScreen {
    public static void display(Notification alert) {
        System.out.println("OrganizerAlertScreen: display() called for Organizer Alert: " + alert.getTitle() + " | " + alert.getMessage());
    }
}
