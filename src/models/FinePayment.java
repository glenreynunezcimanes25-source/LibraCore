package models;

import java.io.Serializable;
import java.time.LocalDate;

public class FinePayment implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String paymentId;
    private final String loanId;
    private final String memberId;
    private final double amount;
    private final LocalDate paymentDate;

    // constructor
    public FinePayment(
            String paymentId,
            String loanId,
            String memberId,
            double amount,
            LocalDate paymentDate) {

        this.paymentId = requireText(paymentId, "Payment ID");
        this.loanId = requireText(loanId, "Loan ID");
        this.memberId = requireText(memberId, "Member ID");
        if (!Double.isFinite(amount) || amount <= 0.0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero.");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date cannot be null.");
        }

        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    // the getter
    public String getPaymentId() {
        return paymentId;
    }

    public String getLoanId() {
        return loanId;
    }

    public String getMemberId() {
        return memberId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }
}
