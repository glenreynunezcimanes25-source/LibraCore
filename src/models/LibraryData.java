package models;

import java.io.Serializable;
import java.util.ArrayList;

public class LibraryData implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_VERSION = 1;

    private final int version;
    private final ArrayList<Book> books;
    private final ArrayList<LibraryMember> members;
    private final ArrayList<Loan> loans;
    private final ArrayList<FinePayment> payments;

    public LibraryData(
            ArrayList<Book> books,
            ArrayList<LibraryMember> members,
            ArrayList<Loan> loans,
            ArrayList<FinePayment> payments) {

        this.version = CURRENT_VERSION;
        this.books = new ArrayList<>(books);
        this.members = new ArrayList<>(members);
        this.loans = new ArrayList<>(loans);
        this.payments = new ArrayList<>(payments);
    }

    public int getVersion() {
        return version;
    }

    public ArrayList<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public ArrayList<LibraryMember> getMembers() {
        return new ArrayList<>(members);
    }

    public ArrayList<Loan> getLoans() {
        return new ArrayList<>(loans);
    }

    public ArrayList<FinePayment> getPayments() {
        return new ArrayList<>(payments);
    }
}
