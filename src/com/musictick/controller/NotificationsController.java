package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.dao.AlertDAO;
import models.Notification;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class NotificationsController {
    @FXML private ListView<Notification> alertsListView;
    @FXML private Label statusLabel;

    private final AlertDAO alertDAO = new AlertDAO();

    @FXML
    private void initialize() {
        setupCellFactory();
        loadAlerts();
    }

    /**
     * Custom cell factory so each notification shows:
     *   🔔 Title (bold)
     *   Message text
     */
    private void setupCellFactory() {
        if (alertsListView == null) return;
        alertsListView.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox box = new VBox(4);
                    box.setStyle("-fx-padding: 8 5 8 5;");

                    Label titleLbl = new Label("🔔 " + n.getTitle());
                    titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
                    titleLbl.setTextFill(Color.web("#00f3ff"));

                    Label msgLbl = new Label(n.getMessage());
                    msgLbl.setWrapText(true);
                    msgLbl.setMaxWidth(560);
                    msgLbl.setTextFill(Color.WHITE);
                    msgLbl.setFont(Font.font("System", 12));

                    String typeColor = typeColor(n.getType());
                    Label typeLbl = new Label("[" + (n.getType() != null ? n.getType() : "INFO") + "]");
                    typeLbl.setTextFill(Color.web(typeColor));
                    typeLbl.setFont(Font.font("System", FontWeight.BOLD, 11));

                    box.getChildren().addAll(titleLbl, msgLbl, typeLbl);
                    setGraphic(box);
                    setText(null);
                    setStyle("-fx-background-color: #1e2a42; -fx-background-radius: 6;");
                }
            }

            private String typeColor(String type) {
                if (type == null) return "#94a3b8";
                switch (type.toUpperCase()) {
                    case "ALERT":    return "#ff007f";
                    case "REFUND":   return "#2ecc71";
                    case "TRANSFER": return "#f39c12";
                    default:         return "#94a3b8";
                }
            }
        });
    }

    @FXML
    private void checkNewAlerts() {
        loadAlerts();
    }

    private void loadAlerts() {
        int userId = Session.getCurrentUserId();
        
        // Trigger the Notification Sequence Diagram Flow (NotificationManager -> AlertList -> IdentityManager -> RegisteredList -> AlertScreens / FailureScreen)
        try {
            new com.musictick.manager.NotificationManager().init(userId);
        } catch (Exception ex) {
            System.err.println("NotificationsController: sequence diagram flow failed: " + ex.getMessage());
        }

        List<Notification> alerts = new ArrayList<>();

        // 1. Try DB
        try {
            alerts = alertDAO.returnAlerts(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (alertsListView != null) {
            alertsListView.setItems(FXCollections.observableArrayList(alerts));
        }

        if (statusLabel != null) {
            if (alerts.isEmpty()) {
                statusLabel.setText("Δεν υπάρχουν ειδοποιήσεις για τον λογαριασμό σας.");
                statusLabel.setStyle("-fx-text-fill: #94a3b8;");
            } else {
                statusLabel.setText("Βρέθηκαν " + alerts.size() + " ειδοποιήσεις για User #" + userId + ".");
                statusLabel.setStyle("-fx-text-fill: #2ecc71;");
            }
        }
    }

    @FXML
    private void deleteSelectedAlert() {
        Notification selected = alertsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (statusLabel != null) {
                statusLabel.setText("Παρακαλώ επιλέξτε μια ειδοποίηση για διαγραφή.");
                statusLabel.setStyle("-fx-text-fill: #ff007f;");
            }
            return;
        }

        try {
            alertDAO.deleteAlert(selected.getNotificationId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadAlerts();
        if (statusLabel != null) {
            statusLabel.setText("Η ειδοποίηση διαγράφηκε με επιτυχία!");
            statusLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
    }

    @FXML
    private void goBack() {
        try {
            String fxml = "/user_home.fxml";
            int width = 900, height = 600;
            String role = Session.getCurrentUserRole();
            if ("ORGANIZER".equals(role)) { fxml = "/organizer_home.fxml"; width = 800; }
            else if ("ADMIN".equals(role)) { fxml = "/admin_home.fxml"; width = 850; }
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, width, height));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
