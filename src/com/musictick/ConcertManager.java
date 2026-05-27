package com.musictick;

import models.Concert;
import models.enums.ConcertStatus;
import models.Ticket;
import models.Refund;
import models.enums.TicketStatus;
import models.enums.RefundStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConcertManager {
    

    private List<Concert> concerts = new ArrayList<>();
    


    public boolean createConcert(String title, String description, LocalDateTime date, Integer venueId, Integer organizerId) {
        if (title == null || title.isEmpty() || date == null || date.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        Concert newConcert = new Concert();
        newConcert.setConcertId(concerts.size() + 1);
        newConcert.setTitle(title);
        newConcert.setDescription(description);
        newConcert.setConcertDate(date);
        newConcert.setVenueId(venueId);
        newConcert.setOrganizerId(organizerId);
        newConcert.setStatus(ConcertStatus.PENDING); // Αρχική κατάσταση
        newConcert.setCreatedAt(LocalDateTime.now());
        
        concerts.add(newConcert);
        System.out.println("Concert Created: " + title);
        return true;
    }


    public boolean cancelConcertAndRefund(Integer concertId) {
        // 1. Εύρεση της συναυλίας
        Concert concertToCancel = null;
        for (Concert c : concerts) {
            if (c.getConcertId().equals(concertId)) {
                concertToCancel = c;
                break;
            }
        }

        if (concertToCancel == null) return false;


        concertToCancel.setStatus(ConcertStatus.CANCELLED);
        
        // 3. Εύρεση εισιτηρίων και δημιουργία Refund
        System.out.println("Processing refunds for concert: " + concertToCancel.getTitle());
        
        // Mock διαδικασία επιστροφής
        processMockRefunds(concertId);
        
        return true;
    }

    private void processMockRefunds(Integer concertId) {
        System.out.println("All tickets for concert " + concertId + " have been refunded.");
    }
}
