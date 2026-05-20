package com.musictick;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private void handleConcerts() {
        System.out.println("Opening Concerts...");
        // Εδώ θα φορτώνεις το fxml των συναυλιών
    }

    @FXML
    private void handleTickets() {
        System.out.println("Opening My Tickets...");
    }

    @FXML
    private void handleForum() {
        System.out.println("Opening Forum...");
    }

    @FXML
    private void handleReport() {
        System.out.println("Opening Report Problem...");
    }

    @FXML
    private void handleLogout() {
        try {
            var resource = getClass().getResource("/login.fxml");
            if (resource == null) {
                System.err.println("Error: Could not find login.fxml");
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = Main.getPrimaryStage();
            if (stage != null) {
                stage.setScene(new Scene(root, 500, 500));
                stage.setTitle("MusicTick - Login");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
