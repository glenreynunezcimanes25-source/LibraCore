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
    private boolean fineAssessed;
    private double assessedFine;

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

    public double getAssessedFine() {
        return assessedFine;
    }

    public boolean hasAssessedFine() {
        return fineAssessed;
    }

    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        LocalDate referenceDate = (returnDate != null) ? returnDate : LocalDate.now();
        return referenceDate.isAfter(dueDate);
    }

    public double calculateFine(double finePerDay) {
        if (!Double.isFinite(finePerDay) || finePerDay < 0) {
            throw new IllegalArgumentException("Fine per day cannot be negative.");
        }

        if (returned && fineAssessed) {
            return assessedFine;
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
            throw new IllegalStateException("This loan has already been returned.");
        }
        this.returnDate = LocalDate.now();
        this.returned = true;
        this.fineAssessed = false;
    }

    public void returnBook(LocalDate date, double fineAmount) {
        if (this.returned) {
            throw new IllegalStateException("This loan has already been returned.");
        }
        if (date == null || date.isBefore(borrowDate)) {
            throw new IllegalArgumentException(
                    "Return date cannot be before the borrow date.");
        }
        if (!Double.isFinite(fineAmount) || fineAmount < 0.0) {
            throw new IllegalArgumentException("Fine amount cannot be negative.");
        }

        this.returnDate = date;
        this.assessedFine = fineAmount;
        this.fineAssessed = true;
        this.returned = true;
    }

    public void restoreActiveState() {
        this.returnDate = null;
        this.assessedFine = 0.0;
        this.fineAssessed = false;
        this.returned = false;
    }

    public void setReturnDate(LocalDate returnDate) {
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date cannot be null.");
        }

        if (returnDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date cannot be before borrow date.");
        }

        this.returnDate = returnDate;
        this.returned = true;
        this.fineAssessed = false;
        this.assessedFine = 0.0;
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
        if ((dueDate != null && dueDate.isBefore(borrowDate))
                || (returnDate != null && returnDate.isBefore(borrowDate))) {
            throw new IllegalArgumentException(
                    "Borrow date cannot be after the due or return date.");
        }

        this.borrowDate = borrowDate;
        this.fineAssessed = false;
        this.assessedFine = 0.0;
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
        this.fineAssessed = false;
        this.assessedFine = 0.0;
    }
}
