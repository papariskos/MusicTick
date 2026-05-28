package com.musictick.manager;

public class ConcertList {
    public static String checkVIPSeatAvailability(int concertId) {
        System.out.println("ConcertList: checkVIPSeatAvailability() called for concertId=" + concertId);
        System.out.println("ConcertList: returnAvailability() -> seatsAvailable");
        return "seatsAvailable";
    }

    public static void temporarilyReserve(int seatId) {
        System.out.println("ConcertList: temporarilyReserve() called for seatId=" + seatId);
        System.out.println("ConcertList: seatTemporarilyReserved()");
    }

    public static void releasePreviousSeat(int oldSeatId) {
        System.out.println("ConcertList: releasePreviousSeat() called for seatId=" + oldSeatId);
    }

    public static void confirmNewSeat(int seatId) {
        System.out.println("ConcertList: confirmNewSeat() called for seatId=" + seatId);
    }
}
