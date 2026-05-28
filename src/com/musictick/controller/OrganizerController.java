package com.musictick.controller;

import com.musictick.Main;
import com.musictick.DBConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrganizerController {

    // Create Event Fields
    @FXML
    private TextField titleField;
    @FXML
    private TextField descField;
    @FXML
    private TextField dateField;
    @FXML
    private ComboBox<String> venueComboBox;
    @FXML
    private TextField regularPriceField;
    @FXML
    private TextField vipPriceField;

    // Reports Field
    @FXML
    private ListView<String> reportsListView;

    @FXML
    private Label statusLabel;

    private List<String> reports = new ArrayList<>();

    @FXML
    public void initialize() {
        if (venueComboBox != null) {
            loadVenues();
        }
        if (reportsListView != null) {
            loadReports();
        }
    }

    // 1. CREATE EVENT FLOW
    private void loadVenues() {
        List<String> venues = new ArrayList<>();
        venues.add("1 - Athens Arena (Athens)");
        venues.add("2 - Metropolis Hall (Thessaloniki)");
        venues.add("3 - Municipal Theater (Patras)");
        venueComboBox.setItems(FXCollections.observableArrayList(venues));
    }

    @FXML
    private void handleCreateEvent() {
        String title = titleField.getText().trim();
        String desc = descField.getText().trim();
        String dateStr = dateField.getText().trim();
        String selectedVenue = venueComboBox.getValue();
        String regPriceStr = regularPriceField.getText().trim();
        String vipPriceStr = vipPriceField.getText().trim();

        // 1. validateConcertData(concertData) -> [invalidData]
        if (title.isEmpty() || dateStr.isEmpty() || selectedVenue == null || regPriceStr.isEmpty()
                || vipPriceStr.isEmpty()) {
            openFailureScreen("Σφάλμα Επικύρωσης: Συμπληρώστε όλα τα πεδία της συναυλίας (ErrorScreen).",
                    "/create_concert.fxml", "MusicTick - Create Concert");
            return;
        }

        // 2. checkVenueAvailability() -> [venueUnavailable] (patras venue #3 is
        // simulated as unavailable)
        if (selectedVenue.contains("Patras")) {
            openFailureScreen(
                    "Σφάλμα Διαθεσιμότητας: Η τοποθεσία '" + selectedVenue.split(" - ")[1]
                            + "' δεν είναι διαθέσιμη για την επιλεγμένη ημερομηνία (UnavailableVenueScreen).",
                    "/create_concert.fxml", "MusicTick - Create Concert");
            return;
        }

        // 3. createTemporaryConcert() & display ConfirmationScreen & createAdminAlert()
        int venueId = Integer.parseInt(selectedVenue.split(" - ")[0].trim());

        try (Connection conn = DBConfig.getConnection()) {
            String insertSql = "INSERT INTO concerts (organizer_id, venue_id, title, description, concert_date, status, average_rating) VALUES (?, ?, ?, ?, ?, 'PENDING', 0.0)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, com.musictick.Session.getCurrentUserId()); // dynamic organizer ID
                ps.setInt(2, venueId);
                ps.setString(3, title);
                ps.setString(4, desc);

                // Parse dateStr or default to current timestamp
                java.sql.Timestamp timestamp;
                try {
                    timestamp = java.sql.Timestamp.valueOf(dateStr);
                } catch (Exception ex) {
                    timestamp = new java.sql.Timestamp(System.currentTimeMillis());
                }
                ps.setTimestamp(5, timestamp);
                ps.executeUpdate();

                try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int genId = rs.getInt(1);
                        System.out.println("OrganizerController: inserted concert, generated ID = " + genId
                                + " mapped to organizer = " + com.musictick.Session.getCurrentUserId());

                        // Insert Ticket Types (Regular & VIP) into database with organizer's specified
                        // prices!
                        String ticketTypesSql = "INSERT INTO ticket_types (concert_id, name, price, quantity) VALUES (?, 'REGULAR', ?, 100), (?, 'VIP', ?, 50)";
                        try (PreparedStatement ttPs = conn.prepareStatement(ticketTypesSql)) {
                            double regPrice = 35.0;
                            double vipPrice = 75.0;
                            try {
                                regPrice = Double.parseDouble(regPriceStr);
                            } catch (Exception ex) {
                            }
                            try {
                                vipPrice = Double.parseDouble(vipPriceStr);
                            } catch (Exception ex) {
                            }
                            ttPs.setInt(1, genId);
                            ttPs.setDouble(2, regPrice);
                            ttPs.setInt(3, genId);
                            ttPs.setDouble(4, vipPrice);
                            ttPs.executeUpdate();
                            System.out.println("OrganizerController: inserted ticket types for concert genId = " + genId
                                    + " (Regular: " + regPrice + ", VIP: " + vipPrice + ")");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            openFailureScreen("Σφάλμα συστήματος: Αποτυχία σύνδεσης με τη βάση δεδομένων.", "/create_concert.fxml",
                    "MusicTick - Create Concert");
            return;
        }

        // Configure SuccessController for Event Creation (ConfirmationScreen sequence
        // block)
        com.musictick.controller.SuccessController.titleText = "Η Συναυλία Δημιουργήθηκε! 📅";
        com.musictick.controller.SuccessController.descText = "Η εκδήλωσή σας '" + title
                + "' δημιουργήθηκε επιτυχώς και έχει σταλεί για έγκριση στον Admin.";
        com.musictick.controller.SuccessController.buttonText = "ΕΠΙΣΤΡΟΦΗ ΣΤΗΝ ΑΡΧΙΚΗ 🏠";
        com.musictick.controller.SuccessController.nextFxml = "/organizer_home.fxml";
        com.musictick.controller.SuccessController.nextTitle = "MusicTick - Organizer Home";
        com.musictick.controller.SuccessController.nextWidth = 800;
        com.musictick.controller.SuccessController.nextHeight = 600;

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/success.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root, 650, 450));
            stage.setTitle("MusicTick - Επιτυχία");
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Σφάλμα κατά τη φόρτωση της οθόνης επιβεβαίωσης.");
        }
    }

    private void openFailureScreen(String msg, String backTarget, String backTitle) {
        try {
            com.musictick.controller.BookingFailureController.failureMessage = msg;
            com.musictick.controller.BookingFailureController.backTargetFxml = backTarget;
            com.musictick.controller.BookingFailureController.backTargetTitle = backTitle;
            Parent root = FXMLLoader.load(getClass().getResource("/booking_failure.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 550));
            stage.setTitle("MusicTick - Σφάλμα");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearCreateForm() {
        titleField.clear();
        descField.clear();
        dateField.clear();
        regularPriceField.clear();
        vipPriceField.clear();
        venueComboBox.setValue(null);
    }

    // 2. REVIEW PROBLEMS FLOW
    // 2. REVIEW PROBLEMS FLOW
    private void loadReports() {
        reports.clear();
        String sql = "SELECT report_id, ticket_id, description FROM reports WHERE status = 'OPEN'";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String line = "Report #" + rs.getInt("report_id") + " | Ticket #" + rs.getInt("ticket_id") + " : "
                        + rs.getString("description");
                reports.add(line);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        reportsListView.setItems(FXCollections.observableArrayList(reports));
    }

    @FXML
    private void handleApproveRefund() {
        String selected = reportsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια αναφορά προβλήματος.");
            return;
        }

        int ticketId = parseTicketId(selected);
        int reportId = parseReportId(selected);
        int userId = findUserIdForTicket(ticketId);

        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String upRep = "UPDATE reports SET status = 'REFUND_APPROVED' WHERE report_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(upRep)) {
                    ps.setInt(1, reportId);
                    ps.executeUpdate();
                }

                String upTick = "UPDATE tickets SET status = 'CANCELLED' WHERE ticket_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(upTick)) {
                    ps.setInt(1, ticketId);
                    ps.executeUpdate();
                }
                com.musictick.manager.WaitlistManager.handleTicketCancellation(ticketId);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("❌ Αποτυχία ενημέρωσης βάσης.");
            return;
        }

        // Send notification to the user
        String notifTitle = "Επιστροφή Χρημάτων Εγκρίθηκε 💸";
        String notifMsg = "Η αίτηση επιστροφής χρημάτων για το εισιτήριο #" + ticketId
                + " εγκρίθηκε από τον διοργανωτή. Τα χρήματά σας θα επιστραφούν σύντομα.";

        try {
            new com.musictick.dao.AlertDAO().saveAlert(userId, notifTitle, notifMsg, "REFUND");
            System.out.println("OrganizerController: refund notification sent to userId=" + userId
                    + " for ticketId=" + ticketId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setStatus("💸 Αποζημίωση εγκρίθηκε! Ο χρήστης ενημερώθηκε για την επιστροφή χρημάτων.");
        loadReports();
    }

    /**
     * Parses the ticket ID from a report entry like "Report #1 | Ticket #302 : ..."
     */
    private int parseTicketId(String reportLine) {
        try {
            String part = reportLine.split("Ticket #")[1].split(" ")[0].split(":")[0].trim();
            return Integer.parseInt(part);
        } catch (Exception e) {
            return -1;
        }
    }

    private int parseReportId(String reportLine) {
        try {
            String part = reportLine.split("Report #")[1].split(" \\|")[0].trim();
            return Integer.parseInt(part);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Finds the user who owns a given ticket.
     */
    private int findUserIdForTicket(int ticketId) {
        try (Connection conn = DBConfig.getConnection()) {
            String sql = "SELECT user_id FROM tickets WHERE ticket_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, ticketId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // fallback customer ID
    }

    @FXML
    private void handleDismissReport() {
        String selected = reportsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια αναφορά προβλήματος.");
            return;
        }

        int reportId = parseReportId(selected);

        try (Connection conn = DBConfig.getConnection()) {
            String sql = "UPDATE reports SET status = 'CLOSED' WHERE report_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, reportId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("❌ Αποτυχία ενημέρωσης βάσης.");
            return;
        }

        setStatus("❌ Η αναφορά αρχειοθετήθηκε/απορρίφθηκε.");
        loadReports();
    }

    // GENERAL NAVIGATION
    @FXML
    private void goBack() {
        openPage("/organizer_home.fxml", "MusicTick - Organizer Home", 800, 600);
    }

    private void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }

    private void openPage(String fxml, String title, int width, int height) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
