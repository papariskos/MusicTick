package com.musictick.dao;

import models.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/musictick?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<Notification> returnAlerts(int recipientId) throws SQLException {
        List<Notification> alerts = new ArrayList<>();
        String sql = "SELECT notification_id, recipient_id, title, message, type, is_read, created_at FROM notifications WHERE recipient_id = ? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification notification = new Notification(
                            rs.getInt("notification_id"),
                            rs.getInt("recipient_id"),
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getString("type"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    alerts.add(notification);
                }
            }
        }
        return alerts;
    }

    public void saveAlert(int recipientId, String title, String message, String type) throws SQLException {
        String sql = "INSERT INTO notifications (recipient_id, title, message, type) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.executeUpdate();
        }
    }
}
