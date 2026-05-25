package com.musictick.manager;

import com.musictick.dao.ConcertDAO;
import models.Concert;
import models.Seat;

import java.sql.SQLException;
import java.util.List;

public class SearchManager {
    private final ConcertDAO concertDAO = new ConcertDAO();

    public List<Concert> searchConcerts(String terms) throws SQLException {
        return concertDAO.searchConcerts(terms);
    }

    public boolean checkAvailability(int concertId) throws SQLException {
        List<Seat> seats = concertDAO.getAvailableSeats(concertId);
        return !seats.isEmpty();
    }

    public List<Seat> getAvailableSeats(int concertId) throws SQLException {
        return concertDAO.getAvailableSeats(concertId);
    }
}
