package com.musictick.dao;

import com.musictick.DBConfig;
import models.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
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

    public void deleteAlert(int notificationId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        }
    }
}
