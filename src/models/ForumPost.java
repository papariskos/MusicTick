package models;

import java.time.LocalDateTime;

public class ForumPost {
    private Integer postId;
    private Integer concertId;
    private Integer userId;
    private Integer parentPostId;
    private String title;
    private String content;
    private Boolean isLocked;
    private Boolean isDeleted;
    private LocalDateTime createdAt;

    public ForumPost() {}

    public ForumPost(Integer postId, Integer concertId, Integer userId, Integer parentPostId, String title, String content, Boolean isLocked, Boolean isDeleted, LocalDateTime createdAt) {
        this.postId = postId;
        this.concertId = concertId;
        this.userId = userId;
        this.parentPostId = parentPostId;
        this.title = title;
        this.content = content;
        this.isLocked = isLocked;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }
    public Integer getConcertId() { return concertId; }
    public void setConcertId(Integer concertId) { this.concertId = concertId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getParentPostId() { return parentPostId; }
    public void setParentPostId(Integer parentPostId) { this.parentPostId = parentPostId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
