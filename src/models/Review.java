package models;

import java.time.LocalDateTime;

public class Review {
    private Integer reviewId;
    private Integer concertId;
    private Integer userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review() {}

    public Review(Integer reviewId, Integer concertId, Integer userId, Integer rating, String comment, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.concertId = concertId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getConcertId() { return concertId; }
    public void setConcertId(Integer concertId) { this.concertId = concertId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
