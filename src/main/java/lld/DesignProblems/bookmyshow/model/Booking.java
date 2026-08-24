package lld.DesignProblems.bookmyshow.model;

import lld.DesignProblems.bookmyshow.enums.BookingStatus;

import java.util.Objects;
import java.util.Set;

public class Booking {
    private final String bookingId;
    private final Customer customer;
    private final Show show;
    private final Set<Seat> seats;
    private volatile BookingStatus status;
    private volatile String paymentReference;
    // Making Booking Status and PaymentReference Volatile Makes different threads see their values immediately as soon as they are changed.

    public Booking(String bookingId, Customer customer, Show show, Set<Seat> seats){
        this.bookingId = bookingId;
        this.customer = customer;
        this.show = show;
        this.seats = Set.copyOf(seats);
        this.status = BookingStatus.INITIATED;
    }

    public boolean confirm(String paymentReference) {
        if (status == BookingStatus.CONFIRMED) {
            return Objects.equals(this.paymentReference, paymentReference);
        }

        if (status != BookingStatus.INITIATED || paymentReference == null || paymentReference.isBlank()) {
            return false;
        }

        this.paymentReference = paymentReference;
        this.status = BookingStatus.CONFIRMED;
        return true;
    }

    public boolean fail() {
        if (status != BookingStatus.INITIATED) {
            return false;
        }
        status = BookingStatus.FAILED;
        return true;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Show getShow() {
        return show;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public BookingStatus getStatus() {
        return status;
    }
}
