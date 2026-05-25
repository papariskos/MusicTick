package com.musictick.controller;

import com.musictick.Main;
import com.musictick.manager.SearchManager;
import models.Concert;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SearchController {
    @FXML private TextField searchField;
    @FXML private ListView<String> resultsListView;
    @FXML private Label messageLabel;

    private final SearchManager searchManager = new SearchManager();
    public static List<Concert> lastSearchResults = new ArrayList<>();
    public static Concert selectedConcert = null;

    @FXML
    public void initialize() {
        if (resultsListView != null && !lastSearchResults.isEmpty()) {
            List<String> listItems = new ArrayList<>();
            for (Concert c : lastSearchResults) {
                listItems.add(c.getConcertId() + " - " + c.getTitle() + " (" + c.getConcertDate().toLocalDate() + ")");
            }
            resultsListView.setItems(FXCollections.observableArrayList(listItems));
        }
    }

    @FXML
    private void handleSearch() {
        String terms = searchField.getText().trim();
        if (terms.isEmpty()) {
            messageLabel.setText("Συμπληρώστε όρους αναζήτησης.");
            return;
        }

        try {
            List<Concert> results = searchManager.searchConcerts(terms);
            if (results.isEmpty()) {
                // Open Failure Screen with "No results found"
                openFailureScreen("Δεν βρέθηκαν αποτελέσματα για: " + terms);
            } else {
                lastSearchResults = results;
                openPage("/results.fxml", "MusicTick - Αποτελέσματα Αναζήτησης", 800, 550);
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Σφάλμα κατά την αναζήτηση.");
        }
    }

    @FXML
    private void handleConcertSelection() {
        String selected = resultsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Επιλέξτε μια συναυλία.");
            return;
        }

        try {
            int concertId = Integer.parseInt(selected.split(" - ")[0].trim());
            for (Concert c : lastSearchResults) {
                if (c.getConcertId() == concertId) {
                    selectedConcert = c;
                    break;
                }
            }

            if (selectedConcert != null) {
                // Navigate to CheckAvailabilityController
                CheckAvailabilityController.checkAvailability(selectedConcert.getConcertId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Σφάλμα κατά την επιλογή συναυλίας.");
        }
    }

    @FXML
    private void goBack() {
        openPage("/user_home.fxml", "MusicTick - Home", 900, 600);
    }

    private void openFailureScreen(String msg) {
        BookingFailureController.failureMessage = msg;
        openPage("/booking_failure.fxml", "MusicTick - Αποτυχία", 800, 550);
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
