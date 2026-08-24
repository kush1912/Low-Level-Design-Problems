# BookMyShow - Interview Scope

## Time Constraint

- Design and implement an interview-sized in-memory solution in 45-60 minutes.
- Prioritize one correct end-to-end booking flow over production infrastructure.
- Focus on seat ownership, temporary holds, atomic booking, and concurrency.
- State production limitations and extensions instead of partially implementing them.

## Core Use Cases

- Browse movies available in a city.
- View theatres and scheduled shows for a selected movie.
- View a show's seat layout, availability, and prices.
- Select and temporarily hold one or more available seats.
- Confirm the booking after successful payment.
- Release held seats when payment fails or the hold expires.
- Retrieve the final booking and its status.

### Core API Checklist

```java
// Discovery
Set<Movie> getMovies(City city);

Set<Show> getShows(City city, Movie movie);

Map<Seat, SeatStatus> getSeatAvailability(Show show);

// Booking
Optional<Booking> holdSeats(
        String customerId,
        Show show,
        Set<Seat> requestedSeats
);

boolean confirmBooking(
        String bookingId,
        String paymentReference
);

boolean releaseHold(String bookingId);

Optional<Booking> getBooking(String bookingId);
```

- `getMovies(...)` returns movies having at least one show in the selected city.
- `getShows(...)` returns matching shows with theatre, screen, and timing information.
- `getSeatAvailability(...)` returns status and pricing for one show's seats.
- `holdSeats(...)` atomically holds all requested seats or holds none of them.
- `confirmBooking(...)` succeeds only for an unexpired hold owned by that booking and must be idempotent for duplicate payment callbacks.
- `releaseHold(...)` returns still-held seats to `AVAILABLE`; calling it after release should not corrupt state.
- `getBooking(...)` returns the booking, selected seats, amount, and current status.
- Hold-expiry cleanup is internal behavior rather than a customer-facing API; it may run lazily during availability, hold, or confirmation operations.

## Core Domain

```text
Movie -> Theatre -> Screen -> Show -> Booking
```

- A `Movie` can have multiple scheduled shows.
- A `Theatre` contains one or more screens.
- A `Screen` defines the reusable physical seat layout.
- A `Show` schedules a movie on a screen for a specific time.
- A `Booking` groups seats held by one customer for one show.
- `SeatInventoryService` maps each show's unavailable seats to their owning pending or confirmed booking.

## Critical Modeling Decision

- Availability is derived from the selected show's inventory in `SeatInventoryService`, not stored in the reusable physical `Seat`.
- The same physical seat may be booked for one show and available for another.

```text
Seat A10 for the 3 PM show -> BOOKED
Seat A10 for the 7 PM show -> AVAILABLE
```

## Required States

```text
ShowSeat:
AVAILABLE -> HELD -> BOOKED
                 -> AVAILABLE when the hold expires or payment fails

Booking:
PENDING_PAYMENT -> CONFIRMED
                -> FAILED
                -> EXPIRED
                -> CANCELLED
```

## Required Booking Flow

```text
Search shows
-> View available seats
-> Hold selected seats for a limited time
-> Initiate payment
-> Payment succeeds: HELD -> BOOKED
-> Payment fails or expires: HELD -> AVAILABLE
```

## Critical Invariant

> A seat for a specific show must never be confirmed for more than one booking, even when multiple customers attempt to select it concurrently.

- Holding multiple seats must be atomic: either all requested seats are held or none are.
- Only the booking that owns a valid hold may confirm those seats.
- An expired hold cannot be confirmed.
- Duplicate payment callbacks must not create duplicate confirmations.

## Concurrency Scenarios to Discuss

- Two customers attempt to hold the same seat simultaneously.
- A multi-seat request finds that one requested seat is unavailable.
- A hold expires while payment is being processed.
- Payment succeeds after the hold has expired.
- The payment provider sends the same success callback more than once.
- The application fails after payment succeeds but before booking confirmation.

## Interview-Sized Implementation

- Use in-memory repositories and a deterministic clock where time is required.
- Make the hold duration configurable.
- Protect the complete seat-state transition rather than individual reads.
- Demonstrate one successful booking and one competing request for the same seat.
- Explain database transactions, optimistic locking, and idempotency as production extensions.

## Explicitly Deferred

- Search ranking and recommendations.
- Coupons, taxes, surge pricing, and complex pricing rules.
- Food ordering.
- Reviews and ratings.
- Real payment-gateway integration.
- Notifications and refund processing.
- Database sharding, distributed locking, and multi-region deployment.
- User authentication, authorization, and account management.
