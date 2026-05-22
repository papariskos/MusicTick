package com.musictick;

public class Session {
    private static int currentUserId = 0;
    private static String currentUserRole = "CUSTOMER";

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void setCurrentUserRole(String role) {
        currentUserRole = role != null ? role : "CUSTOMER";
    }
}
