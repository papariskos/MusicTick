package com.musictick.manager;

public class CancellationPolicyDB {
    public static String checkCancellationPolicy(String reservationData) {
        System.out.println("CancellationPolicyDB: checkCancellationPolicy() called for reservationData=" + reservationData);
        // If reservation data contains NO_CANCEL, we simulate a not allowed cancellation
        if (reservationData != null && reservationData.toUpperCase().contains("NO_CANCEL")) {
            System.out.println("CancellationPolicyDB: returnEligibility() -> cancellationNotAllowed");
            return "cancellationNotAllowed";
        }
        System.out.println("CancellationPolicyDB: returnEligibility() -> cancellationAllowed");
        return "cancellationAllowed";
    }
}
