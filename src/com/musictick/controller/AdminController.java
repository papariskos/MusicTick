package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    @FXML
    private ListView<String> pendingConcertsListView;
    @FXML
    private ListView<String> forumReportsListView;
    @FXML
    private Label statusLabel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/musictick?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @FXML
    public void initialize() {
        loadPendingConcerts();
        loadForumReports();
    }

    private void loadPendingConcerts() {
        List<String> concerts = new ArrayList<>();
        String sql = "SELECT concert_id, title, description FROM concerts WHERE status = 'PENDING'";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                concerts.add(rs.getInt("concert_id") + " - " + rs.getString("title") + " ("
                        + rs.getString("description") + ")");
            }
            pendingConcertsListView.setItems(FXCollections.observableArrayList(concerts));
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("Σφάλμα κατά τη φόρτωση συναυλιών.");
        }
    }

    private void loadForumReports() {
        List<String> reports = new ArrayList<>();
        String sql = "SELECT pr.post_report_id, pr.reason, fp.post_id, fp.content " +
                "FROM post_reports pr " +
                "JOIN forum_posts fp ON pr.post_id = fp.post_id " +
                "WHERE fp.is_deleted = FALSE";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reports.add("Report #" + rs.getInt("post_report_id") + " | Post #" + rs.getInt("post_id") + ": "
                        + rs.getString("content") + " (Reason: " + rs.getString("reason") + ")");
            }
            forumReportsListView.setItems(FXCollections.observableArrayList(reports));
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("Σφάλμα κατά τη φόρτωση αναφορών Forum.");
        }
    }

    @FXML
    private void handleApproveConcert() {
        String selected = pendingConcertsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια συναυλία προς έγκριση.");
            return;
        }

        int concertId = Integer.parseInt(selected.split(" - ")[0].trim());
        String sql = "UPDATE concerts SET status = 'APPROVED' WHERE concert_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concertId);
            ps.executeUpdate();
            setStatus("Η συναυλία εγκρίθηκε με επιτυχία!");
            loadPendingConcerts();
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("Σφάλμα κατά την έγκριση συναυλίας.");
        }
    }

    @FXML
    private void handleRejectConcert() {
        String selected = pendingConcertsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια συναυλία προς απόρριψη.");
            return;
        }

        int concertId = Integer.parseInt(selected.split(" - ")[0].trim());
        String sql = "UPDATE concerts SET status = 'REJECTED' WHERE concert_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concertId);
            ps.executeUpdate();
            setStatus("Η συναυλία απορρίφθηκε.");
            loadPendingConcerts();
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("Σφάλμα κατά την απόρριψη συναυλίας.");
        }
    }

    @FXML
    private void handleDeletePost() {
        String selected = forumReportsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια αναφορά Forum.");
            return;
        }

        try {
            int postId = Integer.parseInt(selected.split("Post #")[1].split(":")[0].trim());
            String sql = "UPDATE forum_posts SET is_deleted = TRUE WHERE post_id = ?";

            try (Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();
                setStatus("Η ανάρτηση διαγράφηκε με επιτυχία!");
                loadForumReports();
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Σφάλμα κατά τη διαγραφή της ανάρτησης.");
        }
    }

    @FXML
    private void handleLockThread() {
        String selected = forumReportsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια αναφορά Forum.");
            return;
        }

        try {
            int postId = Integer.parseInt(selected.split("Post #")[1].split(":")[0].trim());
            String sql = "UPDATE forum_posts SET is_locked = TRUE WHERE post_id = ?";

            try (Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();
                setStatus("Το νήμα κλειδώθηκε με επιτυχία!");
                loadForumReports();
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Σφάλμα κατά το κλείδωμα του νήματος.");
        }
    }

    @FXML
    private void handleNotifications() {
        openPage("/notifications.fxml", "MusicTick - Notifications", 800, 550);
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 500, 500));
            stage.setTitle("MusicTick - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
