package com.musictick.controller;

import com.musictick.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class CreateBookingController {
    // Static variables to pass data to TicketScreen / TicketConfirmationController
    public static String confirmedConcertTitle = "";
    public static String confirmedSeatDetails = "";
    public static BigDecimal confirmedPrice = BigDecimal.ZERO;
    public static int confirmedOrderId = -1;

    // save() representing the method in the sequence diagram
    public static void save(int ticketId, int orderId) {
        try {
            // Represent the steps in the sequence diagram:
            // 1. BookedSeat()
            // 2. CreateReservation()
            bookedSeat();
            createReservation();

            // Prepare static data for the TicketScreen
            confirmedConcertTitle = SearchController.selectedConcert.getTitle();
            var seat = ValidatePaymentDetailsController.selectedSeat;
            confirmedSeatDetails = "Section " + seat.getSectionName() + ", Row " + seat.getRowLabel() + ", Seat " + seat.getSeatNumber();
            confirmedPrice = ValidatePaymentDetailsController.ticketPrice;
            confirmedOrderId = orderId;

            // Maintain absolute backward compatibility with the existing BookingController static fields!
            BookingController.confirmedConcertTitle = confirmedConcertTitle;
            BookingController.confirmedSeatDetails = confirmedSeatDetails;
            BookingController.confirmedPrice = confirmedPrice;
            BookingController.confirmedOrderId = confirmedOrderId;


            // 3. DisplayTicketScreen()
            displayTicketScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void bookedSeat() {
        System.out.println("CreateBookingController: BookedSeat() successfully invoked on ConcertList");
    }

    private static void createReservation() {
        System.out.println("CreateBookingController: CreateReservation() successfully invoked on ReservationList");
    }

    private static void displayTicketScreen() {
        try {
            Parent root = FXMLLoader.load(CreateBookingController.class.getResource("/ticket_screen.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 800, 550));
            stage.setTitle("MusicTick - Επιτυχής Κράτηση");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
