package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan implements Serializable {

    private static final long serialVersionUID = 1L;

    private String loanId;
    private String memberId;
    private String bookIsbn;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    private boolean returned;

    public Loan(String loanId, String memberId, String bookIsbn,
            LocalDate borrowDate, LocalDate dueDate) {

        setLoanId(loanId);
        setMemberId(memberId);
        setBookIsbn(bookIsbn);
        setBorrowDate(borrowDate);
        setDueDate(dueDate);

        this.returnDate = null;
        this.returned = false;
    }

    // Getters

    public String getLoanId() {
        return loanId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public boolean isOverdue() {
        LocalDate referenceDate = (returnDate != null) ? returnDate : LocalDate.now();
        return referenceDate.isAfter(dueDate);
    }

    public double calculateFine(double finePerDay) {
        if (finePerDay < 0) {
            throw new IllegalArgumentException("Fine per day cannot be negative.");
        }

        if (!isOverdue()) {
            return 0.0;
        }

        LocalDate referenceDate = (returnDate != null) ? returnDate : LocalDate.now();
        long overdueDays = ChronoUnit.DAYS.between(dueDate, referenceDate);
        return overdueDays * finePerDay;
    }

    public void returnBook() {
        if (this.returned) {
            return;
        }

        this.returnDate = LocalDate.now();
        this.returned = true;
    }

    public void setReturnDate(LocalDate returnDate) {
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date cannot be null.");
        }

        if (borrowDate != null && returnDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date cannot be before borrow date.");
        }

        this.returnDate = returnDate;
        this.returned = true;
    }

    // Setters

    public void setLoanId(String loanId) {
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new IllegalArgumentException("Loan ID cannot be empty.");
        }

        this.loanId = loanId.trim();
    }

    public void setMemberId(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be empty.");
        }

        this.memberId = memberId.trim();
    }

    public void setBookIsbn(String bookIsbn) {
        if (bookIsbn == null || bookIsbn.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ISBN cannot be empty.");
        }

        this.bookIsbn = bookIsbn.trim();
    }

    public void setBorrowDate(LocalDate borrowDate) {
        if (borrowDate == null) {
            throw new IllegalArgumentException("Borrow date cannot be null.");
        }

        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null.");
        }

        if (borrowDate != null && dueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException(
                    "Due date cannot be before borrow date.");
        }

        this.dueDate = dueDate;
    }
}