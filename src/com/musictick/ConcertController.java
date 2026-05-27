package com.musictick;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDateTime;

public class ConcertController {

    private ConcertManager concertManager = new ConcertManager();

    // Πεδία για το "New Concert"
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private TextField venueIdField;
    @FXML private Label statusLabel;

    // Use Case: New Concert
    @FXML
    private void handleSaveConcert() {
        try {
            if (datePicker.getValue() == null) {
                statusLabel.setText("Please select a date.");
                return;
            }
            
            String title = titleField.getText();
            String desc = descriptionArea.getText();
            LocalDateTime date = datePicker.getValue().atStartOfDay();
            Integer venueId = Integer.parseInt(venueIdField.getText());
            

            boolean success = concertManager.createConcert(title, desc, date, venueId, 1);
            
            if (success) {
                statusLabel.setText("Concert saved successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Failed to save concert. Check inputs.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: Venue ID must be a number.");
        } catch (Exception e) {
            statusLabel.setText("Error: Invalid data format.");
        }
    }
}
