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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * PostCreationController - Controller για το post_creation_screen.fxml.
 * Handles: provideConcertData -> savePost -> ConfirmationScreen / ErrorScreen
 */
public class PostCreationController {

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Label messageLabel;

    @FXML
    private void handleSubmitPost() {
        String title = titleField != null ? titleField.getText() : "";
        String content = contentArea != null ? contentArea.getText() : "";
        int userId = Session.getCurrentUserId();
        int concertId = ForumController.currentConcertId;

        String result = ForumManager.createPost(userId, concertId, title, content);

        if ("success".equals(result)) {
            openPage("/post_confirmation_screen.fxml", "MusicTick - Επιτυχία", 600, 400);
        } else {
            // emptyFields -> ErrorScreen
            ErrorController.errorMessage = "Τα πεδία τίτλος και κείμενο δεν πρέπει να είναι κενά.";
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
