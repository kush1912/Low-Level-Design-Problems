package lld.DesignProblems.bookmyshow.services;

import lld.DesignProblems.bookmyshow.enums.BookingStatus;
import lld.DesignProblems.bookmyshow.enums.SeatStatus;
import lld.DesignProblems.bookmyshow.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BookingService {
    private static final Comparator<Seat> SEAT_COMPARATOR = Comparator.comparing(Seat::row).thenComparingInt(Seat::column);

    private final SeatLockProvider seatLockProvider;
    private final Map<String, Booking> bookings;

    public BookingService(SeatLockProvider seatLockProvider) {
        this.seatLockProvider = seatLockProvider;
        this.bookings = new ConcurrentHashMap<>();
    }

    // Returns movies having at least one show in the selected city.
    public Set<Movie> getMovies(City city) {
        return city.getTheatres().stream()
                .flatMap(theatre -> theatre.getScreens().stream())
                .flatMap(screen -> screen.getShows().stream())
                .map(Show::getMovie)
                .collect(Collectors.toSet());
    }


    // Returns matching shows for the given city and movie.
    public Set<Show> getShows(City city, Movie movie) {
        Set<Show> shows = new HashSet<>();

        for (Theatre theatre : city.getTheatres()) {
            for (Screen screen : theatre.getScreens()) {
                for (Show show : screen.getShows()) {
                    if (show.getMovie().equals(movie)) {
                        shows.add(show);
                    }
                }
            }
        }

        return shows;
    }


    //getSeatAvailability(...) returns status and pricing for one show's seats
    public SortedMap<Seat, SeatStatus> getSeatAvailability(Show show) {
        // Use the same Show monitor as hold and confirmation so this method
        // sees one consistent snapshot of confirmed bookings and active locks.
        synchronized (show) {
            long currentTime = System.currentTimeMillis();
            Set<Seat> lockedSeats =
                    seatLockProvider.getLockedSeats(
                            show,
                            currentTime
                    );
            Set<Seat> bookedSeats = getBookedSeats(show);

            SortedMap<Seat, SeatStatus> availability =
                    new TreeMap<>(SEAT_COMPARATOR);

            for (Seat seat : show.getScreen().getSeats()) {
                if (bookedSeats.contains(seat)) {
                    availability.put(seat, SeatStatus.BOOKED);
                } else if (lockedSeats.contains(seat)) {
                    availability.put(seat, SeatStatus.HELD);
                } else {
                    availability.put(seat, SeatStatus.AVAILABLE);
                }
            }

            return Collections.unmodifiableSortedMap(
                    availability
            );
        }
    }


    // Hold Seats
    public Optional<Booking> holdSeats(Customer customer, Show show, Set<Seat> requestedSeats){
        if (customer == null || show == null || requestedSeats == null || requestedSeats.isEmpty() || !show.getScreen().getSeats().containsAll(requestedSeats)) {
            return Optional.empty();
        }

        long currentTime = System.currentTimeMillis();
        long expiresAt = currentTime + 5 * 60 * 1000L;
        String bookingId = "B" + UUID.randomUUID();

        /*
         * This lock protects the complete check-then-act operation across
         * BookingService.bookings and SeatLockProvider.locks.
         *
         * Without it:
         * 1. C2 checks A1 and sees that it is not confirmed.
         * 2. C1 confirms A1 and removes its temporary SeatLock.
         * 3. C2 uses its stale check and successfully locks A1.
         *
         * With the same Show monitor used during confirmation, C2 either sees
         * C1's active lock or sees A1 in a confirmed booking.
         * Different shows use different monitors and proceed concurrently.
         *
         * The frontend normally disables HELD or BOOKED seats, so this action
         * should not be possible through an up-to-date UI. The backend must
         * still enforce it because the UI snapshot may be stale, requests may
         * race, or a caller may invoke the API directly.
         */

        synchronized (show) {
            // It checks whether any of the customer’s requested seats are already booked.
            /*
            Collections.disjoint(a, b) returns:
                true  → the collections have no common element
                false → at least one element exists in both collections
            * */
            if (!Collections.disjoint(getBookedSeats(show), requestedSeats)) {
                return Optional.empty();
            }
            Booking booking = new Booking(bookingId, customer, show, requestedSeats);

            boolean locked = seatLockProvider.tryLockSeats(show, requestedSeats, bookingId, customer, expiresAt, currentTime);

            if (!locked) {
                return Optional.empty();
            }

            bookings.put(bookingId, booking);
            return Optional.of(booking);
        }
    }

    // HELD - BOOKED
    public boolean confirmBooking(String bookingId, String paymentReference) {
        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            return false;
        }

        /*
         * Validate locks, confirm the booking, and remove temporary locks as
         * one per-Show transaction. A competing hold cannot run between these
         * steps and acquire a seat during the HELD-to-BOOKED transition.
         */
//        Confirmation changes 2 Stores:
//            1. BookingService.bookings
//            2. SeatLockProvider.locks
        synchronized (booking.getShow()) {
            // Duplicate Confirmation Callbacks
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                return booking.confirm(paymentReference);
            }

            // Verify Selected Seat Lock Exists within time
            boolean validLocks = seatLockProvider.validateLocks(booking, System.currentTimeMillis());

//            Valid locks and Valid Payment reference
            if (!validLocks || !booking.confirm(paymentReference)) {
                return false;
            }

            seatLockProvider.unlockSeats(booking);
            return true;
        }
    }


    // Compensating Behavior
    public boolean releaseHold(String bookingId) {
        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            return false;
        }

        // Releasing hold changes 2 related States and should be one business operation.
        //    1. Booking Status
        //    2. Seat Locks
        synchronized (booking.getShow()) {
            // INITIATED -> FAILED
            if (!booking.fail()) {
                return false;
            }
            seatLockProvider.unlockSeats(booking);
            return true;
        }
    }


    // Check into existing Bookings and requested seats, and if they are confirmed, it returns the set of successfully booked seats.
    private Set<Seat> getBookedSeats(Show show) {
        return bookings.values().stream()
                .filter(booking ->
                        booking.getShow().equals(show)
                                && booking.getStatus()
                                == BookingStatus.CONFIRMED
                )
                .flatMap(booking ->
                        booking.getSeats().stream()
                )
                .collect(Collectors.toSet());
    }

}
