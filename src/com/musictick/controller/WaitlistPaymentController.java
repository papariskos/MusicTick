package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.WaitlistManager;
import models.Concert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class WaitlistPaymentController {

    @FXML private Label concertLabel;
    @FXML private Label priceLabel;
    @FXML private TextField cardDetailsField;
    @FXML private Label statusLabel;

    private final WaitlistManager waitlistManager = new WaitlistManager();

    @FXML
    public void initialize() {
        Concert selectedConcert = SearchController.selectedConcert;
        if (selectedConcert != null) {
            concertLabel.setText(selectedConcert.getTitle());
        }
        priceLabel.setText("35.00 €"); // Default regular price
    }

    @FXML
    private void handleSubmit() {
        String cardData = cardDetailsField.getText().trim();
        if (cardData.isEmpty()) {
            statusLabel.setText("Συμπληρώστε τα στοιχεία της κάρτας σας.");
            return;
        }

        if (cardData.toUpperCase().contains("FAIL") || cardData.toUpperCase().contains("ERROR")) {
            statusLabel.setText("Σφάλμα: Η προέγκριση της κάρτας απέτυχε. Δοκιμάστε άλλη κάρτα.");
            return;
        }

        Concert selectedConcert = SearchController.selectedConcert;
        if (selectedConcert == null) {
            statusLabel.setText("Σφάλμα: Δεν επιλέχθηκε συναυλία.");
            return;
        }

        int userId = Session.getCurrentUserId();
        int concertId = selectedConcert.getConcertId();

        int result = waitlistManager.joinWaitlistWithCard(userId, concertId, cardData);

        if (result > 0) {
            // Setup Success Screen Modularly
            SuccessController.titleText = "Εγγραφή στη Λίστα Αναμονής! ⏳";
            SuccessController.descText = "Προστεθήκατε επιτυχώς στη λίστα αναμονής της συναυλίας.\n" +
                    "Σειρά προτεραιότητας: #" + result + "\n" +
                    "Η κάρτα σας προεγκρίθηκε και θα χρεωθεί αυτόματα μόλις ελευθερωθεί θέση!";
            SuccessController.buttonText = "ΕΠΙΣΤΡΟΦΗ ΣΤΗΝ ΑΡΧΙΚΗ 🏠";
            SuccessController.nextFxml = "/user_home.fxml";
            SuccessController.nextTitle = "MusicTick - Home";
            SuccessController.nextWidth = 800;
            SuccessController.nextHeight = 600;

            openPage("/success.fxml", "MusicTick - Επιτυχία", 700, 500);
        } else if (result == -2) {
            statusLabel.setText("Έχετε ήδη προστεθεί στη λίστα αναμονής για αυτή τη συναυλία!");
        } else {
            statusLabel.setText("Σφάλμα κατά την εγγραφή στη λίστα αναμονής.");
        }
    }

    @FXML
    private void handleCancel() {
        openPage("/search.fxml", "MusicTick - Αναζήτηση", 800, 550);
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
