package com.musictick.controller;

import com.musictick.DBConfig;
import com.musictick.Main;
import com.musictick.manager.CancellationPolicyDB;
import com.musictick.manager.PaymentSystem;
import com.musictick.manager.ReservationList;
import com.musictick.manager.SeatList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManageCancellationController {
    public static String currentReservationData;

    public static void validateReservationData(String reservationData) {
        currentReservationData = reservationData;
        String status = ReservationList.checkReservationDetails(reservationData);
        if ("invalidReservation".equals(status)) {
            displayErrorScreen("Invalid Reservation", "The reservation details provided are incorrect.");
        } else {
            String eligibility = CancellationPolicyDB.checkCancellationPolicy(reservationData);
            if ("cancellationNotAllowed".equals(eligibility)) {
                displayNoCancellationScreen();
            } else {
                displayCardDetailsScreen();
            }
        }
    }

    public static void validateCardDetails(String cardData) {
        String status = PaymentSystem.validateCard(cardData);
        if ("invalidCard".equals(status)) {
            displayErrorScreen("Invalid Card", "The card details provided are incorrect or the refund was declined.");
        } else {
            // 1. cancelReservation – update ticket to CANCELLED in DB
            cancelTicketInDB(ReservationScreenController.selectedTicketLine);
            // 2. SeatList.updateSeatAvailability (mock)
            SeatList.updateSeatAvailability();
            // 3. refundPayment
            PaymentSystem.refundPayment(cardData);
            displayConfirmationScreen();
        }
    }

    /** Updates the ticket status to CANCELLED in the database. */
    private static void cancelTicketInDB(String ticketLine) {
        if (ticketLine == null || ticketLine.isBlank()) return;
        System.out.println("ManageCancellationController: cancelTicketInDB() -> target: " + ticketLine);
        
        // Extract ticketId from ticketLine to perform database update
        int ticketId = -1;
        try {
            String clean = ticketLine;
            if (clean.startsWith("[userId=")) {
                int end = clean.indexOf(']');
                if (end > 0) clean = clean.substring(end + 1).trim();
            }
            if (clean.startsWith("Ticket #")) {
                int dash = clean.indexOf('-');
                if (dash > 0) {
                    String idStr = clean.substring("Ticket #".length(), dash).trim();
                    ticketId = Integer.parseInt(idStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ticketId != -1) {
            String sql = "UPDATE tickets SET status = 'CANCELLED' WHERE ticket_id = ?";
            try (java.sql.Connection conn = DBConfig.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, ticketId);
                int rows = ps.executeUpdate();
                System.out.println("ManageCancellationController: Database ticket #" + ticketId + " updated to CANCELLED in DB. Rows updated: " + rows);
                com.musictick.manager.WaitlistManager.handleTicketCancellation(ticketId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void displayErrorScreen(String title, String message) {
        ErrorController.errorMessage = title + ": " + message;
        openPage("/error_screen.fxml", "MusicTick - Error");
    }

    private static void displayNoCancellationScreen() {
        openPage("/no_cancellation_screen.fxml", "MusicTick - Cancellation Not Allowed");
    }

    private static void displayCardDetailsScreen() {
        openPage("/card_details_screen.fxml", "MusicTick - Card Details");
    }

    private static void displayConfirmationScreen() {
        openPage("/cancellation_confirmation_screen.fxml", "MusicTick - Cancellation Confirmed");
    }

    private static void openPage(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(ManageCancellationController.class.getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
