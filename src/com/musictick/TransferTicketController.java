package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.dao.TicketDAO;
import com.musictick.manager.TransferTicketManager;
import com.musictick.model.Ticket;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TransferTicketController {
    @FXML private ComboBox<Ticket> ticketComboBox;
    @FXML private TextField recipientEmailField;
    @FXML private Label messageLabel;

    private final TicketDAO ticketDAO = new TicketDAO();
    private final TransferTicketManager transferManager = new TransferTicketManager();

    @FXML
    private void initialize() {
        loadUserTickets();
    }

    private void loadUserTickets() {
        try {
            ticketComboBox.setItems(FXCollections.observableArrayList(ticketDAO.findUserTickets(Session.getCurrentUserId())));
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Αποτυχία φόρτωσης εισιτηρίων.");
        }
    }

    @FXML
    private void submitRecipientData() {
        Ticket selectedTicket = ticketComboBox.getValue();
        String recipientEmail = recipientEmailField.getText().trim();

        if (selectedTicket == null) {
            messageLabel.setText("Επίλεξε εισιτήριο.");
            return;
        }
        if (recipientEmail.isEmpty()) {
            messageLabel.setText("Συμπλήρωσε email παραλήπτη.");
            return;
        }

        TransferTicketManager.TransferResult result = transferManager.transferTicket(
                selectedTicket.getTicketId(), Session.getCurrentUserId(), recipientEmail
        );
        messageLabel.setText(result.message);
        if (result.success) {
            recipientEmailField.clear();
            loadUserTickets();
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
