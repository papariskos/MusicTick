package com.musictick.dao;

import models.User;
import models.enums.Role;
import models.enums.UserStatus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/musictick?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public User findRecipient(String email) throws SQLException {
        String sql = "SELECT user_id, first_name, last_name, email, password_hash, role, status, created_at FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role")),
                        UserStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        }
    }
}
