package com.musictick;

import com.musictick.AuthUtils;
import com.musictick.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/musictick?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Παρακαλώ συμπληρώστε όλα τα πεδία.");
            return;
        }

        try {
            if (email.equals("admin") && password.equals("admin")) {
                Session.setCurrentUserId(0);
                Session.setCurrentUserRole("ADMIN");
                openHomePage();
                return;
            }

            String sql = "SELECT user_id, password_hash, role FROM users WHERE email = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    errorLabel.setText("Λάθος email ή κωδικός.");
                    return;
                }

                String storedHash = rs.getString("password_hash");
                String providedHash = AuthUtils.hashPassword(password);

                if (storedHash != null && storedHash.equals(providedHash)) {
                    Session.setCurrentUserId(rs.getInt("user_id"));
                    Session.setCurrentUserRole(rs.getString("role"));
                    openHomePage();
                } else {
                    errorLabel.setText("Λάθος email ή κωδικός.");
                }
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
            errorLabel.setText("Σφάλμα σύνδεσης στη βάση.");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Σφάλμα ανοίγματος της σελίδας.");
        }
    }

    private void openHomePage() throws Exception {
        String fxml = "/user_home.fxml";
        if ("ORGANIZER".equals(Session.getCurrentUserRole())) {
            fxml = "/organizer_home.fxml";
        }

        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
    }

    @FXML
    private void goToSignUp() {
        try {
            var resource = getClass().getResource("/signup.fxml");
            if (resource == null) {
                System.err.println("Error: Could not find signup.fxml");
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 500));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
