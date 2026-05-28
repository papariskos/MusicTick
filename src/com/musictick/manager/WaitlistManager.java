package com.musictick.manager;

import com.musictick.DBConfig;
import java.math.BigDecimal;
import java.sql.*;

public class WaitlistManager {

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    public boolean isAlreadyInWaitlist(int userId, int concertId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM waitlist_entries WHERE user_id = ? AND concert_id = ? AND status = 'WAITING'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, concertId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        }
    }

    public int joinWaitlist(int userId, int concertId) {
        return joinWaitlistWithCard(userId, concertId, null);
    }

    public int joinWaitlistWithCard(int userId, int concertId, String cardDetails) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (isAlreadyInWaitlist(userId, concertId)) {
                    conn.rollback();
                    return -2; // Code for already in waitlist
                }

                // Calculate next priority order
                int nextPriority = 1;
                String prSql = "SELECT COALESCE(MAX(priority_order), 0) AS max_pr FROM waitlist_entries WHERE concert_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(prSql)) {
                    ps.setInt(1, concertId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            nextPriority = rs.getInt("max_pr") + 1;
                        }
                    }
                }

                // Insert into waitlist including card_details
                String insertSql = "INSERT INTO waitlist_entries (concert_id, user_id, priority_order, status, card_details) VALUES (?, ?, ?, 'WAITING', ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, userId);
                    ps.setInt(3, nextPriority);
                    ps.setString(4, cardDetails);
                    ps.executeUpdate();
                }

                conn.commit();
                return nextPriority;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return -1; // General database error
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void handleTicketCancellation(int cancelledTicketId) {
        System.out.println("WaitlistManager: handleTicketCancellation() called for ticketId=" + cancelledTicketId);
        String queryTicket = "SELECT concert_id, seat_id, ticket_type_id FROM tickets WHERE ticket_id = ?";
        
        try (Connection conn = DBConfig.getConnection()) {
            int concertId = -1;
            int seatId = -1;
            int ticketTypeId = -1;
            
            try (PreparedStatement ps = conn.prepareStatement(queryTicket)) {
                ps.setInt(1, cancelledTicketId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        concertId = rs.getInt("concert_id");
                        seatId = rs.getInt("seat_id");
                        ticketTypeId = rs.getInt("ticket_type_id");
                    }
                }
            }
            
            if (concertId == -1) return;
            
            // Find first person in waitlist
            String queryWaitlist = "SELECT waitlist_id, user_id, card_details FROM waitlist_entries " +
                                   "WHERE concert_id = ? AND status = 'WAITING' ORDER BY priority_order ASC LIMIT 1";
            
            int waitlistId = -1;
            int userId = -1;
            String cardDetails = null;
            
            try (PreparedStatement ps = conn.prepareStatement(queryWaitlist)) {
                ps.setInt(1, concertId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        waitlistId = rs.getInt("waitlist_id");
                        userId = rs.getInt("user_id");
                        cardDetails = rs.getString("card_details");
                    }
                }
            }
            
            if (waitlistId == -1) {
                System.out.println("WaitlistManager: No waiting users in the waitlist for concert ID " + concertId);
                return;
            }
            
            // Start transactional auto-promotion
            conn.setAutoCommit(false);
            try {
                // Nullify the seat_id on the cancelled ticket to release it for the waitlist user!
                String releaseSeatSql = "UPDATE tickets SET seat_id = NULL WHERE ticket_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(releaseSeatSql)) {
                    ps.setInt(1, cancelledTicketId);
                    ps.executeUpdate();
                }

                // Get concert title
                String concertTitle = "Συναυλία";
                String queryConcert = "SELECT title FROM concerts WHERE concert_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(queryConcert)) {
                    ps.setInt(1, concertId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            concertTitle = rs.getString("title");
                        }
                    }
                }
                
                // Get ticket price to simulate/insert payment
                BigDecimal price = new BigDecimal("35.00");
                String queryPrice = "SELECT price FROM ticket_types WHERE ticket_type_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(queryPrice)) {
                    ps.setInt(1, ticketTypeId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            price = rs.getBigDecimal("price");
                        }
                    }
                }
                
                // Create order for waitlist user
                String orderSql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, 'PAID')";
                int orderId = -1;
                try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setBigDecimal(2, price);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            orderId = rs.getInt(1);
                        }
                    }
                }
                
                // Create payment
                String paymentSql = "INSERT INTO payments (order_id, amount, payment_method, payment_status, transaction_reference) " +
                                     "VALUES (?, ?, 'CARD', 'SUCCESS', ?)";
                try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                    ps.setInt(1, orderId);
                    ps.setBigDecimal(2, price);
                    ps.setString(3, cardDetails != null ? cardDetails : "PRE-AUTH-WAITLIST");
                    ps.executeUpdate();
                }
                
                // Assign seat/ticket to waitlist user
                String insertTicketSql = "INSERT INTO tickets (concert_id, user_id, ticket_type_id, seat_id, status, qr_code) " +
                                         "VALUES (?, ?, ?, ?, 'ACTIVE', ?)";
                int newTicketId = -1;
                try (PreparedStatement ps = conn.prepareStatement(insertTicketSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, userId);
                    ps.setInt(3, ticketTypeId);
                    ps.setInt(4, seatId);
                    ps.setString(5, "QR-WAITLIST-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            newTicketId = rs.getInt(1);
                        }
                    }
                }
                
                // Link order and ticket
                String linkSql = "INSERT INTO order_tickets (order_id, ticket_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(linkSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, newTicketId);
                    ps.executeUpdate();
                }
                
                // Update waitlist entry to RESERVED
                String updateWaitlistSql = "UPDATE waitlist_entries SET status = 'RESERVED' WHERE waitlist_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateWaitlistSql)) {
                    ps.setInt(1, waitlistId);
                    ps.executeUpdate();
                }
                
                // Send Notification
                String notificationSql = "INSERT INTO notifications (recipient_id, title, message, type, is_read) " +
                                          "VALUES (?, ?, ?, 'ALERT', FALSE)";
                try (PreparedStatement ps = conn.prepareStatement(notificationSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, "Εισιτήριο από Λίστα Αναμονής! 🎫");
                    ps.setString(3, "Συγχαρητήρια! Μια θέση ελευθερώθηκε για τη συναυλία '" + concertTitle + 
                                     "'. Η κάρτα σας χρεώθηκε επιτυχώς με " + price + " € και το εισιτήριό σας εκδόθηκε!");
                    ps.executeUpdate();
                }
                
                conn.commit();
                System.out.println("WaitlistManager: User ID " + userId + " promoted from waitlist to ACTIVE ticket #" + newTicketId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
