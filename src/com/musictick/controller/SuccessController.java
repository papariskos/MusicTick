package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SuccessController {

    @FXML private Label titleLabel;
    @FXML private Label descLabel;
    @FXML private Button actionButton;

    // Public static variables for modular customization
    public static String titleText = "Επιτυχής Εγγραφή! 🎉";
    public static String descText = "Ο λογαριασμός σας δημιουργήθηκε επιτυχώς! Μπορείτε πλέον να συνδεθείτε στην εφαρμογή.";
    public static String buttonText = "ΣΥΝΔΕΣΗ (LOGIN) 🔑";
    public static String nextFxml = "/login.fxml";
    public static String nextTitle = "MusicTick - Login";
    public static int nextWidth = 500;
    public static int nextHeight = 500;

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.setText(titleText);
        }
        if (descLabel != null) {
            descLabel.setText(descText);
        }
        if (actionButton != null) {
            actionButton.setText(buttonText);
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(nextFxml));
            Stage stage = Main.getPrimaryStage();
            if (stage != null) {
                stage.setScene(new Scene(root, nextWidth, nextHeight));
                stage.setTitle(nextTitle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
