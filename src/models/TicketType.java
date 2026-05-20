package models;

import java.math.BigDecimal;
import models.enums.TicketTypeName;

public class TicketType {
    private Integer ticketTypeId;
    private Integer concertId;
    private TicketTypeName name;
    private BigDecimal price;
    private Integer quantity;

    public TicketType() {}

    public TicketType(Integer ticketTypeId, Integer concertId, TicketTypeName name, BigDecimal price, Integer quantity) {
        this.ticketTypeId = ticketTypeId;
        this.concertId = concertId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Integer getTicketTypeId() { return ticketTypeId; }
    public void setTicketTypeId(Integer ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public Integer getConcertId() { return concertId; }
    public void setConcertId(Integer concertId) { this.concertId = concertId; }
    public TicketTypeName getName() { return name; }
    public void setName(TicketTypeName name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
