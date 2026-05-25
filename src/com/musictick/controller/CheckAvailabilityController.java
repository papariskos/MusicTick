package com.musictick.controller;

import com.musictick.Main;
import com.musictick.manager.SearchManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CheckAvailabilityController {
    private static final SearchManager searchManager = new SearchManager();

    public static void checkAvailability(int concertId) {
        try {
            boolean available = searchManager.checkAvailability(concertId);
            if (available) {
                // Navigate to SeatingMap Screen
                openPage("/seating_map.fxml", "MusicTick - Επιλογή Θέσης", 800, 550);
            } else {
                // Navigate to Failure Screen
                BookingFailureController.failureMessage = "Δεν υπάρχουν διαθέσιμες θέσεις για αυτή τη συναυλία.";
                openPage("/booking_failure.fxml", "MusicTick - Μη Διαθέσιμο", 800, 550);
            }
        } catch (Exception e) {
            e.printStackTrace();
            BookingFailureController.failureMessage = "Σφάλμα κατά τον έλεγχο διαθεσιμότητας.";
            openPage("/booking_failure.fxml", "MusicTick - Σφάλμα", 800, 550);
        }
    }

    private static void openPage(String fxml, String title, int width, int height) {
        try {
            Parent root = FXMLLoader.load(CheckAvailabilityController.class.getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
