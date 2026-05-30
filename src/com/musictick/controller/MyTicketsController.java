package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.DBConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyTicketsController {

    @FXML private ListView<String> ticketsListView;
    @FXML private Label statusLabel;


    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }
    @FXML
    public void initialize() {
        Session.setSelectedTicketIdForCancellation(-1);
        loadPurchasedTickets();
    }

    private void loadPurchasedTickets() {
        List<String> tickets = new ArrayList<>();
        int currentUserId = Session.getCurrentUserId();

        // Query database for active tickets strictly!
        try (Connection conn = DBConfig.getConnection()) {
            String sql = "SELECT t.ticket_id, c.concert_id, c.title, s.section_name, s.row_label, s.seat_number " +
                         "FROM tickets t " +
                         "JOIN concerts c ON t.concert_id = c.concert_id " +
                         "JOIN seats s ON t.seat_id = s.seat_id " +
                         "WHERE t.user_id = ? AND t.status IN ('ACTIVE', 'TEMPORARY', 'UPGRADED')";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, currentUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ticketDetails = "Ticket #" + rs.getInt("ticket_id") + " [Concert ID: " + rs.getInt("concert_id") + "] - " + rs.getString("title") +
                                " | Section " + rs.getString("section_name") + ", Row " + rs.getString("row_label") + ", Seat " + rs.getString("seat_number");
                        tickets.add(ticketDetails);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ticketsListView.setItems(FXCollections.observableArrayList(tickets));
        statusLabel.setText("Συνολικά Εισιτήρια: " + tickets.size());
    }

    @FXML
    private void goBack() {
        openPage("/user_home.fxml", "MusicTick - Home", 900, 600);
    }

    @FXML
    private void cancelTicket() {
        String selected = ticketsListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            statusLabel.setText("⚠️ Παρακαλώ επιλέξτε ένα εισιτήριο από τη λίστα πρώτα.");
            return;
        }

        int ticketId = -1;
        try {
            if (selected.startsWith("Ticket #")) {
                StringBuilder sb = new StringBuilder();
                for (int i = "Ticket #".length(); i < selected.length(); i++) {
                    char c = selected.charAt(i);
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                ticketId = Integer.parseInt(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ticketId != -1) {
            Session.setSelectedTicketIdForCancellation(ticketId);
        } else {
            Session.setSelectedTicketIdForCancellation(-1);
        }

        openPage("/cancellation_screen.fxml", "MusicTick - Cancellation", 900, 600);
    }

    private void openPage(String fxml, String title, int width, int height) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            if (stage != null) {
                stage.setScene(new Scene(root, width, height));
                stage.setTitle(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
