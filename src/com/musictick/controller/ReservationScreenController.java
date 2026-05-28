package com.musictick.controller;

import com.musictick.DBConfig;
import com.musictick.Main;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationScreenController {

    @FXML private ComboBox<String> ticketComboBox;
    @FXML private Label statusLabel;

    /** The full ticket line selected by the user, to be removed on successful cancellation. */
    public static String selectedTicketLine;

    @FXML
    public void initialize() {
        List<String> tickets = loadTicketsFromDB();
        if (ticketComboBox != null) {
            ticketComboBox.setItems(FXCollections.observableArrayList(tickets));
        }
    }

    private List<String> loadTicketsFromDB() {
        List<String> tickets = new ArrayList<>();
        int currentUserId = com.musictick.Session.getCurrentUserId();
        
        try (java.sql.Connection conn = DBConfig.getConnection()) {
            String sql = "SELECT t.ticket_id, c.title, s.section_name, s.row_label, s.seat_number " +
                         "FROM tickets t " +
                         "JOIN concerts c ON t.concert_id = c.concert_id " +
                         "JOIN seats s ON t.seat_id = s.seat_id " +
                         "WHERE t.user_id = ? AND t.status IN ('ACTIVE', 'TEMPORARY', 'UPGRADED')";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, currentUserId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ticketDetails = "Ticket #" + rs.getInt("ticket_id") + " - " + rs.getString("title") +
                                " | Section " + rs.getString("section_name") + ", Row " + rs.getString("row_label") + ", Seat " + rs.getString("seat_number");
                        tickets.add(ticketDetails);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tickets;
    }

    @FXML
    private void submitReservationData() {
        String selected = ticketComboBox != null ? ticketComboBox.getValue() : null;
        if (selected == null || selected.isBlank()) {
            if (statusLabel != null) statusLabel.setText("Επιλέξτε ένα εισιτήριο από τη λίστα.");
            return;
        }
        selectedTicketLine = selected;
        ManageCancellationController.validateReservationData(selected);
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cancellation_screen.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("MusicTick - Cancellation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
