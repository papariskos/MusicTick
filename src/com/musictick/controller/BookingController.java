package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.BookingManager;
import com.musictick.manager.SearchManager;
import models.Concert;
import models.Seat;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BookingController {
    // Seating Selection Pane fields
    @FXML private VBox seatingPane;
    @FXML private ListView<String> seatsListView;
    @FXML private Label concertTitleLabel;
    @FXML private Label selectionErrorLabel;

    // Payment Pane fields
    @FXML private VBox paymentPane;
    @FXML private Label paymentConcertLabel;
    @FXML private Label paymentSeatLabel;
    @FXML private Label paymentPriceLabel;
    @FXML private Label timerLabel;
    @FXML private TextField cardDetailsField;
    @FXML private Label paymentStatusLabel;

    private final SearchManager searchManager = new SearchManager();
    private final BookingManager bookingManager = new BookingManager();

    private List<Seat> availableSeats = new ArrayList<>();
    private Seat selectedSeat = null;
    private int temporaryTicketId = -1;
    private BigDecimal ticketPrice = BigDecimal.ZERO;

    // Timer fields for Timeout simulation
    private Timer countdownTimer;
    private int secondsRemaining = 30;

    // Static variables to pass data to confirmation screen
    public static String confirmedConcertTitle = "";
    public static String confirmedSeatDetails = "";
    public static BigDecimal confirmedPrice = BigDecimal.ZERO;
    public static int confirmedOrderId = -1;

    @FXML
    public void initialize() {
        Concert currentConcert = SearchController.selectedConcert;
        if (currentConcert == null) {
            if (selectionErrorLabel != null) {
                selectionErrorLabel.setText("Σφάλμα: Δεν έχει επιλεγεί συναυλία.");
            }
            return;
        }

        if (concertTitleLabel != null) {
            concertTitleLabel.setText(currentConcert.getTitle());
        }

        if (seatingPane != null) {
            seatingPane.setVisible(true);
            seatingPane.setManaged(true);
        }

        if (paymentPane != null) {
            paymentPane.setVisible(false);
            paymentPane.setManaged(false);
        }

        loadSeats(currentConcert.getConcertId());
    }

    private void loadSeats(int concertId) {
        try {
            availableSeats = searchManager.getAvailableSeats(concertId);
            List<String> listItems = new ArrayList<>();
            for (Seat s : availableSeats) {
                listItems.add(s.getSeatId() + " - Section " + s.getSectionName() + ", Row " + s.getRowLabel() + ", Seat " + s.getSeatNumber() + " [" + s.getSeatType() + "]");
            }
            seatsListView.setItems(FXCollections.observableArrayList(listItems));
            if (availableSeats.isEmpty()) {
                selectionErrorLabel.setText("Δεν υπάρχουν διαθέσιμες θέσεις.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            selectionErrorLabel.setText("Σφάλμα κατά τη φόρτωση των θέσεων.");
        }
    }

    @FXML
    private void handleReserveSeat() {
        String selected = seatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selectionErrorLabel.setText("Παρακαλώ επιλέξτε μια θέση.");
            return;
        }

        int seatId = Integer.parseInt(selected.split(" - ")[0].trim());
        for (Seat s : availableSeats) {
            if (s.getSeatId() == seatId) {
                selectedSeat = s;
                break;
            }
        }

        if (selectedSeat == null) return;

        try {
            Concert currentConcert = SearchController.selectedConcert;
            int userId = Session.getCurrentUserId();
            
            // Initiate Booking flow
            temporaryTicketId = bookingManager.initiateBooking(userId, currentConcert.getConcertId(), selectedSeat.getSeatId(), selectedSeat.getSeatType());
            ticketPrice = bookingManager.getTicketPrice(currentConcert.getConcertId(), selectedSeat.getSeatType());

            // Switch to Payment view
            seatingPane.setVisible(false);
            seatingPane.setManaged(false);
            
            paymentPane.setVisible(true);
            paymentPane.setManaged(true);

            paymentConcertLabel.setText(currentConcert.getTitle());
            paymentSeatLabel.setText("Section " + selectedSeat.getSectionName() + ", Row " + selectedSeat.getRowLabel() + ", Seat " + selectedSeat.getSeatNumber() + " (" + selectedSeat.getSeatType() + ")");
            paymentPriceLabel.setText(ticketPrice.toString() + " €");

            startTimeoutTimer();
        } catch (Exception e) {
            e.printStackTrace();
            selectionErrorLabel.setText("Αποτυχία δέσμευσης θέσης. Ίσως η θέση μόλις κρατήθηκε.");
        }
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

    private void stopTimeoutTimer() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    private void handlePaymentTimeout() {
        stopTimeoutTimer();
        bookingManager.cancelBooking(temporaryTicketId);
        
        // Redirect to booking timeout failure
        BookingFailureController.failureMessage = "Η κράτηση έληξε (Timeout)! Δεν ολοκληρώσατε την πληρωμή εντός 30 δευτερολέπτων.";
        openPage("/booking_failure.fxml", "MusicTick - Λήξη Χρόνου", 800, 550);
    }

    @FXML
    private void handleSubmitPayment() {
        String cardData = cardDetailsField.getText().trim();
        if (cardData.isEmpty()) {
            paymentStatusLabel.setText("Συμπληρώστε τα στοιχεία της κάρτας σας.");
            return;
        }

        stopTimeoutTimer();

        int userId = Session.getCurrentUserId();
        BookingManager.PaymentResult result = bookingManager.processPayment(userId, temporaryTicketId, cardData, ticketPrice);

        if (result.success) {
            confirmedConcertTitle = SearchController.selectedConcert.getTitle();
            confirmedSeatDetails = "Section " + selectedSeat.getSectionName() + ", Row " + selectedSeat.getRowLabel() + ", Seat " + selectedSeat.getSeatNumber();
            confirmedPrice = ticketPrice;
            confirmedOrderId = result.orderId;

            openPage("/ticket_confirmation.fxml", "MusicTick - Επιτυχής Κράτηση", 800, 550);
        } else {
            if ("TIMEOUT".equals(result.message)) {
                BookingFailureController.failureMessage = "Η κράτηση έληξε (Timeout)!";
                openPage("/booking_failure.fxml", "MusicTick - Λήξη Χρόνου", 800, 550);
            } else if ("CIRCUIT_OPEN".equals(result.message)) {
                paymentStatusLabel.setText("Η πύλη πληρωμών είναι προσωρινά εκτός λειτουργίας (Circuit Breaker OPEN). Δοκιμάστε ξανά σε λίγο.");
                // Resume timer since they can correct payment later or wait
                startTimeoutTimer();
            } else if ("GATEWAY_FAILURE".equals(result.message)) {
                paymentStatusLabel.setText("Αποτυχία πύλης πληρωμών. Άκυρη κάρτα (Δοκιμάστε άλλη).");
                startTimeoutTimer();
            } else {
                paymentStatusLabel.setText("Σφάλμα πληρωμής. Δοκιμάστε ξανά.");
                startTimeoutTimer();
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
