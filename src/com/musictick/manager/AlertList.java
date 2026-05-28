package com.musictick.manager;

import com.musictick.dao.AlertDAO;
import models.Notification;
import java.sql.SQLException;
import java.util.List;

public class AlertList {
    private static final AlertDAO alertDAO = new AlertDAO();

    public static List<Notification> checkNewAlerts(int currentUserId) throws SQLException {
        System.out.println("AlertList: checkNewAlerts() called");
        List<Notification> alerts = alertDAO.returnAlerts(currentUserId);
        System.out.println("AlertList: returnAlerts() returned list of size=" + alerts.size());
        return alerts;
    }

    public static void saveAlert(Alert alert) {
        System.out.println("AlertList: saveAlert() called for alert: " + alert.getTitle());
    }
}
