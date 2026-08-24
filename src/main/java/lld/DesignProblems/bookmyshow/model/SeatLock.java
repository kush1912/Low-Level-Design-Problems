package lld.DesignProblems.bookmyshow.model;

public record SeatLock(
        Seat seat, // Which Seat
        Show show, // Which Show
        String bookingId, // Which booking attempted
        Customer customer, // by whom
        long expiresAt // Expires at
) {
    public boolean isExpired(long currentTime) {
        return currentTime >= expiresAt;
    }
}
