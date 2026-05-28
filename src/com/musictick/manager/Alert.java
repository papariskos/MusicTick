package com.musictick.manager;

public class Alert {
    private String title;
    private String message;

    public Alert(String title, String message) {
        this.title = title;
        this.message = message;
        System.out.println("Alert: <<create>> create(alertDetails) -> Title: " + title + ", Message: " + message);
        System.out.println("Alert: alertDetails()");
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
}
