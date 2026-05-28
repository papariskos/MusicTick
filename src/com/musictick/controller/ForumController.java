package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.ForumManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import models.ForumPost;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ForumController - Controller για το forum_screen.fxml (ForumScreen).
 * Εμφανίζει τα posts της επιλεγμένης συναυλίας και επιτρέπει:
 * - Νέα Ανάρτηση (selectNewPost)
 * - Επιλογή Post για Reply/Report (selectPost)
 */
public class ForumController {

    public static int currentConcertId = 1; // Set by previous screen (concert details)

    @FXML private Label forumTitleLabel;
    @FXML private ListView<String> postsListView;
    @FXML private Label statusLabel;
    @FXML private Button adminManageButton;

    // Internal map: display index -> post id
    private List<ForumPost> displayedPosts;

    @FXML
    public void initialize() {
        if (forumTitleLabel != null) {
            forumTitleLabel.setText("Forum Συναυλίας #" + currentConcertId);
        }
        // Show admin button only if admin
        if (adminManageButton != null) {
            boolean isAdmin = Session.getCurrentUserRole() != null &&
                    Session.getCurrentUserRole().equalsIgnoreCase("ADMIN");
            adminManageButton.setVisible(isAdmin);
            adminManageButton.setManaged(isAdmin);
        }
        loadPosts();
    }

    private void loadPosts() {
        displayedPosts = ForumManager.getPostsByConcert(currentConcertId);
        if (postsListView != null) {
            List<String> items = displayedPosts.stream()
                    .map(p -> "[#" + p.getPostId() + "] " + p.getTitle() + " — " + p.getContent())
                    .collect(Collectors.toList());
            if (items.isEmpty()) {
                items.add("Δεν υπάρχουν αναρτήσεις ακόμα. Γίνε ο πρώτος!");
            }
            postsListView.setItems(FXCollections.observableArrayList(items));
        }
    }

    @FXML
    private void handleNewPost() {
        openPage("/post_creation_screen.fxml", "MusicTick - Νέα Ανάρτηση", 700, 500);
    }

    @FXML
    private void handleSelectPost() {
        if (postsListView == null || displayedPosts == null) return;
        int idx = postsListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= displayedPosts.size()) {
            if (statusLabel != null) statusLabel.setText("Επιλέξτε ένα post πρώτα.");
            return;
        }
        PostAnswerController.selectedPost = displayedPosts.get(idx);
        openPage("/post_answer_screen.fxml", "MusicTick - Απάντηση / Αναφορά", 700, 550);
    }

    @FXML
    private void handleAdminManage() {
        openPage("/post_management_screen.fxml", "MusicTick - Διαχείριση Forum", 800, 600);
    }

    @FXML
    private void goBack() {
        openPage("/user_home.fxml", "MusicTick - Home", 900, 600);
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
