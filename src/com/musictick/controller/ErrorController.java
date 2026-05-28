package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ErrorController {
    @FXML private Label errorLabel;

    public static String errorMessage = "Παρουσιάστηκε σφάλμα κατά την πληρωμή.";

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setText(errorMessage);
        }
    }

    @FXML
    private void goBack() {
        // Go back to the payment screen to try again
        openPage("/booking_screen.fxml", "MusicTick - Στοιχεία Πληρωμής", 800, 550);
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
