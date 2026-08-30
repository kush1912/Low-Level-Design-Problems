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

## ConcurrentHashMap Versus Compound-Operation Locking

`ConcurrentHashMap` makes a single map operation thread-safe:

```java
bookings.put(bookingId, booking);
bookings.get(bookingId);
locks.computeIfAbsent(show, ignored -> new HashMap<>());
```

For example, two threads can safely execute:

```java
bookings.put("B1", booking1);
bookings.put("B2", booking2);
```

The internal map structure will not be corrupted.

However, booking involves a sequence of related operations:

```text
Check A1
Check A2
Lock A1
Lock A2
```

A concurrent collection does not automatically treat this complete sequence as
one transaction.

### Race with Individual Concurrent Operations

Suppose the inner map were also a `ConcurrentHashMap`:

```java
Map<Seat, SeatLock> showLocks =
        new ConcurrentHashMap<>();
```

C1 and C2 execute:

```text
C1 requests A1, A2
C2 requests A2, A3

C1 checks A1 -> free
C2 checks A2 -> free
C1 locks A1
C2 locks A2
C1 checks A2 -> occupied
```

C1 failed to acquire all requested seats, but A1 is already partially locked.

The required business invariant is:

```text
Lock all requested seats
OR
Lock none of them
```

Therefore, we protect the complete operation:

```java
synchronized (showLocks) {
    for (Seat seat : requestedSeats) {
        if (showLocks.containsKey(seat)) {
            return false;
        }
    }

    for (Seat seat : requestedSeats) {
        showLocks.put(seat, new SeatLock(...));
    }

    return true;
}
```

### Two Levels in Our Code

```text
ConcurrentHashMap
-> protects individual storage operations and concurrent map access

synchronized(showLocks)
-> protects the multi-seat check-and-lock transaction

synchronized(show)
-> protects operations spanning bookings and temporary locks
```

For example, confirmation crosses two structures:

```text
BookingService.bookings:
B1 -> INITIATED

SeatLockProvider.locks:
A1 -> B1
```

The transition:

```text
Validate A1's lock
-> B1 becomes CONFIRMED
-> remove A1's temporary lock
```

must be atomic. That is why `ConcurrentHashMap` alone is insufficient.

> Thread-safe collections protect their own individual operations. They do not
> automatically protect a business transaction involving multiple keys or
> multiple collections.

## Single-JVM Limitations

Our locks and maps exist in the Java process's heap:

```text
JVM 1:
BookingService
SeatLockProvider
synchronized monitors
```

This works when every request reaches that one JVM.

Production applications commonly run several instances:

```text
                    Load Balancer
                   /             \
             Application 1    Application 2
```

Each instance has independent memory:

```text
Application 1:
locks = {}
bookings = {}

Application 2:
locks = {}
bookings = {}
```

Now consider:

```text
C1 requests A1 -> routed to Application 1
C2 requests A1 -> routed to Application 2
```

Application 1 synchronizes on its `Show` object:

```java
synchronized (showOnInstance1)
```

Application 2 synchronizes on a completely different object:

```java
synchronized (showOnInstance2)
```

Neither monitor knows about the other. Both instances see A1 as available and
both may hold it.

Other in-memory limitations include:

- Restarting the process loses bookings and locks.
- A deployment clears the state.
- Autoscaling creates additional independent copies.
- Different instances may have clock differences when evaluating expiry.
- Sticky sessions do not provide reliable correctness because instances may
  fail or be rebalanced.

> Java synchronization provides thread safety inside one JVM, not distributed
> consistency across application instances.

Production concurrency must use storage shared by every instance.

## Production Evolution Using Database Transactions

A relational database can maintain one row per show-seat:

```text
ShowSeat
--------------------------------
show_id
seat_id
status
booking_id
hold_expires_at
version
```

The unique identity is:

```text
PRIMARY KEY (show_id, seat_id)
```

Example:

```text
SHOW-10 | A1 | AVAILABLE | null | null
SHOW-10 | A2 | HELD      | B1   | 10:05
SHOW-10 | A3 | BOOKED    | B2   | null
```

### Pessimistic Transaction

To hold A1 and A2:

```text
BEGIN TRANSACTION

SELECT the A1 and A2 rows FOR UPDATE
-> other transactions must wait

Check both rows:
AVAILABLE or expired HELD?

If both are available:
-> update both to HELD by B1
-> COMMIT

If either is unavailable:
-> ROLLBACK
```

Conceptual SQL:

```sql
SELECT *
FROM show_seat
WHERE show_id = :showId
  AND seat_id IN (:seatIds)
FOR UPDATE;
```

Then:

```sql
UPDATE show_seat
SET status = 'HELD',
    booking_id = :bookingId,
    hold_expires_at = :expiresAt
WHERE show_id = :showId
  AND seat_id = :seatId;
```

The database transaction replaces:

```java
synchronized (showLocks)
```

All application instances coordinate through the same database rows.

### Optimistic Locking

Each row can contain:

```text
version = 5
```

The application reads version 5 and attempts:

```sql
UPDATE show_seat
SET status = 'HELD',
    booking_id = :bookingId,
    version = version + 1
WHERE show_id = :showId
  AND seat_id = :seatId
  AND version = 5
  AND status = 'AVAILABLE';
```

