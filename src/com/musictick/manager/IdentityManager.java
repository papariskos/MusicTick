package com.musictick.manager;

import models.Notification;

public class IdentityManager {
    public static void checkRecipientIdentity(Notification alert) {
        System.out.println("IdentityManager: checkRecipientIdentity() called");
        String recipientRole = RegisteredList.findRecipient(alert.getRecipientId());
        System.out.println("IdentityManager: returnRecipient() -> " + recipientRole);

        if ("CUSTOMER".equals(recipientRole)) {
            UserAlertScreen.display(alert);
        } else if ("ORGANIZER".equals(recipientRole)) {
            OrganizerAlertScreen.display(alert);
        } else if ("ADMIN".equals(recipientRole)) {
            AdminAlertScreen.display(alert);
        }
    }
}
