package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.WaitlistManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class BookingFailureController {
    @FXML private Label errorLabel;
    @FXML private Button waitlistButton;
    
    public static String backTargetFxml = "/search.fxml";
    public static String backTargetTitle = "MusicTick - Αναζήτηση";
    public static String failureMessage = "Παρουσιάστηκε σφάλμα.";
    public static int currentConcertId = -1;

    private final WaitlistManager waitlistManager = new WaitlistManager();

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setText(failureMessage);
        }
        
        // Show waitlist button only if there are no available seats and a concert is set
        if (waitlistButton != null) {
            if (currentConcertId != -1 && "Δεν υπάρχουν διαθέσιμες θέσεις για αυτή τη συναυλία.".equals(failureMessage)) {
                waitlistButton.setVisible(true);
                waitlistButton.setManaged(true);
            } else {
                waitlistButton.setVisible(false);
                waitlistButton.setManaged(false);
            }
        }
    }

    @FXML
    private void goBack() {
        // Reset concert ID when going back
        currentConcertId = -1;
        openPage(backTargetFxml, backTargetTitle, 800, 550);
    }

    @FXML
    private void joinWaitlist() {
        openPage("/waitlist_payment_screen.fxml", "MusicTick - Προέγκριση Κάρτας", 800, 550);
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
