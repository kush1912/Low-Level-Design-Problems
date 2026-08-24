package lld.DesignProblems.bookmyshow.services;

import lld.DesignProblems.bookmyshow.model.Booking;
import lld.DesignProblems.bookmyshow.model.Customer;
import lld.DesignProblems.bookmyshow.model.Seat;
import lld.DesignProblems.bookmyshow.model.Show;

import java.util.Optional;
import java.util.Set;

public class BookingOrchestrator {
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public BookingOrchestrator(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    public Optional<Booking> bookTickets(Customer customer, Show show, Set<Seat> requestedSeats) {
        Optional<Booking> heldBooking = bookingService.holdSeats(customer, show, requestedSeats);

        if (heldBooking.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = heldBooking.get();
        double amount = requestedSeats.stream()
                .mapToDouble(seat -> seat.tier().getPrice())
                .sum();

        PaymentResult payment = paymentService.pay(booking.getBookingId(), amount);

        if (!payment.successful()) {
            bookingService.releaseHold(booking.getBookingId());
            return Optional.empty();
        }

        boolean confirmed = bookingService.confirmBooking(booking.getBookingId(), payment.paymentReference());

        if (confirmed) {
            return Optional.of(booking);
        }

        boolean refunded = paymentService.refund(
                payment.paymentReference()
        );
        bookingService.releaseHold(booking.getBookingId());

        if (!refunded) {
            throw new IllegalStateException(
                    "Payment succeeded but booking and refund failed"
            );
        }
        return Optional.empty();
    }
}
