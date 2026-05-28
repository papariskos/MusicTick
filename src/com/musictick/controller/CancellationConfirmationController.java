package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CancellationConfirmationController {

    @FXML
    private void goHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_home.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("MusicTick - Home");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
