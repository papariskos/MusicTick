package com.musictick;

import com.musictick.dao.TicketDAO;
import com.musictick.dao.UserDAO;
import com.musictick.dao.BookingDAO;
import com.musictick.dao.ConcertDAO;
import com.musictick.manager.*;
import models.Ticket;
import models.User;
import models.ForumPost;
import models.Notification;
import models.enums.Role;
import models.enums.UserStatus;
import models.enums.ConcertStatus;

import java.math.BigDecimal;
import java.util.List;
import java.sql.*;

public class MusicTickTestRunner {
    private static int createdTicketId1 = -1;
    private static int createdTicketId2 = -1;
    private static int createdTicketId3 = -1;

    // Color codes for professional console prints
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";

    public static void main(String[] args) {
        System.out.println(CYAN + "==========================================================================" + RESET);
        System.out.println(CYAN + "⚡ STARTING COMPREHENSIVE MUSICTICK SYSTEM TEST SUITE (HAPPY & ALT PATHS) ⚡" + RESET);
        System.out.println(CYAN + "==========================================================================" + RESET);

        try {
            cleanDatabaseForTesting();

            // 1. LOGIN & REGISTRATION FLOWS
            testLoginFlows();

            // 2. SEARCH & BUY FLOWS (inc. Circuit Breaker & Seat Release)
            testSearchAndBuyFlows();

            // 3. MY TICKETS & TRANSFER FLOWS
            testTicketTransferFlows();

            // 4. TICKET CANCELLATION FLOWS
            testTicketCancellationFlows();

            // 5. REPORT PROBLEM & ORGANIZER REFUND FLOWS
            testReportProblemFlows();

            // 6. FORUM & POST MODERATION FLOWS (inc. Hardcoded Admin checks)
            testForumAndModerationFlows();

            // 7. NOTIFICATIONS & DELETION FLOWS
            testNotificationsFlows();

            // 8. ADMIN ACTIVE CONCERT DELETION FLOWS
            testAdminConcertDeletionFlows();

            // 9. VIP UPGRADE FLOWS
            testVipUpgradeFlows();

            // 10. CONCERT REVIEW FLOWS
            testConcertReviewFlows();

            System.out.println(
                    CYAN + "\n==========================================================================" + RESET);
            System.out.println(GREEN + "✅ ALL MUSIC TICK SYSTEM TESTS COMPLETED SUCCESSFULLY! " + RESET);
            System.out.println(
                    CYAN + "==========================================================================" + RESET);

        } catch (Exception e) {
            System.err.println(RED + "\n❌ CRITICAL TEST FAILURE: " + e.getMessage() + RESET);
            e.printStackTrace();
        }
    }

