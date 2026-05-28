package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.BookingManager;
import models.Concert;
import models.Seat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class ValidatePaymentDetailsController {
    @FXML private Label paymentConcertLabel;
    @FXML private Label paymentSeatLabel;
    @FXML private Label paymentPriceLabel;
    @FXML private Label timerLabel;
    @FXML private TextField cardDetailsField;
    @FXML private Label paymentStatusLabel;

    // Static fields populated by TemporaryBookingController
    public static Seat selectedSeat = null;
    public static int temporaryTicketId = -1;
    public static BigDecimal ticketPrice = BigDecimal.ZERO;

    private final BookingManager bookingManager = new BookingManager();

    private Timer countdownTimer = null;
    private int secondsRemaining = 30;

    @FXML
    public void initialize() {
        Concert currentConcert = SearchController.selectedConcert;
        if (currentConcert == null || selectedSeat == null) {
            if (paymentStatusLabel != null) {
                paymentStatusLabel.setText("Σφάλμα: Δεν βρέθηκαν στοιχεία κράτησης.");
            }
            return;
        }

        if (paymentConcertLabel != null) {
            paymentConcertLabel.setText(currentConcert.getTitle());
        }
        if (paymentSeatLabel != null) {
            paymentSeatLabel.setText("Section " + selectedSeat.getSectionName() + ", Row " + selectedSeat.getRowLabel() + ", Seat " + selectedSeat.getSeatNumber() + " (" + selectedSeat.getSeatType() + ")");
        }
        if (paymentPriceLabel != null) {
            paymentPriceLabel.setText(ticketPrice.toString() + " €");
        }

        startTimeoutTimer();
    }

    private void startTimeoutTimer() {
        secondsRemaining = 30;
        countdownTimer = new Timer(true);
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    secondsRemaining--;
                    if (timerLabel != null) {
                        timerLabel.setText("Χρόνος για ολοκλήρωση: " + secondsRemaining + "s");
                    }
                    if (secondsRemaining <= 0) {
                        handlePaymentTimeout();
                    }
                });
            }
        }, 1000, 1000);
    }

    public void stopTimeoutTimer() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    private void handlePaymentTimeout() {
        stopTimeoutTimer();
        bookingManager.cancelBooking(temporaryTicketId);

        // Redirect to timeout_screen.fxml representing "DisplayTimeoutScreen()" in sequence diagram
        openPage("/timeout_screen.fxml", "MusicTick - Λήξη Χρόνου", 800, 550);
    }

    @FXML
    private void handleSubmitPayment() {
        String cardData = cardDetailsField.getText().trim();
        if (cardData.isEmpty()) {
            paymentStatusLabel.setText("Συμπληρώστε τα στοιχεία της κάρτας σας.");
            return;
        }

        stopTimeoutTimer();

        // Calling checkBalance() representing method from the sequence diagram
        checkBalance(cardData);
    }

    // checkBalance() representing the method in the sequence diagram:
    // "BookingScreen -> ValidatePaymentDetailsController : checkBalance()"
    private void checkBalance(String cardData) {
        int userId = Session.getCurrentUserId();
        BookingManager.PaymentResult result = bookingManager.processPayment(userId, temporaryTicketId, cardData, ticketPrice);

        if (result.success) {
            // ValidatePaymentDetailsController -> CreateBookingController : save()
            CreateBookingController.save(temporaryTicketId, result.orderId);
        } else {
            if ("TIMEOUT".equals(result.message)) {
                openPage("/timeout_screen.fxml", "MusicTick - Λήξη Χρόνου", 800, 550);
            } else {
                // Navigate to ErrorScreen (/error_screen.fxml)
                // We set a static error message so the ErrorScreen can display it
                ErrorController.errorMessage = "Αποτυχία πύλης πληρωμών. " + 
                    ("CIRCUIT_OPEN".equals(result.message) ? 
                     "Η πύλη πληρωμών είναι προσωρινά εκτός λειτουργίας (Circuit Breaker OPEN). Δοκιμάστε ξανά σε λίγο." : 
                     "Άκυρη κάρτα (Δοκιμάστε άλλη).");
                openPage("/error_screen.fxml", "MusicTick - Σφάλμα Πληρωμής", 800, 550);
            }
        }
    }

    @FXML
    private void handleCancel() {
        stopTimeoutTimer();
        if (temporaryTicketId != -1) {
            bookingManager.cancelBooking(temporaryTicketId);
        }
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
