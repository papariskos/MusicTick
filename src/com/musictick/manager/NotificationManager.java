package com.musictick.manager;

import com.musictick.dao.AlertDAO;
import models.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationManager {
    private final AlertDAO alertDAO = new AlertDAO();

    // init() matching the sequence diagram:
    // System -> NotificationManager : init()
    public void init(int currentUserId) {
        System.out.println("NotificationManager: init() called");
        try {
            // NotificationManager -> AlertList : checkNewAlerts()
            List<Notification> alerts = AlertList.checkNewAlerts(currentUserId);
            
            if (alerts != null && !alerts.isEmpty()) {
                System.out.println("NotificationManager: [alertsFound] path matched");
                for (Notification alert : alerts) {
                    // NotificationManager -> IdentityManager : checkRecipientIdentity(alert)
                    IdentityManager.checkRecipientIdentity(alert);
                }
            } else {
                System.out.println("NotificationManager: [noAlertsFound] path matched");
                // NotificationManager -> FailureScreen : display()
                FailureScreen.display();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("NotificationManager: SQLException caught, invoking FailureScreen");
            FailureScreen.display();
        }
    }

    public List<Notification> checkNewAlerts(int currentUserId) throws SQLException {
        return alertDAO.returnAlerts(currentUserId);
    }

    public void createTransferAlert(int recipientId, int newTicketId) throws SQLException {
        alertDAO.saveAlert(recipientId, "Νέα μεταβίβαση εισιτηρίου", "Σου μεταβιβάστηκε το εισιτήριο #" + newTicketId, "TRANSFER");
    }
}