If another request changed the row first, the update count is zero. The
application rejects or retries the request.

Optimistic locking works well when conflicts are uncommon. For a very popular
show, retries may become expensive.

### Confirmation

Confirmation should also be transactional:

```text
Verify every row:
status = HELD
booking_id = B1
hold_expires_at > now

Then:
Booking B1 -> CONFIRMED
ShowSeat rows -> BOOKED
Commit together
```

The database becomes the durable source of truth.

## Production Evolution Using Redis

Redis naturally supports temporary data with expiration.

One seat hold could be:

```text
Key:   seat-lock:SHOW-10:A1
Value: B1
TTL:   300 seconds
```

After five minutes, Redis automatically removes the key.

Basic single-seat acquisition:

```text
SET seat-lock:SHOW-10:A1 B1 NX EX 300
```

Meaning:

```text
NX -> set only if the key does not exist
EX -> automatically expire it
```

Only one request can acquire A1.

### Why a Lua Script Is Needed

For A1 and A2, separate Redis commands could partially succeed:

```text
SET A1 -> succeeds
SET A2 -> fails
```

A Lua script can execute atomically:

```text
Check A1 and A2
If either exists:
    return failure

Otherwise:
    create both keys with B1 and TTL
    return success
```

Redis executes the script as one atomic operation.

### Safe Release

Do not blindly delete a seat key:

```text
DEL seat-lock:SHOW-10:A1
```

The old B1 request might delete a newer B2 lock.

Release must compare ownership:

```text
If current value == B1:
    delete key
```

This is also commonly implemented through a Lua script.

### Redis and Database Together

A typical split is:

```text
Redis
-> fast temporary holds and TTL

Database
-> durable bookings and final confirmed-seat ownership
```

The difficult part is keeping them consistent. Confirmation must ensure that:

```text
The Redis hold still belongs to B1
AND
The database seat has not already been confirmed
```

For the strongest correctness, the database remains the final authority. Redis
may improve performance, but a confirmed booking must still satisfy database
constraints.

## Hot-Show Scalability

Our current implementation synchronizes per Show:

```java
synchronized (show)
```

This is much better than one global lock because different shows proceed
independently:

```text
3 PM Show -> one monitor
7 PM Show -> another monitor
```

However, one extremely popular Show becomes a hot key:

```text
10,000 users all requesting seats for the same opening-night Show
```

All hold and confirmation operations for that Show wait for the same monitor.

Our current availability method also synchronizes on `show`, so many read
requests may contend with booking writes.

### Possible Evolution: Seat-Level Locking

Different customers selecting non-overlapping seats could proceed concurrently:

```text
C1 requests A1, A2
C2 requests D10, D11
```

But multi-seat locking becomes more complex. Locks must be acquired in
deterministic order:

```text
A1 before A2 before B1 before B2
```

Otherwise:

```text
C1 locks A1 and waits for A2
C2 locks A2 and waits for A1
-> deadlock
```

Seat-level locking improves concurrency but increases implementation risk.

### Partition by `showId`

All state for one Show can be routed to the same partition:

```text
hash(showId) -> partition
```

This makes it easier to execute atomic operations near the data. The trade-off
is that one extremely hot Show still concentrates traffic on one partition.

### Cache Availability

Availability is read much more frequently than seats are booked. It can be
cached:

```text
Show seat layout
Booked-seat snapshot
Availability counts
```

The displayed result may be slightly stale. That is acceptable because:

```text
Availability API -> informational
Hold API         -> authoritative
```

The final hold must always validate against Redis or the database.

### Other Hot-Show Controls

Production systems may use:

- Rate limiting.
- Virtual waiting rooms.
- Request queues.
- Per-customer hold limits.
- Bot protection.
- Short hold durations.
- Availability updates through WebSockets.
- Backpressure when lock contention is high.
- Metrics for conflict rate, latency, and expired holds.

## API Evolution

The interview implementation passes domain objects:

```java
holdSeats(
        Customer customer,
        Show show,
        Set<Seat> seats
);
```

That is convenient in one JVM. Production clients should send stable
identifiers:

```java
holdSeats(
        String customerId,
        String showId,
        Set<String> seatIds,
        String idempotencyKey
);
```

The service then:

```text
Authenticates customer
-> loads Show
-> validates Seat IDs
-> validates Show has not started
-> checks request idempotency
-> atomically creates the hold
```

`Optional<Booking>` gives only success or failure. A production API generally
needs richer information:

```java
public record HoldResult(
        HoldStatus status,
        String bookingId,
        Set<String> unavailableSeatIds,
        long expiresAt
) {
}
```

Possible statuses:

```text
SUCCESS
INVALID_SHOW
INVALID_SEAT
SHOW_STARTED
SEAT_ALREADY_HELD
SEAT_ALREADY_BOOKED
DUPLICATE_REQUEST
```

The client can then display an accurate failure message without learning
another customer's private booking details.

> Keep the business invariant constant--one show-seat can belong to only one
> valid booking--while replacing local synchronization with shared, durable,
> atomic storage as the system scales.
