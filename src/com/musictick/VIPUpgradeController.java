package com.musictick;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class VIPUpgradeController {

    @FXML private ComboBox<String> ticketComboBox;
    @FXML private ComboBox<String> vipSeatComboBox;
    @FXML private Label amountLabel;
    @FXML private TextField paymentDataField;
    @FXML private Label messageLabel;

    private final VIPUpgradeManager vipUpgradeManager = new VIPUpgradeManager();

    // Προσωρινό μέχρι να συνδεθεί με login/session.
    private final int currentUserId = 1;

    @FXML
    public void initialize() {
        loadTickets();
        ticketComboBox.setOnAction(event -> loadVipSeatsAndAmount());
    }

    private void loadTickets() {
        try {
            List<String> tickets = vipUpgradeManager.getUserActiveTickets(currentUserId);
            ticketComboBox.getItems().setAll(tickets);
            if (tickets.isEmpty()) {
                messageLabel.setText("Δεν υπάρχουν ενεργά εισιτήρια για upgrade.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Σφάλμα κατά τη φόρτωση εισιτηρίων.");
        }
    }

    private void loadVipSeatsAndAmount() {
        Integer ticketId = getSelectedId(ticketComboBox);
        if (ticketId == null) return;

        try {
            List<String> seats = vipUpgradeManager.getAvailableVipSeatsForTicket(ticketId);
            vipSeatComboBox.getItems().setAll(seats);

            BigDecimal amount = vipUpgradeManager.calculateExtraAmount(ticketId);
            amountLabel.setText("Extra ποσό: " + amount + " €");

            if (seats.isEmpty()) {
                messageLabel.setText("Δεν υπάρχουν διαθέσιμες VIP θέσεις.");
            } else {
                messageLabel.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Σφάλμα κατά τη φόρτωση VIP θέσεων.");
        }
    }

    @FXML
    private void submitUpgrade() {
        Integer ticketId = getSelectedId(ticketComboBox);
        Integer seatId = getSelectedId(vipSeatComboBox);

        if (ticketId == null) {
            messageLabel.setText("Επίλεξε εισιτήριο.");
            return;
        }
        if (seatId == null) {
            messageLabel.setText("Επίλεξε VIP θέση.");
            return;
        }

        String result = vipUpgradeManager.upgradeTicketToVIP(
                currentUserId,
                ticketId,
                seatId,
                paymentDataField.getText()
        );
        messageLabel.setText(result);
    }

    @FXML
    private void goBack() {
        openPage("/user_home.fxml", "MusicTick - Home", 700, 500);
    }

    private Integer getSelectedId(ComboBox<String> comboBox) {
        String selected = comboBox.getValue();
        if (selected == null || selected.isBlank()) return null;
        try {
            return Integer.parseInt(selected.split(" - ")[0].trim());
        } catch (Exception e) {
            return null;
        }
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
