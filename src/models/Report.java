package models;

import java.time.LocalDateTime;
import models.enums.ReportStatus;

public class Report {
    private Integer reportId;
    private Integer ticketId;
    private Integer userId;
    private Integer organizerId;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;

    public Report() {}

    public Report(Integer reportId, Integer ticketId, Integer userId, Integer organizerId, String description, ReportStatus status, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.ticketId = ticketId;
        this.userId = userId;
        this.organizerId = organizerId;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }
    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getOrganizerId() { return organizerId; }
    public void setOrganizerId(Integer organizerId) { this.organizerId = organizerId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
