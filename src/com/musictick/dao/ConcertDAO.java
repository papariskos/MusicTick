package com.musictick.dao;

import com.musictick.DBConfig;
import models.Concert;
import models.Seat;
import models.enums.ConcertStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcertDAO {

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    public List<Concert> searchConcerts(String terms) throws SQLException {
        List<Concert> concerts = new ArrayList<>();
        String sql = "SELECT * FROM concerts WHERE (title LIKE ? OR description LIKE ?) AND status = 'APPROVED'";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String queryTerm = "%" + terms + "%";
            ps.setString(1, queryTerm);
            ps.setString(2, queryTerm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    concerts.add(new Concert(
                            rs.getInt("concert_id"),
                            rs.getInt("organizer_id"),
                            rs.getInt("venue_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getTimestamp("concert_date").toLocalDateTime(),
                            ConcertStatus.valueOf(rs.getString("status")),
                            rs.getDouble("average_rating"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return concerts;
    }

    public Concert getConcertById(int id) throws SQLException {
        String sql = "SELECT * FROM concerts WHERE concert_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Concert(
                            rs.getInt("concert_id"),
                            rs.getInt("organizer_id"),
                            rs.getInt("venue_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getTimestamp("concert_date").toLocalDateTime(),
                            ConcertStatus.valueOf(rs.getString("status")),
                            rs.getDouble("average_rating"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    public List<Seat> getAvailableSeats(int concertId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT s.seat_id, s.venue_id, s.section_name, s.row_label, s.seat_number, s.seat_type " +
                     "FROM seats s " +
                     "JOIN concerts c ON s.venue_id = c.venue_id " +
                     "WHERE c.concert_id = ? " +
                     "AND s.seat_id NOT IN (" +
                     "  SELECT seat_id FROM tickets WHERE concert_id = ? AND seat_id IS NOT NULL AND status IN ('ACTIVE', 'UPGRADED', 'TEMPORARY')" +
                     ")";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concertId);
            ps.setInt(2, concertId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(new Seat(
                            rs.getInt("seat_id"),
                            rs.getInt("venue_id"),
                            rs.getString("section_name"),
                            rs.getString("row_label"),
                            rs.getString("seat_number"),
                            rs.getString("seat_type")
                    ));
                }
            }
        }
        return seats;
    }
}
