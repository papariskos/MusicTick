package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class BookingFailureController {
    @FXML private Label errorLabel;
    
    public static String backTargetFxml = "/search.fxml";
    public static String backTargetTitle = "MusicTick - Αναζήτηση";
    public static String failureMessage = "Παρουσιάστηκε σφάλμα.";

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setText(failureMessage);
        }
    }

    @FXML
    private void goBack() {
        openPage(backTargetFxml, backTargetTitle, 800, 550);
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
