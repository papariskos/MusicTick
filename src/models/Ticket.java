package models;

import java.time.LocalDateTime;
import models.enums.TicketStatus;

public class Ticket {
    private Integer ticketId;
    private Integer concertId;
    private Integer userId;
    private Integer ticketTypeId;
    private Integer seatId;
    private TicketStatus status;
    private String qrCode;
    private LocalDateTime purchaseDate;

    public Ticket() {}

    public Ticket(Integer ticketId, Integer concertId, Integer userId, Integer ticketTypeId, Integer seatId, TicketStatus status, String qrCode, LocalDateTime purchaseDate) {
        this.ticketId = ticketId;
        this.concertId = concertId;
        this.userId = userId;
        this.ticketTypeId = ticketTypeId;
        this.seatId = seatId;
        this.status = status;
        this.qrCode = qrCode;
        this.purchaseDate = purchaseDate;
    }

    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }
    public Integer getConcertId() { return concertId; }
    public void setConcertId(Integer concertId) { this.concertId = concertId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getTicketTypeId() { return ticketTypeId; }
    public void setTicketTypeId(Integer ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public Integer getSeatId() { return seatId; }
    public void setSeatId(Integer seatId) { this.seatId = seatId; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
}
