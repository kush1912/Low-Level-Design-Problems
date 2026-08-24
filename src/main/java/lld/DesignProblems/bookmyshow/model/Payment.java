package lld.DesignProblems.bookmyshow.model;

public record Payment(String bookingId, String paymentReference, double amount, boolean refunded) {
}
