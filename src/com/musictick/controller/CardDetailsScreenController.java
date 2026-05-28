package com.musictick.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class CardDetailsScreenController {

    @FXML
    private TextField cardDataField;

    @FXML
    private void submitCardDetails() {
        String data = cardDataField.getText();
        ManageCancellationController.validateCardDetails(data);
    }

    @FXML
    private void cancelRefund() {
        // Just go back to home or tickets
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/my_tickets.fxml"));
            javafx.stage.Stage stage = com.musictick.Main.getPrimaryStage();
            stage.setScene(new javafx.scene.Scene(root, 900, 600));
            stage.setTitle("MusicTick - My Tickets");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
