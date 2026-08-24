package lld.DesignProblems.bookmyshow.services;

public record PaymentResult(
        boolean successful,
        String paymentReference
) {
}
