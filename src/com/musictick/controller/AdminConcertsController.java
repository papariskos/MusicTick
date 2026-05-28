package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.DBConfig;
import com.musictick.dao.ConcertDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import models.Concert;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminConcertsController {

    @FXML
    private ListView<String> activeConcertsListView;
    @FXML
    private Label statusLabel;

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    @FXML
    public void initialize() {
        loadActiveConcerts();
    }

    private void loadActiveConcerts() {
        List<String> listItems = new ArrayList<>();

        // Fetch from Database strictly
        String sql = "SELECT concert_id, title, description FROM concerts WHERE status = 'APPROVED'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String item = rs.getInt("concert_id") + " - " + rs.getString("title") + " (" + rs.getString("description") + ") [DB]";
                listItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        activeConcertsListView.setItems(FXCollections.observableArrayList(listItems));
    }

    @FXML
    private void handleDeleteConcert() {
        String selected = activeConcertsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια συναυλία προς διαγραφή.", "#e74c3c");
            return;
        }

        int concertId = parseConcertId(selected);
        if (concertId == -1) {
            setStatus("Σφάλμα κατά την ανάλυση του ID της συναυλίας.", "#e74c3c");
            return;
        }

        String title = parseConcertTitle(selected);

        // Deletion from SQL Database strictly inside a transaction
        boolean removedDB = false;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete refunds referencing payments referencing orders referencing tickets of this concert
                String delRefunds = "DELETE FROM refunds WHERE payment_id IN (" +
                                    "  SELECT payment_id FROM payments WHERE order_id IN (" +
                                    "    SELECT order_id FROM order_tickets WHERE ticket_id IN (" +
                                    "      SELECT ticket_id FROM tickets WHERE concert_id = ? OR ticket_type_id IN (SELECT ticket_type_id FROM ticket_types WHERE concert_id = ?)" +
                                    "    )" +
                                    "  )" +
                                    ")";
                try (PreparedStatement ps = conn.prepareStatement(delRefunds)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, concertId);
                    ps.executeUpdate();
                }

                // Delete payments referencing orders referencing tickets of this concert
                String delPayments = "DELETE FROM payments WHERE order_id IN (" +
                                     "  SELECT order_id FROM order_tickets WHERE ticket_id IN (" +
                                     "    SELECT ticket_id FROM tickets WHERE concert_id = ? OR ticket_type_id IN (SELECT ticket_type_id FROM ticket_types WHERE concert_id = ?)" +
                                     "  )" +
                                     ")";
                try (PreparedStatement ps = conn.prepareStatement(delPayments)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, concertId);
                    ps.executeUpdate();
                }

                // Delete order_tickets referencing tickets of this concert
                String delOrderTickets = "DELETE FROM order_tickets WHERE ticket_id IN (" +
                                         "  SELECT ticket_id FROM tickets WHERE concert_id = ? OR ticket_type_id IN (SELECT ticket_type_id FROM ticket_types WHERE concert_id = ?)" +
                                         ")";
                try (PreparedStatement ps = conn.prepareStatement(delOrderTickets)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, concertId);
                    ps.executeUpdate();
                }

                // Clean empty orders
                String delEmptyRefunds = "DELETE FROM refunds WHERE payment_id IN (" +
                                         "  SELECT payment_id FROM payments WHERE order_id NOT IN (" +
                                         "    SELECT order_id FROM order_tickets" +
                                         "  )" +
                                         ")";
                conn.createStatement().executeUpdate(delEmptyRefunds);

                String delEmptyPayments = "DELETE FROM payments WHERE order_id NOT IN (" +
                                          "  SELECT order_id FROM order_tickets" +
                                          ")";
                conn.createStatement().executeUpdate(delEmptyPayments);

                String delEmptyOrders = "DELETE FROM orders WHERE order_id NOT IN (SELECT order_id FROM order_tickets)";
                conn.createStatement().executeUpdate(delEmptyOrders);

                // Delete reports referencing tickets of this concert
                String delReports = "DELETE FROM reports WHERE ticket_id IN (" +
                                    "  SELECT ticket_id FROM tickets WHERE concert_id = ? OR ticket_type_id IN (SELECT ticket_type_id FROM ticket_types WHERE concert_id = ?)" +
                                    ")";
                try (PreparedStatement ps = conn.prepareStatement(delReports)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, concertId);
                    ps.executeUpdate();
                }

                // Delete tickets of this concert
                String delTickets = "DELETE FROM tickets WHERE concert_id = ? OR ticket_type_id IN (SELECT ticket_type_id FROM ticket_types WHERE concert_id = ?)";
                try (PreparedStatement ps = conn.prepareStatement(delTickets)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, concertId);
                    ps.executeUpdate();
                }

                // Finally delete the concert
                String delConcert = "DELETE FROM concerts WHERE concert_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(delConcert)) {
                    ps.setInt(1, concertId);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        removedDB = true;
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (removedDB) {
            setStatus("✅ Η συναυλία '" + title + "' διαγράφηκε επιτυχώς!", "#2ecc71");
            loadActiveConcerts();
        } else {
            setStatus("❌ Αποτυχία διαγραφής της συναυλίας.", "#e74c3c");
        }
    }

    @FXML
    private void handleGoBack() {
        openPage("/admin_home.fxml", "MusicTick - Admin Dashboard", 800, 600);
    }

    private int parseConcertId(String item) {
        try {
            return Integer.parseInt(item.split(" - ")[0].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private String parseConcertTitle(String item) {
        try {
            return item.split(" - ")[1].split(" \\(")[0].trim();
        } catch (Exception e) {
            return item;
        }
    }

    private void setStatus(String msg, String color) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
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
