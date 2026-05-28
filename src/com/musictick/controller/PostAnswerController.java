package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.ForumManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import models.ForumPost;

/**
 * PostAnswerController - Controller για post_answer_screen.fxml.
 * Handles: Reply (Απάντηση) και Report (Αναφορά) για ένα επιλεγμένο post.
 */
public class PostAnswerController {

    public static ForumPost selectedPost;

    @FXML private Label postTitleLabel;
    @FXML private Label postContentLabel;
    @FXML private Label postStatusLabel;
    @FXML private TextArea replyArea;
    @FXML private TextArea reportReasonArea;

    @FXML
    public void initialize() {
        if (selectedPost != null) {
            if (postTitleLabel != null)
                postTitleLabel.setText(selectedPost.getTitle() != null ? selectedPost.getTitle() : "(Απάντηση σε post)");
            if (postContentLabel != null)
                postContentLabel.setText(selectedPost.getContent());
            if (postStatusLabel != null) {
                String locked = Boolean.TRUE.equals(selectedPost.getIsLocked()) ? "🔒 ΚΛΕΙΔΩΜΕΝΟ" : "🔓 Ανοιχτό";
                postStatusLabel.setText(locked);
            }
        }
    }

    // ── Reply ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleReply() {
        if (selectedPost == null) return;
        String content = replyArea != null ? replyArea.getText() : "";
        int userId = Session.getCurrentUserId();
        int concertId = ForumController.currentConcertId;

        String result = ForumManager.replyToPost(userId, concertId, selectedPost.getPostId(), content);
        switch (result) {
            case "success":
                openPage("/post_confirmation_screen.fxml", "MusicTick - Επιτυχία", 600, 400);
                break;
            case "locked":
                ErrorController.errorMessage = "Το νήμα είναι κλειδωμένο. Η απάντηση δεν επιτράπηκε.";
                openPage("/error_screen.fxml", "MusicTick - Κλειδωμένο", 600, 400);
                break;
            default:
                ErrorController.errorMessage = "Το κείμενο απάντησης δεν μπορεί να είναι κενό.";
                openPage("/error_screen.fxml", "MusicTick - Σφάλμα", 600, 400);
        }
    }

    // ── Report ─────────────────────────────────────────────────────────────────

    @FXML
    private void handleReport() {
        if (selectedPost == null) return;
        String reason = reportReasonArea != null ? reportReasonArea.getText() : "";
        int userId = Session.getCurrentUserId();

        String result = ForumManager.reportPost(userId, selectedPost.getPostId(), reason);
        if ("success".equals(result)) {
            openPage("/post_report_confirmation_screen.fxml", "MusicTick - Αναφορά Υποβλήθηκε", 600, 400);
        } else {
            ErrorController.errorMessage = "Ο λόγος αναφοράς δεν μπορεί να είναι κενός.";
            openPage("/error_screen.fxml", "MusicTick - Σφάλμα", 600, 400);
        }
    }

    @FXML
    private void goBack() {
        openPage("/forum_screen.fxml", "MusicTick - Forum", 800, 600);
    }

    private void openPage(String fxml, String title, int w, int h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
