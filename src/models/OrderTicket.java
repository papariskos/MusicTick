package models;

public class OrderTicket {
    private Integer orderId;
    private Integer ticketId;

    public OrderTicket() {}

    public OrderTicket(Integer orderId, Integer ticketId) {
        this.orderId = orderId;
        this.ticketId = ticketId;
    }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }
}
