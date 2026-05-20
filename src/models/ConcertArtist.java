package models;

public class ConcertArtist {
    private Integer concertId;
    private Integer artistId;

    public ConcertArtist() {}

    public ConcertArtist(Integer concertId, Integer artistId) {
        this.concertId = concertId;
        this.artistId = artistId;
    }

    public Integer getConcertId() { return concertId; }
    public void setConcertId(Integer concertId) { this.concertId = concertId; }
    public Integer getArtistId() { return artistId; }
    public void setArtistId(Integer artistId) { this.artistId = artistId; }
}
