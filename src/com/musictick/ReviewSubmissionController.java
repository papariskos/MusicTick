package com.musictick;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ReviewSubmissionController {

    @FXML private TextField concertIdField;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private TextArea commentArea;
    @FXML private Label messageLabel;

    private final ReviewManager reviewManager = new ReviewManager();

    // Dynamic user ID from current session
    private final int currentUserId = Session.getCurrentUserId();

    @FXML
    public void initialize() {
        ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
    }

    @FXML
    private void submitReview() {
        try {
            int concertId = Integer.parseInt(concertIdField.getText().trim());
            Integer rating = ratingComboBox.getValue();
            String comment = commentArea.getText();

            if (rating == null) {
                messageLabel.setText("Επίλεξε βαθμολογία.");
                return;
            }

            String result = reviewManager.submitReview(currentUserId, concertId, rating, comment);
            messageLabel.setText(result);
        } catch (NumberFormatException e) {
            messageLabel.setText("Δώσε σωστό concert id.");
        }
    }

    @FXML
    private void goBack() {
        openPage("/review_display.fxml", "MusicTick - Reviews", 800, 550);
    }

    private void openPage(String fxml, String title, int width, int height) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
