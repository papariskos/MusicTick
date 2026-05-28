package com.musictick.controller;

import com.musictick.Main;
import com.musictick.manager.ForumManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import models.ForumPost;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PostManagementController - Controller για post_management_screen.fxml.
 * Διαχειριστής: Επιλέγει post και το Διαγράφει ή Κλειδώνει.
 */
public class PostManagementController {

    @FXML private ListView<String> postsListView;
    @FXML private Label statusLabel;

    private List<ForumPost> allPosts;

    @FXML
    public void initialize() {
        allPosts = ForumManager.getAllPosts().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .collect(Collectors.toList());

        List<String> items = allPosts.stream()
                .map(p -> "[#" + p.getPostId() + "] "
                        + (Boolean.TRUE.equals(p.getIsLocked()) ? "🔒 " : "")
                        + (p.getTitle() != null ? p.getTitle() : "(Απάντηση)")
                        + " — " + p.getContent())
                .collect(Collectors.toList());

        if (postsListView != null)
            postsListView.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    private void handleDeletePost() {
        int idx = getSelectedIndex();
        if (idx < 0) return;
        ForumPost post = allPosts.get(idx);
        String result = ForumManager.deletePost(com.musictick.Session.getCurrentUserId(), post.getPostId());
        if ("success".equals(result)) {
            if (statusLabel != null)
                statusLabel.setText("✅ Post #" + post.getPostId() + " διαγράφηκε. Alert εστάλη στον δημιουργό.");
            initialize(); // refresh
        } else {
            if (statusLabel != null) statusLabel.setText("❌ Το post δεν βρέθηκε.");
        }
    }

    @FXML
    private void handleLockThread() {
        int idx = getSelectedIndex();
        if (idx < 0) return;
        ForumPost post = allPosts.get(idx);
        String result = ForumManager.lockThread(com.musictick.Session.getCurrentUserId(), post.getPostId());
        switch (result) {
            case "success":
                if (statusLabel != null)
                    statusLabel.setText("🔒 Post #" + post.getPostId() + " κλειδώθηκε.");
                initialize();
                break;
            case "alreadyLocked":
                if (statusLabel != null)
                    statusLabel.setText("⚠️ Το post είναι ήδη κλειδωμένο.");
                break;
            default:
                if (statusLabel != null) statusLabel.setText("❌ Post δεν βρέθηκε.");
        }
    }

    private int getSelectedIndex() {
        if (postsListView == null) return -1;
        int idx = postsListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 && statusLabel != null) statusLabel.setText("Επιλέξτε ένα post πρώτα.");
        return idx;
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/forum_screen.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("MusicTick - Forum");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
