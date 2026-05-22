package com.musictick.dao;

import models.Ticket;
import models.enums.TicketStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/musictick?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<Ticket> findUserTickets(int userId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT ticket_id, concert_id, user_id, ticket_type_id, seat_id, status, qr_code, purchase_date " +
                     "FROM tickets WHERE user_id = ? AND status = 'ACTIVE'";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket ticket = new Ticket(
                            rs.getInt("ticket_id"),
                            rs.getInt("concert_id"),
                            rs.getInt("user_id"),
                            rs.getInt("ticket_type_id"),
                            rs.getObject("seat_id") == null ? null : rs.getInt("seat_id"),
                            TicketStatus.valueOf(rs.getString("status")),
                            rs.getString("qr_code"),
                            rs.getTimestamp("purchase_date").toLocalDateTime()
                    );
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    public boolean checkTicketValidity(int ticketId, int currentUserId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM tickets WHERE ticket_id = ? AND user_id = ? AND status = 'ACTIVE'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            ps.setInt(2, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        }
    }

    public int transferTicket(int ticketId, int currentUserId, int recipientId) throws SQLException {
        String sql = "UPDATE tickets SET user_id = ?, status = 'TRANSFERRED' WHERE ticket_id = ? AND user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            ps.setInt(2, ticketId);
            ps.setInt(3, currentUserId);
            int updated = ps.executeUpdate();
            return updated > 0 ? ticketId : -1;
        }
    }
}
