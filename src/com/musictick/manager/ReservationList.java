package com.musictick.manager;

import com.musictick.Session;
import com.musictick.dao.TicketDAO;
import models.Ticket;
import models.enums.TicketStatus;

import java.sql.SQLException;

public class ReservationList {
    private static final TicketDAO ticketDAO = new TicketDAO();

    public static String checkTicketValidity(int ticketId) {
        System.out.println("ReservationList: checkTicketValidity() called for ticketId=" + ticketId);
        try {
            boolean valid = ticketDAO.checkTicketValidity(ticketId, Session.getCurrentUserId());
            String status = valid ? "validTicket" : "invalidTicket";
            System.out.println("ReservationList: returnTicketStatus() -> " + status);
            return status;
        } catch (SQLException e) {
            e.printStackTrace();
            return "invalidTicket";
        }
    }

    public static void deleteTicket(int ticketId) {
        System.out.println("ReservationList: deleteTicket() called for ticketId=" + ticketId);
        System.out.println("ReservationList: ticketDeleted()");
    }

    public static void saveTicket(Ticket newTicket) {
        System.out.println("ReservationList: saveTicket() called for ticketId=" + newTicket.getTicketId());
    }

    public static String checkReservationDetails(String reservationData) {
        System.out.println("ReservationList: checkReservationDetails() called for reservationData=" + reservationData);
        if (reservationData != null && reservationData.toUpperCase().contains("FAIL")) {
            System.out.println("ReservationList: returnReservationStatus() -> invalidReservation");
            return "invalidReservation";
        }
        System.out.println("ReservationList: returnReservationStatus() -> validReservation");
        return "validReservation";
    }

    public static void cancelReservation(String reservationData) {
        System.out.println("ReservationList: cancelReservation() called for reservationData=" + reservationData);
        System.out.println("ReservationList: reservationCancelled()");
    }
}
