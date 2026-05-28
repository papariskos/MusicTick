package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CancellationScreenController {

    @FXML
    private void proceedToReservationScreen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/reservation_screen.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("MusicTick - Enter Reservation Data");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/my_tickets.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("MusicTick - My Tickets");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
