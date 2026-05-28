package com.musictick.manager;

import com.musictick.DBConfig;
import models.ForumPost;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

/**
 * ForumManager - κεντρικός ενορχηστρωτής Forum use cases.
 * Αντιστοιχεί στον participant ForumManager/PostList του Sequence Diagram.
 */
public class ForumManager {

    private static Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    // ── Use Case: View Forum ──────────────────────────────────────────────────

    /** Επιστρέφει όλα τα top-level posts (threads) για μια συναυλία. */
    public static List<ForumPost> getPostsByConcert(int concertId) {
        System.out.println("ForumManager: getPostsByConcert() called for concertId=" + concertId);
        List<ForumPost> result = new ArrayList<>();
        String sql = "SELECT * FROM forum_posts WHERE concert_id = ? AND parent_post_id IS NULL AND is_deleted = FALSE ORDER BY created_at ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concertId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer pid = rs.getInt("post_id");
                    Integer parentId = rs.getObject("parent_post_id") != null ? rs.getInt("parent_post_id") : null;
                    result.add(new ForumPost(
                        pid,
                        rs.getInt("concert_id"),
                        rs.getInt("user_id"),
                        parentId,
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getBoolean("is_locked"),
                        rs.getBoolean("is_deleted"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("PostList: returnPostList() -> " + result.size() + " posts");
        return result;
    }

    /** Επιστρέφει τις απαντήσεις ενός post. */
    public static List<ForumPost> getReplies(int parentPostId) {
        System.out.println("ForumManager: getReplies() called for parentPostId=" + parentPostId);
        List<ForumPost> result = new ArrayList<>();
        String sql = "SELECT * FROM forum_posts WHERE parent_post_id = ? AND is_deleted = FALSE ORDER BY created_at ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentPostId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer pid = rs.getInt("post_id");
                    Integer parentId = rs.getObject("parent_post_id") != null ? rs.getInt("parent_post_id") : null;
                    result.add(new ForumPost(
                        pid,
                        rs.getInt("concert_id"),
                        rs.getInt("user_id"),
                        parentId,
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getBoolean("is_locked"),
                        rs.getBoolean("is_deleted"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ── Use Case: New Post (Ανάρτηση) ────────────────────────────────────────

    /**
     * Δημιουργεί νέο post.
     * @return "success" ή "emptyFields" ή "notRegistered"
     */
    public static String createPost(int userId, int concertId, String title, String content) {
        System.out.println("ForumManager: createPost() called by userId=" + userId);

        // Έλεγχος εγγεγραμμένου (RegisteredList)
        String reg = RegisteredList.findRecipient(userId);
        System.out.println("ForumManager: checkRegistration() -> " + reg);

        // Έλεγχος κενών πεδίων
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            System.out.println("ForumManager: [emptyFields] -> displayErrorScreen()");
            return "emptyFields";
        }

        int generatedPostId = -1;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO forum_posts (concert_id, user_id, title, content) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, userId);
                    ps.setString(3, title.trim());
                    ps.setString(4, content.trim());
                    ps.executeUpdate();
                    
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedPostId = keys.getInt(1);
                        }
                    }
                }

                if (generatedPostId != -1) {
                    String sqlRep = "INSERT INTO post_reports (post_id, user_id, reason) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlRep)) {
                        ps.setInt(1, generatedPostId);
                        ps.setInt(2, userId);
                        ps.setString(3, "Νέο Forum Thread: " + title);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }

        System.out.println("ForumManager: savePost() -> postSaved() post#" + generatedPostId);
        return "success";
    }

    // ── Use Case: Reply (Απάντηση σε post) ───────────────────────────────────

    /**
     * Δημιουργεί απάντηση σε post.
     * @return "success" ή "emptyFields", "locked"
     */
    public static String replyToPost(int userId, int concertId, int parentPostId, String content) {
        System.out.println("ForumManager: replyToPost() called by userId=" + userId + " for parentPostId=" + parentPostId);

        if (content == null || content.trim().isEmpty()) {
            System.out.println("ForumManager: [emptyFields] -> displayErrorScreen()");
            return "emptyFields";
        }

        // Check parent not locked
        ForumPost parent = findById(parentPostId);
        if (parent != null && Boolean.TRUE.equals(parent.getIsLocked())) {
            System.out.println("ForumManager: [lockedThread] -> displayFailureScreen()");
            return "locked";
        }

        int generatedPostId = -1;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO forum_posts (concert_id, user_id, parent_post_id, content) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, concertId);
                    ps.setInt(2, userId);
                    ps.setInt(3, parentPostId);
                    ps.setString(4, content.trim());
                    ps.executeUpdate();
                    
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedPostId = keys.getInt(1);
                        }
                    }
                }

                if (generatedPostId != -1) {
                    String sqlRep = "INSERT INTO post_reports (post_id, user_id, reason) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlRep)) {
                        ps.setInt(1, generatedPostId);
                        ps.setInt(2, userId);
                        ps.setString(3, "Νέα Απάντηση στο θέμα #" + parentPostId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }

        System.out.println("ForumManager: saveAnswer() -> answerSaved() reply#" + generatedPostId);
        return "success";
    }

    // ── Use Case: Report Post (Αναφορά) ──────────────────────────────────────

    /**
     * Αναφορά post σε διαχειριστή.
     * @return "success" ή "emptyFields"
     */
    public static String reportPost(int reporterUserId, int postId, String reason) {
        System.out.println("ForumManager: reportPost() called by userId=" + reporterUserId + " for postId=" + postId);

        if (reason == null || reason.trim().isEmpty()) {
            System.out.println("ForumManager: [emptyReason] -> displayErrorScreen()");
            return "emptyFields";
        }

        System.out.println("ForumManager: saveReport() called for postId=" + postId + " reason=" + reason);

        try {
            // Save alert/notification directly in DB
            String sql = "INSERT INTO post_reports (post_id, user_id, reason) VALUES (?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.setInt(2, reporterUserId);
                ps.setString(3, reason.trim());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("ForumManager: alertAdministrator() -> Saved directly to post_reports DB table.");
        return "success";
    }

    // ── Admin Use Case: Delete / Lock Post ───────────────────────────────────

    /**
     * Διαγραφή post (soft delete).
     * @return "success" ή "notFound"
     */
    public static String deletePost(int adminUserId, int postId) {
        System.out.println("ForumManager: deletePost() called by adminUserId=" + adminUserId + " for postId=" + postId);
        ForumPost post = findById(postId);
        if (post == null) return "notFound";

        try (Connection conn = getConnection()) {
            String sql = "UPDATE forum_posts SET is_deleted = TRUE WHERE post_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();
            }
            
            // Send Alert notification to creator in DB
            new com.musictick.dao.AlertDAO().saveAlert(post.getUserId(), "Post Διαγράφηκε", "Το post σας #" + postId + " διαγράφηκε από διαχειριστή.", "ALERT");
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    /**
     * Κλείδωμα νήματος.
     * @return "success", "alreadyLocked" ή "notFound"
     */
    public static String lockThread(int adminUserId, int postId) {
        System.out.println("ForumManager: lockThread() called by adminUserId=" + adminUserId + " for postId=" + postId);
        ForumPost post = findById(postId);
        if (post == null) return "notFound";
        if (Boolean.TRUE.equals(post.getIsLocked())) {
            System.out.println("ForumManager: checkStatus() -> [locked] -> displayFailureScreen()");
            return "alreadyLocked";
        }

        try (Connection conn = getConnection()) {
            String sql = "UPDATE forum_posts SET is_locked = TRUE WHERE post_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
        System.out.println("ForumManager: lockPost() -> PostList updated");
        System.out.println("ForumManager: checkStatus() -> Locked");
        return "success";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static ForumPost findById(int postId) {
        String sql = "SELECT * FROM forum_posts WHERE post_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer parentId = rs.getObject("parent_post_id") != null ? rs.getInt("parent_post_id") : null;
                    return new ForumPost(
                        rs.getInt("post_id"),
                        rs.getInt("concert_id"),
                        rs.getInt("user_id"),
                        parentId,
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getBoolean("is_locked"),
                        rs.getBoolean("is_deleted"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ForumPost> getAllPosts() {
        System.out.println("ForumManager: getAllPosts() called");
        List<ForumPost> result = new ArrayList<>();
        String sql = "SELECT * FROM forum_posts ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer pid = rs.getInt("post_id");
                Integer parentId = rs.getObject("parent_post_id") != null ? rs.getInt("parent_post_id") : null;
                result.add(new ForumPost(
                    pid,
                    rs.getInt("concert_id"),
                    rs.getInt("user_id"),
                    parentId,
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getBoolean("is_locked"),
                    rs.getBoolean("is_deleted"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void resetForTesting() {
        try (Connection conn = getConnection()) {
            conn.createStatement().executeUpdate("DELETE FROM post_reports");
            conn.createStatement().executeUpdate("DELETE FROM forum_posts");
            conn.createStatement().executeUpdate("ALTER TABLE forum_posts AUTO_INCREMENT = 1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
