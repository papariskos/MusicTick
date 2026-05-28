package com.musictick.controller;

import com.musictick.Main;
import com.musictick.Session;
import com.musictick.manager.BookingManager;
import com.musictick.manager.SearchManager;
import models.Concert;
import models.Seat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TemporaryBookingController {
    @FXML private Label concertTitleLabel;
    @FXML private Label selectionErrorLabel;
    @FXML private ListView<String> seatsListView;

    private final SearchManager searchManager = new SearchManager();
    private final BookingManager bookingManager = new BookingManager();

    private List<Seat> availableSeats = new ArrayList<>();
    private Seat selectedSeat = null;

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
            int temporaryTicketId = bookingManager.initiateBooking(userId, currentConcert.getConcertId(), selectedSeat.getSeatId(), selectedSeat.getSeatType());
            BigDecimal ticketPrice = bookingManager.getTicketPrice(currentConcert.getConcertId(), selectedSeat.getSeatType());

            // Set the shared fields for the next controller (ValidatePaymentDetailsController)
            ValidatePaymentDetailsController.selectedSeat = selectedSeat;
            ValidatePaymentDetailsController.temporaryTicketId = temporaryTicketId;
            ValidatePaymentDetailsController.ticketPrice = ticketPrice;

            // Navigate to BookingScreen (/booking_screen.fxml)
            openPage("/booking_screen.fxml", "MusicTick - Στοιχεία Πληρωμής", 800, 550);
        } catch (Exception e) {
            e.printStackTrace();
            selectionErrorLabel.setText("Αποτυχία δέσμευσης θέσης. Ίσως η θέση μόλις κρατήθηκε.");
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
