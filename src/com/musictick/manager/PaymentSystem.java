package com.musictick.manager;

public class PaymentSystem {
    public static String validateCard(String cardData) {
        System.out.println("PaymentSystem: validateCard() called for cardData=" + cardData);
        if (cardData != null && cardData.toUpperCase().contains("FAIL")) {
            System.out.println("PaymentSystem: returnCardValidationStatus() -> invalidCard");
            return "invalidCard";
        }
        System.out.println("PaymentSystem: returnCardValidationStatus() -> validCard");
        return "validCard";
    }

    public static void refundPayment(String cardData) {
        System.out.println("PaymentSystem: refundPayment() called for cardData=" + cardData);
        System.out.println("PaymentSystem: refundCompleted()");
    }
}
