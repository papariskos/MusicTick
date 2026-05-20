package models;

import java.time.LocalDateTime;

public class PostReport {
    private Integer postReportId;
    private Integer postId;
    private Integer userId;
    private String reason;
    private LocalDateTime createdAt;

    public PostReport() {}

    public PostReport(Integer postReportId, Integer postId, Integer userId, String reason, LocalDateTime createdAt) {
        this.postReportId = postReportId;
        this.postId = postId;
        this.userId = userId;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Integer getPostReportId() { return postReportId; }
    public void setPostReportId(Integer postReportId) { this.postReportId = postReportId; }
    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