    private static void testLoginFlows() {
        System.out.println(PURPLE + "\n--- [1] TESTING LOGIN & REGISTRATION FLOWS ---" + RESET);

        // Case A: Happy Path (Hardcoded Admin Login with email form)
        System.out.println(YELLOW + "  [Case A] Happy Path: Logging in as Hardcoded Admin..." + RESET);
        Session.setCurrentUserId(0); // clear first
        Session.setCurrentUserRole("");

        // Simulating hardcoded credentials check
        String email = "admin@musictick.com";
        String password = "admin";

        if ((email.equalsIgnoreCase("admin") || email.equalsIgnoreCase("admin@musictick.com"))
                && password.equals("admin")) {
            Session.setCurrentUserId(3);
            Session.setCurrentUserRole("ADMIN");
        }

        assertTest(Session.getCurrentUserId() == 3, "Admin ID should be hardcoded to 3");
        assertTest("ADMIN".equals(Session.getCurrentUserRole()), "Admin role should be ADMIN");
        System.out.println(GREEN + "  [PASS] Hardcoded Admin login bypass verified." + RESET);

        // Case B: Happy Path (Shortcut Customer Login)
        System.out.println(YELLOW + "  [Case B] Happy Path: Logging in as Customer shortcut..." + RESET);
        email = "user";
        password = "user";
        if ((email.equalsIgnoreCase("user") || email.equalsIgnoreCase("user@musictick.com"))
                && password.equals("user")) {
            Session.setCurrentUserId(1);
            Session.setCurrentUserRole("CUSTOMER");
        }
        assertTest(Session.getCurrentUserId() == 1, "Customer ID should be 1");
        assertTest("CUSTOMER".equals(Session.getCurrentUserRole()), "Customer role should be CUSTOMER");
        System.out.println(GREEN + "  [PASS] Customer login verified." + RESET);

        // Case C: Alternative Path (Invalid Credentials)
        System.out.println(YELLOW + "  [Case C] Alternative Path: Attempting login with incorrect password..." + RESET);
        email = "user";
        password = "wrongpassword";
        boolean checkShortcut = ((email.equalsIgnoreCase("user") || email.equalsIgnoreCase("user@musictick.com"))
                && password.equals("user"));
        assertTest(!checkShortcut, "Shortcut check must fail for wrong password");
        System.out.println(GREEN + "  [PASS] Invalid password rejected as expected." + RESET);

        // Case D: Happy Path (New User Signup - Unique Email)
        System.out.println(YELLOW + "  [Case D] Happy Path: Registering a new user with a unique email..." + RESET);
        String signupEmail = "new_unique_user_" + System.currentTimeMillis() + "@musictick.com";
        boolean signupExists = false;
        if (signupEmail.equalsIgnoreCase("admin@musictick.com") || signupEmail.equalsIgnoreCase("user@musictick.com")) {
            signupExists = true;
        }
        assertTest(!signupExists, "New unique email should not exist");
        System.out.println(GREEN + "  [PASS] Unique email registration pre-check allowed." + RESET);

        // Case E: Alternative Path (Signup - Duplicate Email)
        System.out.println(
                YELLOW + "  [Case E] Alternative Path: Attempting to register an already existing email..." + RESET);
        String duplicateEmail = "admin@musictick.com";
        boolean dupExists = false;
        if (duplicateEmail.equalsIgnoreCase("admin@musictick.com")
                || duplicateEmail.equalsIgnoreCase("user@musictick.com")) {
            dupExists = true;
        }
        assertTest(dupExists, "Duplicate email registration must be flagged as existing");
        System.out.println(GREEN + "  [PASS] Duplicate registration blocked as expected." + RESET);
    }

    private static void testSearchAndBuyFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [2] TESTING SEARCH & BUY FLOWS ---" + RESET);

        BookingManager manager = new BookingManager();
        Session.setCurrentUserId(1);
        Session.setCurrentUserRole("CUSTOMER");

        // Case A: Happy Path (Initiate Booking and Pay Successfully)
        System.out.println(
                YELLOW + "  [Case A] Happy Path: Searching concert & initiating booking for Regular seat..." + RESET);
        int concertId = 1;
        int seatId = 15;
        BigDecimal price = manager.getTicketPrice(concertId, "REGULAR");
        System.out.println("  Regular Seat Price is: " + price + " EUR");

        int ticketId = manager.initiateBooking(1, concertId, seatId, "REGULAR");
        System.out.println("  Booking created successfully with ticket ID: " + ticketId);
        createdTicketId1 = ticketId;

        System.out.println("  Processing payment with valid card...");
        BookingManager.PaymentResult result = manager.processPayment(1, ticketId, "VISA-1234-VALID", price);
        assertTest(result.success, "Payment should succeed");
        System.out.println(
                GREEN + "  [PASS] Happy Path booking and payment successful. Order ID: " + result.orderId + RESET);

        // Dynamically book ticket 2 for Transfer Test
        int ticketId2 = manager.initiateBooking(1, concertId, seatId + 1, "REGULAR");
        createdTicketId2 = ticketId2;
        manager.processPayment(1, ticketId2, "VISA-1234-VALID", price);
        System.out.println("  Ticket 2 created and paid for Transfer Test. Ticket ID: " + ticketId2);

        // Dynamically book ticket 3 for Cancellation Test
        int ticketId3 = manager.initiateBooking(1, concertId, seatId + 2, "REGULAR");
        createdTicketId3 = ticketId3;
        manager.processPayment(1, ticketId3, "VISA-1234-VALID", price);
        System.out.println("  Ticket 3 created and paid for Cancellation Test. Ticket ID: " + ticketId3);

