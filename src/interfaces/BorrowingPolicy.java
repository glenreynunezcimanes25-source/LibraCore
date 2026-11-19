package interfaces;

public interface BorrowingPolicy {

    int getMaxLoans();

    int getLoanPeriodDays();

    double getFinePerDay();

}
