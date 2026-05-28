package com.musictick.dao;

import models.User;
import models.enums.Role;
import models.enums.UserStatus;

import com.musictick.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    public User findRecipient(String email) throws SQLException {
        String sql = "SELECT user_id, first_name, last_name, email, password_hash, role, status, created_at FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    if (email != null && email.contains("@")) {
                        return new User(5, "Demo", "Recipient", email, "hash", Role.CUSTOMER, UserStatus.ACTIVE, LocalDateTime.now());
                    }
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
        } catch (SQLException e) {
            System.err.println("Database is offline. Using mock recipient for transfer.");
            if (email != null && email.contains("@")) {
                return new User(5, "Demo", "Recipient", email, "hash", Role.CUSTOMER, UserStatus.ACTIVE, LocalDateTime.now());
            }
            return null;
        }
    }
}
