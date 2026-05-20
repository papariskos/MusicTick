package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import models.enums.OrderStatus;

public class Order {
    private Integer orderId;
    private Integer userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;

    public Order() {}

    public Order(Integer orderId, Integer userId, BigDecimal totalAmount, OrderStatus status, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
    }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
}
