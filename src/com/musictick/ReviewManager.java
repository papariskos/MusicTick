package com.musictick;

import com.musictick.DBConfig;
import models.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewManager {

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    public List<Review> getReviewsForConcert(int concertId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT review_id, concert_id, user_id, rating, comment, created_at " +
                     "FROM reviews WHERE concert_id = ? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concertId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reviews.add(new Review(
                        rs.getInt("review_id"),
                        rs.getInt("concert_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return reviews;
    }

    public double getAverageRating(int concertId) throws SQLException {
        String sql = "SELECT COALESCE(average_rating, 0) AS average_rating FROM concerts WHERE concert_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concertId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("average_rating") : 0.0;
        }
    }

    public boolean canUserReview(int userId, int concertId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM tickets t " +
                     "WHERE t.user_id = ? AND t.concert_id = ? " +
                     "AND t.status IN ('ACTIVE', 'UPGRADED')";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, concertId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        }
    }

    public String submitReview(int userId, int concertId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            return "Η βαθμολογία πρέπει να είναι από 1 έως 5.";
        }
        if (comment == null || comment.trim().isEmpty()) {
            return "Το σχόλιο δεν μπορεί να είναι κενό.";
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            if (!canUserReview(userId, concertId)) {
                conn.rollback();
                return "Δεν μπορείς να κάνεις review για αυτή τη συναυλία.";
            }

            if (reviewAlreadyExists(conn, userId, concertId)) {
                conn.rollback();
                return "Έχεις ήδη κάνει review για αυτή τη συναυλία.";
            }

            String insertReview = "INSERT INTO reviews (concert_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertReview)) {
                ps.setInt(1, concertId);
                ps.setInt(2, userId);
                ps.setInt(3, rating);
                ps.setString(4, comment.trim());
                ps.executeUpdate();
            }

            updateAverageRating(conn, concertId);
            conn.commit();
            return "Το review καταχωρήθηκε επιτυχώς.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Σφάλμα κατά την καταχώρηση του review.";
        }
    }

    private boolean reviewAlreadyExists(Connection conn, int userId, int concertId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM reviews WHERE user_id = ? AND concert_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, concertId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("total") > 0;
        }
    }

    private void updateAverageRating(Connection conn, int concertId) throws SQLException {
        String sql = "UPDATE concerts SET average_rating = " +
                     "(SELECT AVG(rating) FROM reviews WHERE concert_id = ?) " +
                     "WHERE concert_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concertId);
            ps.setInt(2, concertId);
            ps.executeUpdate();
        }
    }
}
