package com.musictick.manager;

import com.musictick.dao.TicketDAO;
import com.musictick.dao.UserDAO;
import com.musictick.model.User;

public class TransferTicketManager {
    private final TicketDAO ticketDAO = new TicketDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationManager notificationManager = new NotificationManager();

    public TransferResult transferTicket(int ticketId, int currentUserId, String recipientEmail) {
        try {
            if (!ticketDAO.checkTicketValidity(ticketId, currentUserId)) {
                return TransferResult.invalidTicket();
            }

            User recipient = userDAO.findRecipient(recipientEmail);
            if (recipient == null) {
                return TransferResult.userNotFound();
            }
            if (recipient.getUserId() == currentUserId) {
                return TransferResult.failure("Δεν μπορείς να μεταβιβάσεις εισιτήριο στον εαυτό σου.");
            }

            int newTicketId = ticketDAO.transferTicket(ticketId, currentUserId, recipient.getUserId());
            if (newTicketId <= 0) return TransferResult.invalidTicket();

            notificationManager.createTransferAlert(recipient.getUserId(), newTicketId);
            return TransferResult.success(newTicketId);
        } catch (Exception e) {
            e.printStackTrace();
            return TransferResult.failure("Αποτυχία μεταβίβασης. Δοκιμάστε ξανά.");
        }
    }

    public static class TransferResult {
        public final boolean success;
        public final String message;
        public final int newTicketId;

        private TransferResult(boolean success, String message, int newTicketId) {
            this.success = success;
            this.message = message;
            this.newTicketId = newTicketId;
        }

        public static TransferResult success(int newTicketId) { return new TransferResult(true, "Η μεταβίβαση ολοκληρώθηκε.", newTicketId); }
        public static TransferResult invalidTicket() { return new TransferResult(false, "Το εισιτήριο δεν είναι έγκυρο ή δεν σου ανήκει.", -1); }
        public static TransferResult userNotFound() { return new TransferResult(false, "Ο παραλήπτης δεν βρέθηκε.", -1); }
        public static TransferResult failure(String msg) { return new TransferResult(false, msg, -1); }
    }
}
