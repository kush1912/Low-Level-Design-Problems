package lld.DesignProblems.bookmyshow.model;

import java.util.HashSet;
import java.util.Set;

public class Screen {
    private final String name;
    private final int totalRows;
    private final int seatsPerRow;
    private final Set<Show> shows;
    private final Set<Seat> seats;

    public Screen(String name, int totalRows, int seatsPerRow){
        this.name = name;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        this.shows = new HashSet<>();
        this.seats = new HashSet<>();
    }

    public String getScreenName(){
        return this.name;
    }

    public Set<Show> getShows(){
        return Set.copyOf(shows);
    }

    public Set<Seat> getSeats(){
        return Set.copyOf(seats);
    }

    public boolean addSeat(Seat seat) {
        boolean positionAlreadyExists = seats.stream()
                .anyMatch(existing ->
                        existing.row().equals(seat.row())
                        && existing.column() == seat.column()
                );

        if (positionAlreadyExists
                || seats.size() >= totalRows * seatsPerRow) {
            return false;
        }

        return seats.add(seat);
    }

    public boolean addShow(Show show){
        for (Show existingShow : shows) {
            if (show.overlapsWith(existingShow)) {
                return false;
            }
        }
        return shows.add(show);
    }
}
