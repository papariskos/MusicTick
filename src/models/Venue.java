package models;

public class Venue {
    private Integer venueId;
    private String name;
    private String city;
    private String address;
    private Integer capacity;

    public Venue() {}

    public Venue(Integer venueId, String name, String city, String address, Integer capacity) {
        this.venueId = venueId;
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
    }

    public Integer getVenueId() { return venueId; }
    public void setVenueId(Integer venueId) { this.venueId = venueId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
