package com.musictick.manager;

import com.musictick.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisteredList {

    public static String findRecipient(int recipientId) {
        System.out.println("RegisteredList: findRecipient() called for recipientId=" + recipientId);
        
        // 1. Try DB
        try (Connection conn = DBConfig.getConnection()) {
            String sql = "SELECT role FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, recipientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");
                        System.out.println("RegisteredList: found role from DB -> " + role);
                        return role.toUpperCase();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("RegisteredList: DB offline, falling back to mock mapping.");
        }

        // 2. Fallback to Session or Mock
        if (recipientId == com.musictick.Session.getCurrentUserId()) {
            return com.musictick.Session.getCurrentUserRole();
        }
        if (recipientId == 0 || recipientId == 3) return "ADMIN";
        if (recipientId == 2) return "ORGANIZER";
        return "CUSTOMER"; // default recipient role
    }
}
