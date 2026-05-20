package models;

public class Seat {
    private Integer seatId;
    private Integer venueId;
    private String sectionName;
    private String rowLabel;
    private String seatNumber;
    private String seatType;

    public Seat() {}

    public Seat(Integer seatId, Integer venueId, String sectionName, String rowLabel, String seatNumber, String seatType) {
        this.seatId = seatId;
        this.venueId = venueId;
        this.sectionName = sectionName;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public Integer getSeatId() { return seatId; }
    public void setSeatId(Integer seatId) { this.seatId = seatId; }
    public Integer getVenueId() { return venueId; }
    public void setVenueId(Integer venueId) { this.venueId = venueId; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
}
