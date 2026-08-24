package lld.DesignProblems.bookmyshow.services;

import lld.DesignProblems.bookmyshow.model.Booking;
import lld.DesignProblems.bookmyshow.model.Customer;
import lld.DesignProblems.bookmyshow.model.Seat;
import lld.DesignProblems.bookmyshow.model.SeatLock;
import lld.DesignProblems.bookmyshow.model.Show;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SeatLockProvider {
    //Both threads may call computeIfAbsent concurrently. ConcurrentHashMap ensures that one inner map is safely created for each show, or accessed by each user.
    //It coordinates threads, not users directly.
    /*
    * Why Internal Map is not ConcurrentHashMap?
    *  The requirement is:
    *   - All requested seats are locked
    *   - No requested seats are locked
    * A ConcurrentHashMap cannot provide one atomic operation across multiple keys(Setas),that is why we used synchronized Inside.
    * Example:
        C1 requests A1 and A2
        C2 requests A2 and A3
        - Using only putIfAbsent could produce:
            - C1 locks A1
            - C2 locks A2
            - C1 fails to lock A2
            - C2 locks A3
       - Now C1 has partially acquired A1 even though its complete request failed. You would need rollback, and rollback introduces additional races.
      Example 2:
        C1 checks A1 in showLocks → absent
        C2 checks A1 in showLocks → absent

        C1 inserts A1 → Lock for B1
        C2 inserts A1 → Lock for B2

        C1 returns success
        C2 returns success

        Because the inner map is a normal HashMap, C2 may overwrite C1’s entry:
            A1 → Lock for B2
        But both customers received successful hold results. B1 now exists without owning its expected lock.
    * */

    private final Map<Show, Map<Seat, SeatLock>> locks = new ConcurrentHashMap<>();

    public boolean tryLockSeats(Show show, Set<Seat> seats, String bookingId, Customer customer, long expiresAt, long currentTime) {
        // retrieves the seat-lock map for a show, creating it if this is the first booking attempt for that show:
        Map<Seat, SeatLock> showLocks = locks.computeIfAbsent(show, missingShow -> new HashMap<>());

        //synchronized allows one thread at a time to perform the seat-lock transaction;
        // it does not allow only one customer to hold seats for the entire show
        /*
            Without this the inner HashMap is not threadSafe , it may lead to:
            - C2 Overwriting C1s Entry and B1 now exists without owning its expected locks
            - For multiple Seats partial holds are also possible es axplained in above example
        */
        synchronized (showLocks) {
            //this removes all expired seat locks from the current show’s map.
            showLocks.values().removeIf(seatLock -> seatLock.isExpired(currentTime));

            for (Seat seat : seats) {
                if (showLocks.containsKey(seat)) {
                    return false;
                }
            }

            //put lock for all selected Seats
            for (Seat seat : seats) {
                showLocks.put(seat, new SeatLock(seat, show, bookingId, customer, expiresAt));
            }
            return true;
        }
    }


    public boolean validateLocks(Booking booking, long currentTime) {
        Map<Seat, SeatLock> showLocks = locks.get(booking.getShow());

        if (showLocks == null) {
            return false;
        }

        synchronized (showLocks) {
            showLocks.values().removeIf(seatLock -> seatLock.isExpired(currentTime));

            for (Seat seat : booking.getSeats()) {
                SeatLock seatLock = showLocks.get(seat);

                //Seat locked by someone and booking made by someone are same person or not
                if (seatLock == null || !seatLock.bookingId().equals(booking.getBookingId())) {
                    return false;
                }
            }
            return true;
        }
    }

    public void unlockSeats(Booking booking) {
        Map<Seat, SeatLock> showLocks = locks.get(booking.getShow());

        if (showLocks == null) {
            return;
        }

        synchronized (showLocks) {
            for (Seat seat : booking.getSeats()) {
                SeatLock seatLock = showLocks.get(seat);

                if (seatLock != null && seatLock.bookingId().equals(booking.getBookingId())) {
                    showLocks.remove(seat);
                }
            }
        }
    }

    //Return all seats that currently have an active, unexpired lock for a particular show.
    public Set<Seat> getLockedSeats(Show show, long currentTime) {
        Map<Seat, SeatLock> showLocks = locks.get(show);

        if (showLocks == null) {
            return Collections.emptySet();
        }

        synchronized (showLocks) {
            showLocks.values().removeIf(seatLock -> seatLock.isExpired(currentTime));
            return Set.copyOf(showLocks.keySet());
        }
    }
}
