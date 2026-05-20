package models;

import java.time.LocalDateTime;
import models.enums.WaitlistStatus;

public class WaitlistEntry {
    private Integer waitlistId;
    private Integer concertId;
    private Integer userId;
    private Integer priorityOrder;
    private WaitlistStatus status;
    private LocalDateTime joinedAt;

    public WaitlistEntry() {}

    public WaitlistEntry(Integer waitlistId, Integer concertId, Integer userId, Integer priorityOrder, WaitlistStatus status, LocalDateTime joinedAt) {
        this.waitlistId = waitlistId;
        this.concertId = concertId;
        this.userId = userId;
        this.priorityOrder = priorityOrder;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public Integer getWaitlistId() { return waitlistId; }
    public void setWaitlistId(Integer waitlistId) { this.waitlistId = waitlistId; }
    public Integer getConcertId() { return concertId; }
    public void setConcertId(Integer concertId) { this.concertId = concertId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(Integer priorityOrder) { this.priorityOrder = priorityOrder; }
    public WaitlistStatus getStatus() { return status; }
    public void setStatus(WaitlistStatus status) { this.status = status; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