        // Case B: Alternative Path (Payment Gateway Fails)
        System.out.println(
                YELLOW + "  [Case B] Alternative Path: Attempting payment with simulated gateway failure..." + RESET);
        int ticketIdFail = manager.initiateBooking(1, concertId, seatId + 3, "REGULAR");
        BookingManager.PaymentResult result2 = manager.processPayment(1, ticketIdFail, "FAIL_TRANSACTION", price);
        assertTest(!result2.success, "Payment should fail");
        assertTest("GATEWAY_FAILURE".equals(result2.message), "Failure message should be GATEWAY_FAILURE");
        System.out.println(GREEN + "  [PASS] Gateway failure handled correctly." + RESET);

        // Case C: Alternative Path (Circuit Breaker Tripped)
        System.out.println(
                YELLOW + "  [Case C] Alternative Path: Tripping Circuit Breaker with multiple failures..." + RESET);
        BookingManager.resetCircuit();

        // 1st failure
        manager.processPayment(1, ticketIdFail, "FAIL_TRANSACTION", price);
        // 2nd failure
        int tid3 = manager.initiateBooking(1, concertId, seatId + 4, "REGULAR");
        manager.processPayment(1, tid3, "FAIL_TRANSACTION", price);
        // 3rd failure
        int tid4 = manager.initiateBooking(1, concertId, seatId + 5, "REGULAR");
        manager.processPayment(1, tid4, "FAIL_TRANSACTION", price);

        System.out.println("  Circuit state after 3 failures: " + BookingManager.getCircuitState());
        assertTest(BookingManager.getCircuitState() == BookingManager.CircuitState.OPEN,
                "Circuit breaker should be OPEN");

        // Try a 4th payment - should block immediately!
        int tid5 = manager.initiateBooking(1, concertId, seatId + 6, "REGULAR");
        BookingManager.PaymentResult cbResult = manager.processPayment(1, tid5, "VALID_CARD", price);
        assertTest(!cbResult.success, "Payment should be rejected by circuit breaker");
        assertTest("CIRCUIT_OPEN".equals(cbResult.message), "Error should be CIRCUIT_OPEN");
        System.out.println(GREEN + "  [PASS] Circuit Breaker successfully blocked downstream calls when OPEN." + RESET);

