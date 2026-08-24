# BookMyShow Senior Interview Questions

### 1. How do you prevent two customers from booking the same seat?

The backend treats a seat as unique within one Show. The same physical A1 may
be booked for the 3 PM Show and available for the 7 PM Show, so temporary locks
are stored as:

```java
Map<Show, Map<Seat, SeatLock>> locks;
```

The outer `ConcurrentHashMap` safely finds or creates one inner map per Show:

```java
Map<Seat, SeatLock> showLocks =
        locks.computeIfAbsent(
                show,
                ignored -> new HashMap<>()
        );
```

Every booking attempt for the same Show receives the same `showLocks` object.
The complete multi-seat check and update is synchronized on that object:

```java
synchronized (showLocks) {
    removeExpiredLocks();

    for (Seat seat : requestedSeats) {
        if (showLocks.containsKey(seat)) {
            return false;
        }
    }

    for (Seat seat : requestedSeats) {
        showLocks.put(
                seat,
                new SeatLock(...)
        );
    }
}
```

The first loop checks every requested seat without changing anything. The
second loop runs only when all seats are free. This guarantees:

```text
All requested seats are held
OR
No requested seat is held
```

For example, suppose C1 and C2 simultaneously request A1:

```text
C1 enters synchronized(showLocks).
C2 waits for the same monitor.

C1 checks A1:
No lock exists.

C1 creates:
A1 -> SeatLock(booking B1, customer C1, expiry T)

C1 exits the synchronized block.
C2 enters.

C2 checks A1:
An active lock exists, so C2 receives failure.
```

Before calling `tryLockSeats`, `BookingService.holdSeats()` also checks whether
any requested seat belongs to a confirmed booking. The booked-seat check,
temporary lock creation, and booking insertion execute under:

```java
synchronized (show)
```

Confirmation uses the same Show monitor:

```text
Validate every SeatLock belongs to this booking ID
-> change Booking from INITIATED to CONFIRMED
-> remove its temporary SeatLocks
```

This prevents a competing request from acquiring A1 during the short
HELD-to-BOOKED transition. It will either see C1's active lock or C1's confirmed
booking.

Different Shows use different inner maps and different Show monitors, so a
booking for the 3 PM Show does not block a booking for the 7 PM Show.

The frontend may disable A1 after seeing it as HELD or BOOKED, but availability
is only a snapshot. Two customers may have loaded the page while A1 was still
available, or a caller may invoke the API directly. Therefore, the atomic
backend hold operation—not the frontend—is the final authority.

### 2. Why is `ConcurrentHashMap` insufficient for a multi-seat hold?

It makes individual map operations thread-safe, but it cannot atomically update
multiple keys. Without an enclosing critical section, one request could lock A1
and then fail on A2, leaving a partial hold. The current implementation
synchronizes on the per-Show lock map to check all seats and then lock all seats
as one operation.

### 3. Why do we use both `synchronized(showLocks)` and `synchronized(show)`?

`synchronized(showLocks)` protects the temporary seat-lock `HashMap`.
`synchronized(show)` protects the complete business transition across two
stores: `BookingService.bookings` and `SeatLockProvider.locks`. It prevents a
competing hold from running between booking confirmation and temporary-lock
removal.

### 4. What happens when a hold expires?

`SeatLock` stores `expiresAt`. Availability and new hold requests lazily remove
expired locks, making those seats available again. A complete implementation
should also move the associated Booking from `INITIATED` to `EXPIRED`.

### 5. What is the booking and payment lifecycle?

```text
Hold succeeds
-> Booking becomes INITIATED
-> SeatLocks are created
-> payment is attempted

Payment succeeds before expiry
-> Booking becomes CONFIRMED
-> SeatLocks are removed
-> seats are derived as BOOKED

Payment fails
-> Booking becomes FAILED
-> SeatLocks are removed

Payment succeeds after expiry
-> confirmation fails
-> payment is refunded
```

Payment callbacks must be idempotent so duplicate callbacks do not create
duplicate confirmations or charges.

### 6. What happens if payment succeeds but confirmation fails?

The orchestrator starts a compensating refund and releases any remaining hold.
If the refund also fails, the error must be persisted or surfaced for retry and
manual reconciliation. A production asynchronous Saga would persist each step
so it could resume after a process crash.

### 7. Why does the current locking design work only in one JVM?

Java monitors coordinate only threads sharing the same objects in one process.
Two application instances have different `Show` objects and different maps, so
their locks do not coordinate. Production correctness must move to a shared
store such as a database transaction or an atomic Redis operation.

### 8. How would this be modeled in a relational database?

```text
ShowSeat
-------------------
show_id
seat_id
status
booking_id
hold_expires_at
version
```

Use a primary key or unique constraint on `(show_id, seat_id)`. A multi-seat
hold runs in one transaction: lock or conditionally update every requested row,
then commit only if all seats were available. Otherwise, roll back all changes.

### 9. How would Redis implement temporary holds?

One key can represent one show-seat lock:

```text
seat-lock:{showId}:{seatId} -> bookingId
TTL                         -> hold duration
```

A Lua script is needed to check and create several keys atomically for a
multi-seat request. Redis can accelerate temporary holds, while a durable
database remains the final source of truth for confirmed bookings.

### 10. What changes are needed for production APIs?

Use stable IDs instead of passing in-memory objects:

```java
holdSeats(
        String customerId,
        String showId,
        Set<String> seatIds,
        String idempotencyKey
);
```

The service loads the models, verifies ownership, and rejects invalid seats.
For detailed failures, return a result containing a status and unavailable
seats rather than only `Optional.empty()`.

### 11. What are the scalability concerns for a popular Show?

Per-Show synchronization is simple and correct but serializes hold and confirm
operations for that Show. Seat-level locking can improve concurrency but makes
atomic multi-seat requests and deadlock prevention more complex. Partitioning
by `showId` keeps related state together, and availability may be cached as long
as the final hold uses authoritative storage.

### 12. Which tests demonstrate correctness?

1. One hundred threads request A1; exactly one hold succeeds.
2. Two overlapping multi-seat requests never partially succeed.
3. Requests for different Shows proceed independently.
4. Expired holds make seats available.
5. Payment after expiry cannot confirm.
6. Duplicate confirmation is idempotent.
7. Failed payment releases every seat.
8. A confirmed seat can never be held again.
9. Seats outside the Show's Screen are rejected.
10. Availability is sorted by row and numeric column.

## Interview Boundary

Implement the models, availability, atomic hold, expiry, confirmation, and
concurrency tests. Explain external payment gateways, distributed Sagas,
message queues, reconciliation, Redis, and database transactions unless the
interviewer explicitly expands the implementation scope.
