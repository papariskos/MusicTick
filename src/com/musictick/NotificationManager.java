package com.musictick.manager;

import com.musictick.dao.AlertDAO;
import com.musictick.model.Alert;

import java.sql.SQLException;
import java.util.List;

public class NotificationManager {
    private final AlertDAO alertDAO = new AlertDAO();

    public List<Alert> checkNewAlerts(int currentUserId) throws SQLException {
        return alertDAO.returnAlerts(currentUserId);
    }

    public void createTransferAlert(int recipientId, int newTicketId) throws SQLException {
        alertDAO.saveAlert(recipientId, "Νέα μεταβίβαση εισιτηρίου", "Σου μεταβιβάστηκε το εισιτήριο #" + newTicketId, "TRANSFER");
    }
}
