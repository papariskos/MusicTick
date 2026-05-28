package com.musictick;

import com.musictick.DBConfig;
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

    private static final String DB_URL = DBConfig.DB_URL;
    private static final String DB_USER = DBConfig.DB_USER;
    private static final String DB_PASSWORD = DBConfig.DB_PASSWORD;

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
        
        // Check if user already exists (Alternative Flow matching Use Case Analysis)
        boolean exists = false;
        boolean dbOffline = false;
        
        String checkSql = "SELECT COUNT(*) AS total FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, email);
            try (var rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("total") > 0) {
                    exists = true;
                }
            }
        } catch (SQLException e) {
            dbOffline = true;
            System.err.println("SignUpController: DB offline during check. Checking mock list.");
            if (email.equalsIgnoreCase("user@musictick.com") || 
                email.equalsIgnoreCase("organizer@musictick.com") || 
                email.equalsIgnoreCase("admin@musictick.com")) {
                exists = true;
            }
        }

        if (exists) {
            openFailureScreen("Σφάλμα: Ο χρήστης με email '" + email + "' υπάρχει ήδη εγγεγραμμένος.");
            return;
        }

        String insertSql = "INSERT INTO users (first_name, last_name, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, AuthUtils.hashPassword(password));
            ps.setString(5, role);
            ps.executeUpdate();

            com.musictick.controller.SuccessController.titleText = "Η Εγγραφή Ολοκληρώθηκε! 🎉";
            com.musictick.controller.SuccessController.descText = "Ο λογαριασμός σας '" + email + "' δημιουργήθηκε επιτυχώς.";
            com.musictick.controller.SuccessController.buttonText = "ΣΥΝΔΕΣΗ 🔑";
            com.musictick.controller.SuccessController.nextFxml = "/login.fxml";
            com.musictick.controller.SuccessController.nextTitle = "MusicTick - Login";
            com.musictick.controller.SuccessController.nextWidth = 500;
            com.musictick.controller.SuccessController.nextHeight = 500;

            openSuccessScreen();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database is offline. Falling back to Simulated Offline SignUp.");
            com.musictick.controller.SuccessController.titleText = "Η Εγγραφή Ολοκληρώθηκε! 🎉";
            com.musictick.controller.SuccessController.descText = "Ο λογαριασμός σας '" + email + "' δημιουργήθηκε επιτυχώς (Offline Demo Mode).";
            com.musictick.controller.SuccessController.buttonText = "ΣΥΝΔΕΣΗ 🔑";
            com.musictick.controller.SuccessController.nextFxml = "/login.fxml";
            com.musictick.controller.SuccessController.nextTitle = "MusicTick - Login";
            com.musictick.controller.SuccessController.nextWidth = 500;
            com.musictick.controller.SuccessController.nextHeight = 500;
            openSuccessScreen();
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