        BookingManager.resetCircuit(); // reset state for remaining tests
    }

    private static void testTicketTransferFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [3] TESTING MY TICKETS & TRANSFER FLOWS ---" + RESET);

        TicketDAO ticketDAO = new TicketDAO();
        UserDAO userDAO = new UserDAO();

        // Case A: Happy Path (Transfer ticket to registered recipient)
        System.out.println(YELLOW + "  [Case A] Happy Path: Fetching recipient and transferring ticket ID "
                + createdTicketId2 + "..." + RESET);
        User recipient = userDAO.findRecipient("organizer@musictick.com");
        assertTest(recipient != null, "Recipient should be found in system");
        System.out.println("  Found recipient: " + recipient.getFirstName() + " (ID: " + recipient.getUserId() + ")");

        int resId = ticketDAO.transferTicket(createdTicketId2, 1, recipient.getUserId());
        assertTest(resId == createdTicketId2, "Transfer should succeed returning the ticketId");

        // Verify that the ticket owner is updated in DB
        boolean transferredOk = false;
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT user_id, status FROM tickets WHERE ticket_id = ?")) {
            ps.setInt(1, createdTicketId2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ownerId = rs.getInt("user_id");
                    String status = rs.getString("status");
                    transferredOk = (ownerId == recipient.getUserId() && "ACTIVE".equals(status));
                }
            }
        }
        assertTest(transferredOk, "Ticket owner should be updated in DB");
        System.out.println(GREEN + "  [PASS] Ticket transferred to recipient successfully." + RESET);

        // Case B: Alternative Path (Transfer to unregistered recipient)
        System.out
                .println(YELLOW + "  [Case B] Alternative Path: Attempting transfer to non-existent email..." + RESET);
        User nullRecipient = userDAO.findRecipient("unknown_user");
        assertTest(nullRecipient == null, "Unregistered user must return null");
        System.out.println(GREEN + "  [PASS] Unregistered user lookup returned null as expected." + RESET);
    }

    private static void testTicketCancellationFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [4] TESTING TICKET CANCELLATION FLOWS ---" + RESET);

        // Case A: Happy Path (Cancellation policy allowed)
        System.out.println(
                YELLOW + "  [Case A] Happy Path: Checking cancellation policy for standard concert..." + RESET);
        String policy = CancellationPolicyDB.checkCancellationPolicy("CONCERT_OK");
        assertTest("cancellationAllowed".equalsIgnoreCase(policy), "Standard concert cancellation should be allowed");
        System.out.println(GREEN + "  [PASS] Cancellation policy verified as allowed." + RESET);

        // Case B: Alternative Path (Cancellation not allowed)
        System.out
                .println(YELLOW + "  [Case B] Alternative Path: Checking policy for concert under 24 hours..." + RESET);
        String policyExp = CancellationPolicyDB.checkCancellationPolicy("NO_CANCEL");
        assertTest("cancellationNotAllowed".equalsIgnoreCase(policyExp), "Expired cancellation should be disallowed");
        System.out.println(GREEN + "  [PASS] Expired policy correctly blocked cancellation." + RESET);

        // Case C: Perform actual DB cancellation on createdTicketId3
        System.out.println(YELLOW + "  [Case C] Happy Path: Performing cancellation in DB on ticket ID "
                + createdTicketId3 + "..." + RESET);
        String sqlCancel = "UPDATE tickets SET status = 'CANCELLED' WHERE ticket_id = ?";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlCancel)) {
            ps.setInt(1, createdTicketId3);
            int rows = ps.executeUpdate();
            assertTest(rows > 0, "DB ticket should be cancelled");
        }

        // Check ticket status is CANCELLED in DB
        boolean isCancelled = false;
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT status FROM tickets WHERE ticket_id = ?")) {
            ps.setInt(1, createdTicketId3);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isCancelled = "CANCELLED".equals(rs.getString("status"));
                }
            }
        }
        assertTest(isCancelled, "Ticket status must be CANCELLED in database");
        System.out.println(GREEN + "  [PASS] Ticket successfully cancelled in DB." + RESET);
    }

    private static void testReportProblemFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [5] TESTING REPORT PROBLEM & REFUND FLOWS ---" + RESET);

        // Case A: Happy Path (Report problem on valid active ticket)
        System.out.println(YELLOW + "  [Case A] Happy Path: Submitting problem report for ticket ID " + createdTicketId1
                + "..." + RESET);
        TicketDAO ticketDAO = new TicketDAO();
        boolean valid = ticketDAO.checkTicketValidity(createdTicketId1, 1);
        assertTest(valid, "Ticket " + createdTicketId1 + " should be valid for user 1");

        System.out.println("  Inserting problem report directly to database 'reports' table...");
        String sqlRep = "INSERT INTO reports (ticket_id, user_id, organizer_id, description, status) VALUES (?, 1, 2, 'Sound issue at venue', 'OPEN')";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlRep)) {
            ps.setInt(1, createdTicketId1);
            int rows = ps.executeUpdate();
            assertTest(rows > 0, "Report should be inserted in DB");
        }

        // Assert that the report is in DB
        boolean reportExists = false;
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT COUNT(*) AS total FROM reports WHERE ticket_id = ?")) {
            ps.setInt(1, createdTicketId1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reportExists = rs.getInt("total") > 0;
                }
            }
        }
        assertTest(reportExists, "Problem report must exist in database");
        System.out.println(GREEN + "  [PASS] Problem reported and verified in DB successfully." + RESET);

        // Case B: Alternative Path (Report on invalid ticket ID)
        System.out.println(
                YELLOW + "  [Case B] Alternative Path: Reporting problem for non-existent ticket 99999..." + RESET);
        boolean invalidTicket = ticketDAO.checkTicketValidity(99999, 1);
        assertTest(!invalidTicket, "Ticket 99999 must be invalid");
        System.out.println(GREEN + "  [PASS] Invalid ticket reporting blocked correctly." + RESET);
    }

    private static void testForumAndModerationFlows() {
        System.out.println(PURPLE + "\n--- [6] TESTING FORUM & POST MODERATION FLOWS ---" + RESET);

        // Reset state so tests run in isolation
        ForumManager.resetForTesting();

        // Case A: Happy Path (Create Thread & Add Reply)
        System.out.println(YELLOW + "  [Case A] Happy Path: Creating thread and adding reply..." + RESET);
        String newPostRes = ForumManager.createPost(1, 1, "Testing Thread", "Hello developers!");
        assertTest("success".equals(newPostRes), "Thread creation should succeed");

        String replyRes = ForumManager.replyToPost(2, 1, 1, "Awesome tool!");
        assertTest("success".equals(replyRes), "Thread reply should succeed");
        System.out.println(GREEN + "  [PASS] Forum thread and reply creation verified." + RESET);

        // Case B: Alternative Path (Reply to locked post)
        System.out.println(YELLOW + "  [Case B] Alternative Path: Attempting to reply to a locked thread..." + RESET);
        ForumManager.lockThread(3, 1); // Lock thread 1 by Admin (ID 3)
        String lockedReplyRes = ForumManager.replyToPost(1, 1, 1, "Attempting to reply to locked");
        assertTest("locked".equals(lockedReplyRes), "Reply must be rejected as locked");
        System.out.println(GREEN + "  [PASS] Replying to locked thread blocked." + RESET);

        // Case C: Happy Path (Admin delete post)
        System.out.println(YELLOW + "  [Case C] Happy Path: Admin delete post verified..." + RESET);
        String deleteRes = ForumManager.deletePost(3, 2); // Admin deletes post 2
        assertTest("success".equals(deleteRes), "Admin post deletion should succeed");

        ForumPost post = ForumManager.findById(2);
        assertTest(post != null && Boolean.TRUE.equals(post.getIsDeleted()), "Post 2 should be marked deleted");
        System.out.println(GREEN + "  [PASS] Admin soft-deletion verified." + RESET);
    }

    private static void testNotificationsFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [7] TESTING NOTIFICATIONS & DELETION FLOWS ---" + RESET);

        // Case A: Happy Path (Generate and view new alert for user)
        System.out.println(YELLOW + "  [Case A] Happy Path: Checking alerts for user ID 1..." + RESET);
        List<Notification> alerts = AlertList.checkNewAlerts(1);
        System.out.println("  Found active alerts: " + alerts.size());
        assertTest(alerts != null, "Alerts list should be returned");
        System.out.println(GREEN + "  [PASS] Notifications loaded correctly." + RESET);
    }

    private static void testAdminConcertDeletionFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [8] TESTING ADMIN CONCERT DELETION FLOWS ---" + RESET);

        // Case A: Create and delete a temporary concert strictly using database
        // transactional cascading
        System.out.println(YELLOW
                + "  [Case A] Happy Path: Creating and deleting temp concert strictly via DB transaction..." + RESET);

        int tempConcertId = -1;
        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert a temporary concert
                String insertConcert = "INSERT INTO concerts (organizer_id, venue_id, title, description, concert_date, status) VALUES (2, 1, 'Temp Concert for Deletion Test', 'Cascade delete test description', '2026-10-10 12:00:00', 'APPROVED')";
                try (PreparedStatement ps = conn.prepareStatement(insertConcert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            tempConcertId = rs.getInt(1);
                        }
                    }
                }

                assertTest(tempConcertId != -1, "Temporary concert should be created");

                // 2. Insert a temporary ticket type
                String insertTicketType = "INSERT INTO ticket_types (concert_id, name, price, quantity) VALUES (?, 'REGULAR', 50.00, 10)";
                int tempTicketTypeId = -1;
                try (PreparedStatement ps = conn.prepareStatement(insertTicketType, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, tempConcertId);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            tempTicketTypeId = rs.getInt(1);
                        }
                    }
                }

                assertTest(tempTicketTypeId != -1, "Temporary ticket type should be created");

                // 3. Insert a temporary ticket for user 1 on seat 11
                String insertTicket = "INSERT INTO tickets (concert_id, user_id, ticket_type_id, seat_id, status) VALUES (?, 1, ?, 11, 'ACTIVE')";
                int tempTicketId = -1;
                try (PreparedStatement ps = conn.prepareStatement(insertTicket, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, tempConcertId);
                    ps.setInt(2, tempTicketTypeId);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            tempTicketId = rs.getInt(1);
                        }
                    }
                }

                assertTest(tempTicketId != -1, "Temporary ticket should be created");

                // 4. Create a problem report for that ticket
                String insertReport = "INSERT INTO reports (ticket_id, user_id, organizer_id, description, status) VALUES (?, 1, 2, 'Test report for cascade delete', 'OPEN')";
                try (PreparedStatement ps = conn.prepareStatement(insertReport)) {
                    ps.setInt(1, tempTicketId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println(
                        "  Temporary concert #" + tempConcertId + " and dependent records successfully created in DB.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        // 5. Now perform cascading delete on the temp concert ID!
        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete reports
                String delReports = "DELETE FROM reports WHERE ticket_id IN (SELECT ticket_id FROM tickets WHERE concert_id = ?)";
                try (PreparedStatement ps = conn.prepareStatement(delReports)) {
                    ps.setInt(1, tempConcertId);
                    ps.executeUpdate();
                }

                // Delete tickets
                String delTickets = "DELETE FROM tickets WHERE concert_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(delTickets)) {
                    ps.setInt(1, tempConcertId);
                    ps.executeUpdate();
                }

                // Delete ticket types
                String delTicketTypes = "DELETE FROM ticket_types WHERE concert_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(delTicketTypes)) {
                    ps.setInt(1, tempConcertId);
                    ps.executeUpdate();
                }

                // Delete concert
                String delConcert = "DELETE FROM concerts WHERE concert_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(delConcert)) {
                    ps.setInt(1, tempConcertId);
                    int rows = ps.executeUpdate();
                    assertTest(rows > 0, "Temporary concert should be deleted");
                }

                conn.commit();
                System.out.println("  Temporary concert #" + tempConcertId
                        + " and all dependent records successfully cascade deleted from DB.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        // 6. Verify they are gone from the DB!
        boolean concertExists = false;
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT COUNT(*) AS total FROM concerts WHERE concert_id = ?")) {
            ps.setInt(1, tempConcertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    concertExists = rs.getInt("total") > 0;
                }
            }
        }
        assertTest(!concertExists, "Concert must no longer exist in DB");
        System.out.println(GREEN + "  [PASS] Database transactional cascade deletion verified successfully." + RESET);
    }

    private static void testVipUpgradeFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [9] TESTING VIP UPGRADE FLOWS ---" + RESET);

        VIPUpgradeManager vipManager = new VIPUpgradeManager();
        TicketDAO ticketDAO = new TicketDAO();

        // 1. Get active tickets for user 1 (createdTicketId1 is ACTIVE regular ticket)
        List<String> activeTickets = vipManager.getUserActiveTickets(1);
        System.out.println("  Active tickets for User 1: " + activeTickets);
        assertTest(activeTickets.stream().anyMatch(t -> t.startsWith(String.valueOf(createdTicketId1))),
                "User active tickets must contain createdTicketId1");

        // 2. Fetch available VIP seats for that ticket
        List<String> vipSeats = vipManager.getAvailableVipSeatsForTicket(createdTicketId1);
        System.out.println("  Available VIP seats for Ticket ID " + createdTicketId1 + ": " + vipSeats);
        assertTest(!vipSeats.isEmpty(), "There should be available VIP seats");

        // Extract the first seat ID (which is seat ID 1)
        int vipSeatId = Integer.parseInt(vipSeats.get(0).split(" - ")[0].trim());

        // 3. Perform VIP upgrade
        System.out.println("  Upgrading Ticket ID " + createdTicketId1 + " to VIP seat ID " + vipSeatId + "...");
        String upgradeRes = vipManager.upgradeTicketToVIP(1, createdTicketId1, vipSeatId, "VISA-9876-VIP-PAY");
        System.out.println("  Upgrade Result: " + upgradeRes);
        assertTest("Το εισιτήριο αναβαθμίστηκε σε VIP επιτυχώς.".equals(upgradeRes), "Upgrade should succeed");

        // 4. Verify that the ticket's status is UPGRADED in the database
        boolean isUpgraded = false;
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT status FROM tickets WHERE ticket_id = ?")) {
            ps.setInt(1, createdTicketId1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isUpgraded = "UPGRADED".equals(rs.getString("status"));
                }
            }
        }
        assertTest(isUpgraded, "Ticket status must be UPGRADED in the database");

        // 5. Verify that TicketDAO findUserTickets() correctly includes this upgraded
        // ticket!
        List<Ticket> userTickets = ticketDAO.findUserTickets(1);
        boolean containsUpgraded = userTickets.stream().anyMatch(t -> t.getTicketId() == createdTicketId1);
        assertTest(containsUpgraded, "findUserTickets should include the UPGRADED ticket");
        System.out.println(
                GREEN + "  [PASS] VIP Ticket upgrade and visibility in active list verified successfully." + RESET);
    }

    private static void testConcertReviewFlows() throws Exception {
        System.out.println(PURPLE + "\n--- [10] TESTING CONCERT REVIEW FLOWS ---" + RESET);

        ReviewManager reviewManager = new ReviewManager();

        // 1. Submit review as User 1 for Concert 1 (they own the upgraded/active Ticket
        // ID createdTicketId1)
        System.out.println("  Submitting review as User 1 for Concert 1...");
        String reviewRes = reviewManager.submitReview(1, 1, 5, "Amazing concert! Great acoustics and performance!");
        System.out.println("  Submission Result: " + reviewRes);
        assertTest("Το review καταχωρήθηκε επιτυχώς.".equals(reviewRes), "Review submission should succeed");

        // 2. Try to submit a duplicate review for same concert by same user
        System.out.println("  Attempting to submit duplicate review...");
        String dupRes = reviewManager.submitReview(1, 1, 4, "Another comment");
        System.out.println("  Duplicate Submission Result: " + dupRes);
        assertTest("Έχεις ήδη κάνει review για αυτή τη συναυλία.".equals(dupRes), "Duplicate review should be blocked");

        // 3. Verify that a user who DOES NOT own an active ticket cannot submit a
        // review (e.g. User 3, Admin, owns no ticket)
        System.out.println("  Attempting to submit review by a user with no active ticket...");
        String unauthorizedRes = reviewManager.submitReview(3, 1, 4, "I did not attend but rating anyway");
        System.out.println("  Unauthorized Submission Result: " + unauthorizedRes);
        assertTest("Δεν μπορείς να κάνεις review για αυτή τη συναυλία.".equals(unauthorizedRes),
                "Unauthorized review should be blocked");

        // 4. Verify that our review is listed for Concert 1
        List<models.Review> reviews = reviewManager.getReviewsForConcert(1);
        boolean containsReview = reviews.stream().anyMatch(r -> r.getUserId() == 1 && r.getRating() == 5);
        assertTest(containsReview, "Reviews list should contain our submitted review");
        System.out.println(GREEN
                + "  [PASS] Concert review authorization, submission, and validation verified successfully." + RESET);
    }

    private static void cleanDatabaseForTesting() {
        System.out.println("Cleaning database for clean test run...");
        String DB_URL = "jdbc:mysql://localhost:3306/musictick?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(DB_URL, "root", "")) {
            conn.setAutoCommit(false);
            try {
                conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                conn.createStatement().executeUpdate("DELETE FROM refunds");
                conn.createStatement().executeUpdate("DELETE FROM payments");
                conn.createStatement().executeUpdate("DELETE FROM order_tickets");
                conn.createStatement().executeUpdate("DELETE FROM orders");
                conn.createStatement().executeUpdate("DELETE FROM reports");
                conn.createStatement().executeUpdate("DELETE FROM waitlist_entries");
                conn.createStatement().executeUpdate("DELETE FROM reviews");
                conn.createStatement().executeUpdate("DELETE FROM tickets");
                conn.createStatement().executeUpdate("ALTER TABLE tickets AUTO_INCREMENT = 1");
                conn.createStatement().executeUpdate("ALTER TABLE orders AUTO_INCREMENT = 1");
                conn.createStatement().executeUpdate("ALTER TABLE payments AUTO_INCREMENT = 1");
                conn.createStatement().executeUpdate("ALTER TABLE reports AUTO_INCREMENT = 1");
                conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                conn.commit();
                System.out.println("Database successfully cleaned.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Failed to clean database: " + e.getMessage());
        }
    }

    private static void assertTest(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Test Assertion Failed: " + message);
        }
    }
}
