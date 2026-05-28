package com.musictick.manager;

import com.musictick.dao.TicketDAO;
import com.musictick.dao.UserDAO;
import models.Ticket;
import models.User;
import models.enums.TicketStatus;

public class TransferTicketManager {
    private final TicketDAO ticketDAO = new TicketDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationManager notificationManager = new NotificationManager();

    // transferTicket(ticketId) matching the sequence diagram:
    // TicketsScreen -> TransferTicketManager : transferTicket(ticketId)
    public String transferTicket(int ticketId) {
        System.out.println("TransferTicketManager: transferTicket() called for ticketId=" + ticketId);
        
        // TransferTicketManager -> ReservationList : checkTicketValidity(ticketId)
        String validity = ReservationList.checkTicketValidity(ticketId);
        
        if ("validTicket".equals(validity)) {
            System.out.println("TransferTicketManager: [validTicket] path matched");
            // TransferTicketManager -> TransferScreen : display()
            TransferScreen.display();
        } else {
            System.out.println("TransferTicketManager: [invalidTicket] path matched");
            // TransferTicketManager -> ErrorScreen : display()
            ErrorScreen.display();
        }
        return validity;
    }

    public TransferResult transferTicket(int ticketId, int currentUserId, String recipientEmail) {
        System.out.println("TransferTicketManager: transferTicket() with recipientEmail called");
        try {
            // First run the validation check from diagram
            String validity = transferTicket(ticketId);
            if (!"validTicket".equals(validity)) {
                return TransferResult.invalidTicket();
            }

            // TransferScreen -> TransferTicketManager : submitRecipientData(recipientEmail)
            System.out.println("TransferTicketManager: submitRecipientData() invoked with email=" + recipientEmail);
            
            // TransferTicketManager -> RegisteredUsersList : findUser(recipientEmail)
            User recipient = RegisteredUsersList.findUser(recipientEmail);
            
            if (recipient == null) {
                System.out.println("TransferTicketManager: [userNotFound] path matched");
                // TransferTicketManager -> FailureScreen : display()
                FailureScreen.display();
                return TransferResult.userNotFound();
            }
            
            if (recipient.getUserId() == currentUserId) {
                return TransferResult.failure("Δεν μπορείς να μεταβιβάσεις εισιτήριο στον εαυτό σου.");
            }

            System.out.println("TransferTicketManager: [userExists] path matched");
            
            // TransferTicketManager -> ReservationList : deleteTicket(ticketId)
            ReservationList.deleteTicket(ticketId);

            // TransferTicketManager -> Ticket : <<create>> create(newTicketData)
            Ticket newTicket = new Ticket(
                ticketId, 
                1, 
                recipient.getUserId(), 
                1, 
                1, 
                TicketStatus.ACTIVE, 
                "QR-MOCK-" + ticketId, 
                java.time.LocalDateTime.now()
            );
            System.out.println("TransferTicketManager: <<create>> create(newTicketData) -> Ticket #" + ticketId + " created");
            System.out.println("TransferTicketManager: newTicketDetails()");

            // TransferTicketManager -> ReservationList : saveTicket(newTicket)
            ReservationList.saveTicket(newTicket);

            // Execute the persistent/DB and file-based transfer
            int newTicketId = ticketDAO.transferTicket(ticketId, currentUserId, recipient.getUserId());
            if (newTicketId <= 0) return TransferResult.invalidTicket();

            notificationManager.createTransferAlert(recipient.getUserId(), newTicketId);

            // TransferTicketManager -> Alert : <<create>> create(alertDetails)
            Alert alert = new Alert("Μεταβίβαση Εισιτηρίου", "Σας μεταβιβάστηκε το εισιτήριο #" + ticketId);
            
            // TransferTicketManager -> AlertList : saveAlert(alert)
            AlertList.saveAlert(alert);

            // TransferTicketManager -> ConfirmationScreen : display()
            ConfirmationScreen.display();

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
