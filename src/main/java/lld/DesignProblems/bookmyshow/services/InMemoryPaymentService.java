package lld.DesignProblems.bookmyshow.services;

import lld.DesignProblems.bookmyshow.model.Payment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryPaymentService implements PaymentService {
    private final Map<String, Payment> paymentsByBooking; // Booking id - Payment
    private final Map<String, Payment> paymentsByReference;

    public InMemoryPaymentService() {
        this.paymentsByBooking = new HashMap<>();
        this.paymentsByReference = new HashMap<>();
    }

    @Override
    public synchronized PaymentResult pay(String bookingId, double amount) {
        if (bookingId == null || bookingId.isBlank() || amount <= 0) {
            return new PaymentResult(false, null);
        }

        Payment existingPayment = paymentsByBooking.get(bookingId);

        // Idempotency Block
        //means pay() has already been called previously for the same booking ID,payment has succeeded previously but got lost.
        if (existingPayment != null) {
            boolean samePayment = Double.compare(existingPayment.amount(), amount) == 0;

            //This prevents the same booking ID from being reused with a changed amount, whether caused by a bug or request tampering.
            //Or The Payment has already been refunded.
            //A refunded booking attempt should not silently create another payment. The customer should normally begin a new booking attempt with a new booking ID.
            if (!samePayment || existingPayment.refunded()) {
                return new PaymentResult(false, null);
            }

            return new PaymentResult(true, existingPayment.paymentReference());
        }

        String paymentReference = "PAY-" + UUID.randomUUID();
        Payment payment = new Payment(bookingId, paymentReference, amount, false);

        paymentsByBooking.put(bookingId, payment);
        paymentsByReference.put(paymentReference, payment);

        return new PaymentResult(true, paymentReference);
    }

    @Override
    public synchronized boolean refund(String paymentReference) {
        Payment payment = paymentsByReference.get(paymentReference);
        if (payment == null) {
            return false;
        }

        if (payment.refunded()) {
            return true;
        }

        Payment refundedPayment = new Payment(payment.bookingId(), payment.paymentReference(), payment.amount(), true);

        paymentsByBooking.put(payment.bookingId(), refundedPayment);
        paymentsByReference.put(paymentReference, refundedPayment);
        return true;
    }

}
