package models;

import java.time.LocalDateTime;
import models.enums.ConcertStatus;

public class Concert {
    private Integer concertId;
    private Integer organizerId;
    private Integer venueId;
    private String title;
    private String description;
    private LocalDateTime concertDate;
    private ConcertStatus status;
    private Double averageRating;
    private LocalDateTime createdAt;

    public Concert() {}

    public Concert(Integer concertId, Integer organizerId, Integer venueId, String title, String description, LocalDateTime concertDate, ConcertStatus status, Double averageRating, LocalDateTime createdAt) {
        this.concertId = concertId;
        this.organizerId = organizerId;
        this.venueId = venueId;
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.status = status;
        this.averageRating = averageRating;
        this.createdAt = createdAt;
    }

    public Integer getConcertId() { return concertId; }
    public void setConcertId(Integer concertId) { this.concertId = concertId; }
    public Integer getOrganizerId() { return organizerId; }
    public void setOrganizerId(Integer organizerId) { this.organizerId = organizerId; }
    public Integer getVenueId() { return venueId; }
    public void setVenueId(Integer venueId) { this.venueId = venueId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getConcertDate() { return concertDate; }
    public void setConcertDate(LocalDateTime concertDate) { this.concertDate = concertDate; }
    public ConcertStatus getStatus() { return status; }
    public void setStatus(ConcertStatus status) { this.status = status; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
