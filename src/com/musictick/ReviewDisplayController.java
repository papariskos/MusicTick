package com.musictick;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Review;

import java.util.List;

public class ReviewDisplayController {

    @FXML private TextField concertIdField;
    @FXML private Label averageRatingLabel;
    @FXML private ListView<String> reviewListView;
    @FXML private Label messageLabel;

    private final ReviewManager reviewManager = new ReviewManager();

    @FXML
    private void showReviews() {
        try {
            int concertId = Integer.parseInt(concertIdField.getText().trim());
            double averageRating = reviewManager.getAverageRating(concertId);
            List<Review> reviews = reviewManager.getReviewsForConcert(concertId);

            averageRatingLabel.setText("Μέση βαθμολογία: " + String.format("%.2f", averageRating));
            reviewListView.getItems().clear();

            for (Review review : reviews) {
                reviewListView.getItems().add(
                        "User " + review.getUserId() + " | " + review.getRating() + "/5\n" + review.getComment()
                );
            }

            if (reviews.isEmpty()) {
                messageLabel.setText("Δεν υπάρχουν reviews για αυτή τη συναυλία.");
            } else {
                messageLabel.setText("");
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Δώσε σωστό concert id.");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Σφάλμα κατά τη φόρτωση των reviews.");
        }
    }

    @FXML
    private void openSubmitReview() {
        openPage("/review_submission.fxml", "MusicTick - Submit Review", 700, 500);
    }

    @FXML
    private void goBack() {
        openPage("/user_home.fxml", "MusicTick - Home", 700, 500);
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
