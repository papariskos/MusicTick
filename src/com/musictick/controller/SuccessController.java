package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SuccessController {

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 500, 500));
            stage.setTitle("MusicTick - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
