package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.DBConfig;
import com.musictick.dao.AlertDAO;
import com.musictick.manager.ReservationList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportProblemController {

    @FXML private TextField ticketIdField;
    @FXML private TextArea reasonArea;
    @FXML private Label statusLabel;


    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }
    @FXML
    private void handleSubmitReport() {
        String ticketIdStr = ticketIdField.getText().trim();
        String reason = reasonArea.getText().trim();

        if (ticketIdStr.isEmpty() || reason.isEmpty()) {
            setStatus("Παρακαλώ συμπληρώστε όλα τα πεδία.");
            return;
        }

        // Clean input: remove "ticket", "#", and extra spaces
        String cleanedIdStr = ticketIdStr.toLowerCase()
                .replace("ticket", "")
                .replace("#", "")
                .replace(" ", "")
                .trim();

        int ticketId = -1;
        try {
            ticketId = Integer.parseInt(cleanedIdStr);
        } catch (NumberFormatException e) {
            // If they type a mock ticket like "FAIL", trigger invalid ticket failure path!
            displayFailureScreen("Σφάλμα Αναφοράς: Ο κωδικός εισιτηρίου δεν είναι έγκυρος.");
            return;
        }

        // 1. Control: Έλεγχος αν υπάρχει το εισιτήριο (validate with DB)
        boolean ticketExists = checkTicketExists(ticketId);

        if (!ticketExists) {
            // [Δεν υπάρχει εισιτήριο] -> displayFailureScreen
            System.out.println("ReportProblemController: [Δεν υπάρχει εισιτήριο] path matched");
            displayFailureScreen("Σφάλμα: Το εισιτήριο #" + ticketId + " δεν βρέθηκε στη λίστα κρατήσεων.");
        } else {
            // [Υπάρχει εισιτήριο] -> Αποθήκευση στην λίστα αναφορών & Εμφάνιση οθόνης επιτυχίας & Δημιουργία alert
            System.out.println("ReportProblemController: [Υπάρχει εισιτήριο] path matched");
            
            // A. Determine the organizer dynamically for this ticket
            int organizerId = findOrganizerForTicket(ticketId);

            // B. Save report strictly to DB reports table
            String sqlRep = "INSERT INTO reports (ticket_id, user_id, organizer_id, description, status) VALUES (?, ?, ?, ?, 'OPEN')";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlRep)) {
                ps.setInt(1, ticketId);
                ps.setInt(2, Session.getCurrentUserId());
                ps.setInt(3, organizerId);
                ps.setString(4, reason);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // C. Δημιουργία alert αποστολή (send alert to Organizer in DB)
            try {
                new AlertDAO().saveAlert(organizerId, "Νέα Αναφορά Προβλήματος ⚠️", 
                    "Υποβλήθηκε αναφορά για το εισιτήριο #" + ticketId + ": " + reason, "ALERT");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            displaySuccessScreen("Η αναφορά προβλήματος για το εισιτήριο #" + ticketId + " υποβλήθηκε με επιτυχία!");
        }
    }

    private boolean checkTicketExists(int ticketId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT COUNT(*) AS total FROM tickets WHERE ticket_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, ticketId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt("total") > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int findOrganizerForTicket(int ticketId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT c.organizer_id FROM tickets t JOIN concerts c ON t.concert_id = c.concert_id WHERE t.ticket_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, ticketId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("organizer_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2; // default fallback organizer ID if not found
    }

    private void displaySuccessScreen(String msg) {
        SuccessController.titleText = "Υποβολή Επιτυχής! ⚠️";
        SuccessController.descText = msg;
        SuccessController.buttonText = "ΕΠΙΣΤΡΟΦΗ 🏠";
        SuccessController.nextFxml = "/user_home.fxml";
        SuccessController.nextTitle = "MusicTick - Home";
        SuccessController.nextWidth = 900;
        SuccessController.nextHeight = 600;

        openPage("/success.fxml", "MusicTick - Επιτυχία");
    }

    private void displayFailureScreen(String msg) {
        BookingFailureController.failureMessage = msg;
        BookingFailureController.backTargetFxml = "/report_problem_screen.fxml";
        BookingFailureController.backTargetTitle = "MusicTick - Report Problem";

        openPage("/booking_failure.fxml", "MusicTick - Σφάλμα");
    }

    @FXML
    private void goBack() {
        openPage("/user_home.fxml", "MusicTick - Home");
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private void openPage(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
