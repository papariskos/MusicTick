package com.musictick;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class CancelConcertController {

    private ConcertManager concertManager = new ConcertManager();

    @FXML private TextField concertIdInput;

    @FXML
    private void handleCancelConcert() {
        try {
            String input = concertIdInput.getText();
            if (input == null || input.isEmpty()) {
                showAlert("Error", "Please enter a valid Concert ID.", Alert.AlertType.ERROR);
                return;
            }

            Integer concertId = Integer.parseInt(input);
            boolean result = concertManager.cancelConcertAndRefund(concertId);
            
            if (result) {
                showAlert("Success", "Concert cancelled and refunds initiated.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Could not find concert with that ID.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "ID must be a number.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
