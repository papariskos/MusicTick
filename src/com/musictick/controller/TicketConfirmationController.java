package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TicketConfirmationController {
    @FXML private Label concertLabel;
    @FXML private Label seatLabel;
    @FXML private Label priceLabel;
    @FXML private Label orderIdLabel;

    @FXML
    public void initialize() {
        if (concertLabel != null) {
            concertLabel.setText(BookingController.confirmedConcertTitle);
        }
        if (seatLabel != null) {
            seatLabel.setText(BookingController.confirmedSeatDetails);
        }
        if (priceLabel != null) {
            priceLabel.setText(BookingController.confirmedPrice.toString() + " €");
        }
        if (orderIdLabel != null) {
            orderIdLabel.setText("#" + BookingController.confirmedOrderId);
        }


    }

    @FXML
    private void goToHome() {
        openPage("/user_home.fxml", "MusicTick - Home", 900, 600);
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
