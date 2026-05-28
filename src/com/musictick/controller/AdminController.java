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

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
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
                String item = rs.getInt("concert_id") + " - " + rs.getString("title") + " ("
                        + rs.getString("description") + ")";
                concerts.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pendingConcertsListView.setItems(FXCollections.observableArrayList(concerts));
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
                String item = "Report #" + rs.getInt("post_report_id") + " | Post #" + rs.getInt("post_id") + ": "
                        + rs.getString("content") + " (Reason: " + rs.getString("reason") + ")";
                reports.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        forumReportsListView.setItems(FXCollections.observableArrayList(reports));
    }

    private int findOrganizerIdForConcert(int concertId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT organizer_id FROM concerts WHERE concert_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, concertId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("organizer_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 2; // fallback to default organizer
    }

    @FXML
    private void handleApproveConcert() {
        String selected = pendingConcertsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια συναυλία προς έγκριση.");
            return;
        }

        int concertId = parseConcertId(selected);
        String cleanTitle = parseConcertTitle(selected);
        int organizerId = findOrganizerIdForConcert(concertId);

        // 1. Send notification to the organizer in DB (createOrganizerAlert)
        try {
            new com.musictick.dao.AlertDAO().saveAlert(organizerId,
                "Έγκριση Συναυλίας 🎉",
                "Η συναυλία σας '" + cleanTitle + "' εγκρίθηκε από τον Admin!", "ALERT");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 2. DB update status to APPROVED strictly
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement("UPDATE concerts SET status = 'APPROVED' WHERE concert_id = ?")) {
            ps.setInt(1, concertId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setStatus("✅ Η συναυλία '" + cleanTitle + "' εγκρίθηκε! Notification στάλθηκε στον Organizer.");
        loadPendingConcerts();
    }

    @FXML
    private void handleRejectConcert() {
        String selected = pendingConcertsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Επιλέξτε μια συναυλία προς απόρριψη.");
            return;
        }

        int concertId = parseConcertId(selected);
        String cleanTitle = parseConcertTitle(selected);
        int organizerId = findOrganizerIdForConcert(concertId);

        // 1. Send rejection notification to organizer in DB (createRejectionAlert)
        try {
            new com.musictick.dao.AlertDAO().saveAlert(organizerId,
                "Απόρριψη Συναυλίας ❌",
                "Η συναυλία σας '" + cleanTitle + "' απορρίφθηκε από τον Admin.", "ALERT");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 2. DB update status to REJECTED strictly
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement("UPDATE concerts SET status = 'REJECTED' WHERE concert_id = ?")) {
            ps.setInt(1, concertId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setStatus("❌ Η συναυλία '" + cleanTitle + "' απορρίφθηκε. Notification στάλθηκε στον Organizer.");
        loadPendingConcerts();
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
            
            // Delete post in DB (which also sends alert via AlertDAO inside deletePost method)
            com.musictick.manager.ForumManager.deletePost(3, postId);

            // ALSO delete related reports in DB
            String sqlDelReports = "DELETE FROM post_reports WHERE post_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps2 = conn.prepareStatement(sqlDelReports)) {
                ps2.setInt(1, postId);
                ps2.executeUpdate();
            }
            
            setStatus("Η ανάρτηση διαγράφηκε με επιτυχία!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadForumReports();
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

            // Lock thread in DB
            com.musictick.manager.ForumManager.lockThread(3, postId);

            String sql = "UPDATE forum_posts SET is_locked = TRUE WHERE post_id = ?";

            try (Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();

                // ALSO delete related reports in DB since the report is now resolved
                String sqlDelReports = "DELETE FROM post_reports WHERE post_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(sqlDelReports)) {
                    ps2.setInt(1, postId);
                    ps2.executeUpdate();
                }

                setStatus("Το νήμα κλειδώθηκε με επιτυχία!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadForumReports();
    }

    @FXML
    private void handleNotifications() {
        openPage("/notifications.fxml", "MusicTick - Notifications", 800, 550);
    }

    @FXML
    private void handleManageConcerts() {
        openPage("/admin_concerts.fxml", "MusicTick - Manage Active Concerts", 800, 600);
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

    // ── Parsing Helpers ────────────────────────────────────────────────────────
    private int parseConcertId(String item) {
        return parseRawConcertId(item);
    }
    /** Static version usable before instance is ready. */
    public static int parseRawConcertId(String item) {
        try { return Integer.parseInt(item.split(" - ")[0].trim()); } catch (Exception e) { return -1; }
    }
    private String parseConcertTitle(String item) {
        try { return item.split(" - ")[1].split(" \\(")[0].trim(); } catch (Exception e) { return item; }
    }
    private String parseConcertDesc(String item) {
        try { return item.split(" \\(")[1].replace(")", "").trim(); } catch (Exception e) { return ""; }
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
