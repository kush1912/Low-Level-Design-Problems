package lld.DesignProblems.bookmyshow.services;

public interface PaymentService {
    PaymentResult pay(String bookingId, double amount);

    boolean refund(String paymentReference);
}
