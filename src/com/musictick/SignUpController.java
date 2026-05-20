package com.musictick;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    @FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        System.out.println("User registered: " + username);
        goToLogin();
    }

    @FXML
    private void goToLogin() {
        try {
            var resource = getClass().getResource("/login.fxml");
            if (resource == null) {
                System.err.println("Error: Could not find login.fxml");
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 500));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}