package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.NotificationManager;
import models.Notification;
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
    @FXML private ListView<Notification> alertsListView;
    @FXML private Label statusLabel;

    private final NotificationManager notificationManager = new NotificationManager();

    @FXML
    private void initialize() {
        checkNewAlerts();
    }

    @FXML
    private void checkNewAlerts() {
        try {
            List<Notification> alerts = notificationManager.checkNewAlerts(Session.getCurrentUserId());
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
            String fxml = "/user_home.fxml";
            int width = 900;
            int height = 600;
            if ("ORGANIZER".equals(Session.getCurrentUserRole())) {
                fxml = "/organizer_home.fxml";
                width = 800;
                height = 600;
            } else if ("ADMIN".equals(Session.getCurrentUserRole())) {
                fxml = "/admin_home.fxml";
                width = 850;
                height = 600;
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, width, height));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
