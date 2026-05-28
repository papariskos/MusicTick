package com.musictick.dao;

import com.musictick.DBConfig;
import models.Ticket;
import models.enums.TicketStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    public List<Ticket> findUserTickets(int userId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();

        try (Connection conn = getConnection()) {
            String sql = "SELECT t.ticket_id, t.concert_id, t.user_id, t.ticket_type_id, t.seat_id, t.status, t.qr_code, t.purchase_date, c.title " +
                         "FROM tickets t JOIN concerts c ON t.concert_id = c.concert_id WHERE t.user_id = ? AND t.status IN ('ACTIVE', 'UPGRADED')";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final String title = rs.getString("title");
                        Ticket ticket = new Ticket(
                                rs.getInt("ticket_id"),
                                rs.getInt("concert_id"),
                                rs.getInt("user_id"),
                                rs.getInt("ticket_type_id"),
                                rs.getObject("seat_id") == null ? null : rs.getInt("seat_id"),
                                TicketStatus.valueOf(rs.getString("status")),
                                rs.getString("qr_code"),
                                rs.getTimestamp("purchase_date").toLocalDateTime()
                        ) {
                            @Override
                            public String toString() {
                                return getTicketId() + " - " + title + " (Active)";
                            }
                        };
                        tickets.add(ticket);
                    }
                }
            }
        }
        return tickets;
    }

    public boolean checkTicketValidity(int ticketId, int currentUserId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM tickets WHERE ticket_id = ? AND user_id = ? AND status IN ('ACTIVE', 'UPGRADED')";
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
        String sql = "UPDATE tickets SET user_id = ? WHERE ticket_id = ? AND user_id = ? AND status IN ('ACTIVE', 'UPGRADED')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            ps.setInt(2, ticketId);
            ps.setInt(3, currentUserId);
            ps.executeUpdate();
            return ticketId;
        }
    }
}
