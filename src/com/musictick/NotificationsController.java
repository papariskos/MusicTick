package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.NotificationManager;
import com.musictick.model.Alert;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class NotificationsController {
    @FXML private ListView<Alert> alertsListView;
    @FXML private Label statusLabel;

    private final NotificationManager notificationManager = new NotificationManager();

    @FXML
    private void initialize() {
        checkNewAlerts();
    }

    @FXML
    private void checkNewAlerts() {
        try {
            List<Alert> alerts = notificationManager.checkNewAlerts(Session.getCurrentUserId());
            alertsListView.setItems(FXCollections.observableArrayList(alerts));
            statusLabel.setText(alerts.isEmpty() ? "Δεν υπάρχουν ειδοποιήσεις." : "Βρέθηκαν " + alerts.size() + " ειδοποιήσεις.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Αποτυχία φόρτωσης ειδοποιήσεων.");
        }
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_home.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
