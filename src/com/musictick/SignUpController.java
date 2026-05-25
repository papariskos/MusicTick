package com.musictick;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUpController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private RadioButton userRadio;
    @FXML private RadioButton organizerRadio;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Label messageLabel;
    @FXML private Button returnButton;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/musictick?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML
    private void initialize() {
        if (returnButton != null) {
            returnButton.setVisible(false);
            returnButton.setManaged(false);
        }
    }

    @FXML
    private void handleSignUp() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Παρακαλώ συμπληρώστε όλα τα πεδία.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Οι κωδικοί δεν ταιριάζουν.");
            return;
        }

        String role = organizerRadio != null && organizerRadio.isSelected() ? "ORGANIZER" : "CUSTOMER";
        String insertSql = "INSERT INTO users (first_name, last_name, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, AuthUtils.hashPassword(password));
            ps.setString(5, role);
            ps.executeUpdate();

            openSuccessScreen();
        } catch (SQLException e) {
            e.printStackTrace();
            openFailureScreen("Αποτυχία εγγραφής. Ίσως το email υπάρχει ήδη καταχωρημένο.");
        }
    }

    private void openSuccessScreen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/success.fxml"));
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("MusicTick - Επιτυχία");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFailureScreen(String msg) {
        try {
            com.musictick.controller.BookingFailureController.failureMessage = msg;
            com.musictick.controller.BookingFailureController.backTargetFxml = "/signup.fxml";
            com.musictick.controller.BookingFailureController.backTargetTitle = "MusicTick - Register";
            Parent root = FXMLLoader.load(getClass().getResource("/booking_failure.fxml"));
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 550));
            stage.setTitle("MusicTick - Αποτυχία");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 500));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}