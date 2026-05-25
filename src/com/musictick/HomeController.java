package com.musictick;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private TextField homeSearchField;

    @FXML
    private void handleHomeSearch() {
        if (homeSearchField == null)
            return;
        String terms = homeSearchField.getText().trim();
        if (terms.isEmpty())
            return;

        try {
            var searchManager = new com.musictick.manager.SearchManager();
            var results = searchManager.searchConcerts(terms);
            if (results.isEmpty()) {
                com.musictick.controller.BookingFailureController.failureMessage = "Δεν βρέθηκαν αποτελέσματα για: "
                        + terms;
                openPage("/booking_failure.fxml", "MusicTick - Αποτυχία", 800, 550);
            } else {
                com.musictick.controller.SearchController.lastSearchResults = results;
                openPage("/results.fxml", "MusicTick - Αποτελέσματα Αναζήτησης", 800, 550);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConcerts() {
        openPage("/search.fxml", "MusicTick - Search Concerts", 800, 550);
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

    @FXML
    private void handleTransferTicket() {
        openPage("/transfer_ticket.fxml", "MusicTick - Transfer Ticket", 800, 550);
    }

    @FXML
    private void handleNotifications() {
        openPage("/notifications.fxml", "MusicTick - Notifications", 800, 550);
    }

    private void openPage(String fxml, String title, int width, int height) {
        try {
            var resource = getClass().getResource(fxml);
            if (resource == null) {
                System.err.println("Error: Could not find " + fxml);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = Main.getPrimaryStage();

            if (stage != null) {
                stage.setScene(new Scene(root, width, height));
                stage.setTitle(title);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReviews() {
        openPage("/review_display.fxml", "MusicTick - Reviews", 800, 550);
    }

    @FXML
    private void handleVIPUpgrade() {
        openPage("/vip_upgrade.fxml", "MusicTick - VIP Upgrade", 800, 550);
    }
}
