package com.musictick;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VIPUpgradeManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/musictick";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<String> getUserActiveTickets(int userId) throws SQLException {
        List<String> tickets = new ArrayList<>();
        String sql = "SELECT t.ticket_id, c.title, t.status " +
                     "FROM tickets t JOIN concerts c ON t.concert_id = c.concert_id " +
                     "WHERE t.user_id = ? AND t.status = 'ACTIVE'";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tickets.add(rs.getInt("ticket_id") + " - " + rs.getString("title") + " (" + rs.getString("status") + ")");
            }
        }
        return tickets;
    }

    public List<String> getAvailableVipSeatsForTicket(int ticketId) throws SQLException {
        List<String> seats = new ArrayList<>();
        String sql = "SELECT s.seat_id, s.section_name, s.row_label, s.seat_number " +
                     "FROM seats s " +
                     "JOIN concerts c ON s.venue_id = c.venue_id " +
                     "JOIN tickets selected_ticket ON selected_ticket.concert_id = c.concert_id " +
                     "WHERE selected_ticket.ticket_id = ? " +
                     "AND s.seat_type = 'VIP' " +
                     "AND s.seat_id NOT IN (" +
                     "    SELECT seat_id FROM tickets WHERE concert_id = selected_ticket.concert_id AND seat_id IS NOT NULL" +
                     ")";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                seats.add(rs.getInt("seat_id") + " - Section " + rs.getString("section_name") +
                        ", Row " + rs.getString("row_label") + ", Seat " + rs.getString("seat_number"));
            }
        }
        return seats;
    }

    public BigDecimal calculateExtraAmount(int ticketId) throws SQLException {
        String sql = "SELECT current_type.price AS current_price, vip_type.price AS vip_price " +
                     "FROM tickets t " +
                     "JOIN ticket_types current_type ON t.ticket_type_id = current_type.ticket_type_id " +
                     "JOIN ticket_types vip_type ON vip_type.concert_id = t.concert_id AND vip_type.name = 'VIP' " +
                     "WHERE t.ticket_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return BigDecimal.ZERO;
            BigDecimal extra = rs.getBigDecimal("vip_price").subtract(rs.getBigDecimal("current_price"));
            return extra.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : extra;
        }
    }

    public String upgradeTicketToVIP(int userId, int ticketId, int vipSeatId, String paymentData) {
        if (paymentData == null || paymentData.trim().isEmpty()) {
            return "Συμπλήρωσε στοιχεία πληρωμής.";
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            if (!isValidTicket(conn, userId, ticketId)) {
                conn.rollback();
                return "Μη έγκυρο εισιτήριο.";
            }

            int vipTicketTypeId = findVipTicketTypeId(conn, ticketId);
            if (vipTicketTypeId == -1) {
                conn.rollback();
                return "Δεν υπάρχει VIP τύπος εισιτηρίου για αυτή τη συναυλία.";
            }

            if (!isVipSeatAvailable(conn, ticketId, vipSeatId)) {
                conn.rollback();
                return "Η VIP θέση δεν είναι διαθέσιμη.";
            }

            BigDecimal extraAmount = calculateExtraAmount(ticketId);

            String updateTicket = "UPDATE tickets SET ticket_type_id = ?, seat_id = ?, status = 'UPGRADED' WHERE ticket_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateTicket)) {
                ps.setInt(1, vipTicketTypeId);
                ps.setInt(2, vipSeatId);
                ps.setInt(3, ticketId);
                ps.executeUpdate();
            }

            int orderId = createOrder(conn, userId, extraAmount);
            createPayment(conn, orderId, extraAmount, paymentData);

            conn.commit();
            return "Το εισιτήριο αναβαθμίστηκε σε VIP επιτυχώς.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Σφάλμα κατά το VIP upgrade.";
        }
    }

    private boolean isValidTicket(Connection conn, int userId, int ticketId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM tickets WHERE ticket_id = ? AND user_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("total") > 0;
        }
    }

    private int findVipTicketTypeId(Connection conn, int ticketId) throws SQLException {
        String sql = "SELECT vip.ticket_type_id FROM tickets t " +
                     "JOIN ticket_types vip ON vip.concert_id = t.concert_id AND vip.name = 'VIP' " +
                     "WHERE t.ticket_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("ticket_type_id") : -1;
        }
    }

    private boolean isVipSeatAvailable(Connection conn, int ticketId, int seatId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM seats s " +
                     "JOIN tickets t ON t.ticket_id = ? " +
                     "JOIN concerts c ON c.concert_id = t.concert_id " +
                     "WHERE s.seat_id = ? AND s.venue_id = c.venue_id AND s.seat_type = 'VIP' " +
                     "AND s.seat_id NOT IN (SELECT seat_id FROM tickets WHERE concert_id = t.concert_id AND seat_id IS NOT NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            ps.setInt(2, seatId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("total") > 0;
        }
    }

    private int createOrder(Connection conn, int userId, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, 'PAID')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("Order id was not generated.");
        }
    }

    private void createPayment(Connection conn, int orderId, BigDecimal amount, String paymentData) throws SQLException {
        String sql = "INSERT INTO payments (order_id, amount, payment_method, payment_status, transaction_reference) " +
                     "VALUES (?, ?, 'CARD', 'SUCCESS', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, paymentData.trim());
            ps.executeUpdate();
        }
    }
}
