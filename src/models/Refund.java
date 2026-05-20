package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import models.enums.RefundStatus;

public class Refund {
    private Integer refundId;
    private Integer paymentId;
    private BigDecimal amount;
    private RefundStatus refundStatus;
    private LocalDateTime refundDate;

    public Refund() {}

    public Refund(Integer refundId, Integer paymentId, BigDecimal amount, RefundStatus refundStatus, LocalDateTime refundDate) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.refundStatus = refundStatus;
        this.refundDate = refundDate;
    }

    public Integer getRefundId() { return refundId; }
    public void setRefundId(Integer refundId) { this.refundId = refundId; }
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }
    public LocalDateTime getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDateTime refundDate) { this.refundDate = refundDate; }
}
