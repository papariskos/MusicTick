package com.musictick.manager;

import com.musictick.dao.BookingDAO;
import com.musictick.dao.ConcertDAO;
import models.Seat;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingManager {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ConcertDAO concertDAO = new ConcertDAO();

    // Map to keep track of the temporary booking creation time
    private static final Map<Integer, LocalDateTime> bookingTimestamps = new HashMap<>();
    private static final long TIMEOUT_SECONDS = 30; // 30 seconds timeout for simulated booking

    // Circuit Breaker State Management
    public enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }

    private static CircuitState circuitState = CircuitState.CLOSED;
    private static int failureCount = 0;
    private static final int FAILURE_THRESHOLD = 3;
    private static LocalDateTime lastStateChange = LocalDateTime.now();
    private static final long COOLDOWN_SECONDS = 10; // Cooldown period of 10s before Half-Open

    public static class PaymentResult {
        public final boolean success;
        public final String message;
        public final int orderId;

        public PaymentResult(boolean success, String message, int orderId) {
            this.success = success;
            this.message = message;
            this.orderId = orderId;
        }
    }

    public int initiateBooking(int userId, int concertId, int seatId, String seatType) throws SQLException {
        int ticketTypeId = bookingDAO.getTicketTypeId(concertId, seatType);
        if (ticketTypeId == -1) {
            throw new SQLException("Δεν βρέθηκε τύπος εισιτηρίου για αυτή τη θέση.");
        }

        int ticketId = bookingDAO.createTemporaryBooking(userId, concertId, seatId, ticketTypeId);
        if (ticketId != -1) {
            bookingTimestamps.put(ticketId, LocalDateTime.now());
        }
        return ticketId;
    }

    public BigDecimal getTicketPrice(int concertId, String seatType) throws SQLException {
        int ticketTypeId = bookingDAO.getTicketTypeId(concertId, seatType);
        if (ticketTypeId == -1)
            return BigDecimal.ZERO;
        return bookingDAO.getTicketPrice(ticketTypeId);
    }

    public boolean isExpired(int ticketId) {
        LocalDateTime timestamp = bookingTimestamps.get(ticketId);
        if (timestamp == null)
            return true;

        long elapsed = Duration.between(timestamp, LocalDateTime.now()).toSeconds();
        return elapsed > TIMEOUT_SECONDS;
    }

    public void cancelBooking(int ticketId) {
        try {
            bookingDAO.cancelTemporaryBooking(ticketId);
            bookingTimestamps.remove(ticketId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Circuit Breaker check/call
    private synchronized void checkCircuitState() {
        if (circuitState == CircuitState.OPEN) {
            long elapsed = Duration.between(lastStateChange, LocalDateTime.now()).toSeconds();
            if (elapsed > COOLDOWN_SECONDS) {
                circuitState = CircuitState.HALF_OPEN;
                lastStateChange = LocalDateTime.now();
                System.out.println("Circuit Breaker transitioned to HALF-OPEN. Testing gateway...");
            }
        }
    }

    public synchronized PaymentResult processPayment(int userId, int ticketId, String paymentData, BigDecimal amount) {
        // 1. Check timeout first
        if (isExpired(ticketId)) {
            cancelBooking(ticketId);
            return new PaymentResult(false, "TIMEOUT", -1);
        }

        // 2. Check Circuit Breaker status
        checkCircuitState();
        if (circuitState == CircuitState.OPEN) {
            return new PaymentResult(false, "CIRCUIT_OPEN", -1);
        }

        // 3. Simulate payment gateway call
        boolean gatewayCallSuccess = true;
        String normalizedPayment = paymentData != null ? paymentData.trim().toUpperCase() : "";

        // Let's trip the circuit breaker if the user types "FAIL" or "GATEWAY_ERROR"
        if (normalizedPayment.contains("FAIL") || normalizedPayment.contains("ERROR")) {
            gatewayCallSuccess = false;
        }

        if (!gatewayCallSuccess) {
            failureCount++;
            System.out.println("Gateway call failed. Failure count: " + failureCount);
            if (circuitState == CircuitState.HALF_OPEN || failureCount >= FAILURE_THRESHOLD) {
                circuitState = CircuitState.OPEN;
                lastStateChange = LocalDateTime.now();
                System.out.println("Circuit Breaker TRIPPED to OPEN!");
            }
            return new PaymentResult(false, "GATEWAY_FAILURE", -1);
        }

        // Success flow
        if (circuitState == CircuitState.HALF_OPEN) {
            circuitState = CircuitState.CLOSED;
            failureCount = 0;
            System.out.println("Circuit Breaker CLOSED! Gateway is healthy again.");
        } else if (circuitState == CircuitState.CLOSED) {
            failureCount = 0; // reset failures on success
        }

        // 4. Create Order and Payment in DB
        try {
            int orderId = bookingDAO.createOrderAndPayment(userId, amount, paymentData);
            bookingDAO.confirmBooking(ticketId, orderId);
            bookingTimestamps.remove(ticketId); // clean up
            return new PaymentResult(true, "SUCCESS", orderId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new PaymentResult(false, "DB_ERROR", -1);
        }
    }

    public static CircuitState getCircuitState() {
        return circuitState;
    }

    public static void resetCircuit() {
        circuitState = CircuitState.CLOSED;
        failureCount = 0;
        lastStateChange = LocalDateTime.now();
    }
}
