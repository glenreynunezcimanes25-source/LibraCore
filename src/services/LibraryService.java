package services;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import exceptions.BookUnavailableException;
import exceptions.BorrowLimitExceededException;
import models.Book;
import models.FinePayment;
import models.LibraryData;
import models.LibraryMember;
import models.Loan;
import models.LibraryMember.MemberStatus;

public class LibraryService {

    private ArrayList<Book> books;
    private ArrayList<LibraryMember> members;
    private ArrayList<Loan> loans;
    private ArrayList<FinePayment> payments;

    private LibraryFileManager fileManager;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public LibraryService() {

        fileManager = new LibraryFileManager();

        try {
            LibraryData libraryData = fileManager.loadData();
            books = libraryData.getBooks();
            members = libraryData.getMembers();
            loans = libraryData.getLoans();
            payments = libraryData.getPayments();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    exception.getMessage(), exception);
        }
    }

    private void saveAllData() {

        try {
            fileManager.saveData(
                    new LibraryData(books, members, loans, payments));

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to safely save library data: "
                            + exception.getMessage(),
                    exception);
        }
    }

    public void addLibraryDataListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removeLibraryDataListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private void notifyDataChanged() {
        changeSupport.firePropertyChange("libraryData", null, new Object());
    }

    public void addBook(Book book) {

        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null.");
        }

        if (findBookByIsbn(book.getIsbn()) != null) {
            throw new IllegalArgumentException("A book with this ISBN already exists.");
        }

        books.add(book);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            books.remove(book);
            throw exception;
        }
        notifyDataChanged();
    }

    public void addMember(LibraryMember member) {

        if (member == null) {
            throw new IllegalArgumentException("Member cannot be empty.");
        }

        if (findMemberById(member.getMemberId()) != null) {
            throw new IllegalArgumentException("Member ID already exists.");
        }

        members.add(member);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            members.remove(member);
            throw exception;
        }
        notifyDataChanged();
    }

    // Search for a book using its ISBN
    public Book findBookByIsbn(String isbn) {

        for (Book book : books) {

            if (isbn != null && book.getIsbn() != null
                    && book.getIsbn().equalsIgnoreCase(isbn.trim())) {
                return book;
            }
        }

        return null;
    }

    // Search for a loan using its ID
    public Loan findLoanById(String loanId) {

        for (Loan loan : loans) {

            if (loanId != null && loan.getLoanId() != null
                    && loan.getLoanId().equalsIgnoreCase(loanId.trim())) {
                return loan;
            }
        }

        return null;
    }

    // Search for a member using their ID
    public LibraryMember findMemberById(String memberId) {

        for (LibraryMember member : members) {

            if (memberId != null && member.getMemberId() != null
                    && member.getMemberId().equalsIgnoreCase(memberId.trim())) {
                return member;
            }
        }

        return null;
    }

    // Search for books using part of their title
    public ArrayList<Book> searchBooksByTitle(String title) {

        ArrayList<Book> results = new ArrayList<>();

        for (Book book : books) {

            if (title != null && book.getTitle().toLowerCase(Locale.ROOT)
                    .contains(title.trim().toLowerCase(Locale.ROOT))) {
                results.add(book);
            }
        }

        return results;
    }

    // Update book information
    public void updateBook(String isbn, String title,
            String author, String category,
            int totalCopies) {

        Book book = findBookByIsbn(isbn);

        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }

        String oldTitle = book.getTitle();
        String oldAuthor = book.getAuthor();
        String oldCategory = book.getCategory();
        int oldTotalCopies = book.getTotalCopies();

        try {
            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(category);
            book.setTotalCopies(totalCopies);
            saveAllData();
        } catch (RuntimeException exception) {
            book.setTitle(oldTitle);
            book.setAuthor(oldAuthor);
            book.setCategory(oldCategory);
            book.setTotalCopies(oldTotalCopies);
            throw exception;
        }
        notifyDataChanged();
    }

    // Delete a book using its ISBN
    public void deleteBook(String isbn) {

        Book book = findBookByIsbn(isbn);

        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }

        for (Loan loan : loans) {
            if (loan.getBookIsbn().equalsIgnoreCase(isbn.trim())) {
                throw new IllegalStateException(
                        "Cannot delete a book with loan history.");
            }
        }

        if (book.getAvailableCopies() != book.getTotalCopies()) {
            throw new IllegalStateException(
                    "Cannot delete a book that has active loans.");
        }

        int index = books.indexOf(book);
        books.remove(index);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            books.add(index, book);
            throw exception;
        }
        notifyDataChanged();
    }

    // Update member information
    public void updateMember(String memberId, String fullName,
            String contactNumber) {
        updateMember(
                memberId, fullName, contactNumber,
                findMemberById(memberId) == null
                        ? null
                        : findMemberById(memberId).getStatus());
    }

    public void updateMember(
            String memberId,
            String fullName,
            String contactNumber,
            MemberStatus status) {

        LibraryMember member = findMemberById(memberId);

        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        String oldFullName = member.getFullname();
        String oldContactNumber = member.getContactNumber();
        MemberStatus oldStatus = member.getStatus();
        if (status == MemberStatus.ARCHIVED
                && oldStatus != MemberStatus.ARCHIVED
                && hasActiveLoans(memberId)) {
            throw new IllegalStateException(
                    "A member with active loans cannot be archived.");
        }

        try {
            member.setFullname(fullName);
            member.setContactNumber(contactNumber);
            member.setStatus(status);
            saveAllData();
        } catch (RuntimeException exception) {
            member.setFullname(oldFullName);
            member.setContactNumber(oldContactNumber);
            member.setStatus(oldStatus);
            throw exception;
        }
        notifyDataChanged();
    }

    public void archiveBook(String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }
        if (book.isArchived()) {
            return;
        }
        if (book.getAvailableCopies() != book.getTotalCopies()) {
            throw new IllegalStateException(
                    "A book with active loans cannot be archived.");
        }

        book.setArchived(true);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            book.setArchived(false);
            throw exception;
        }
        notifyDataChanged();
    }

    public void archiveMember(String memberId) {
        LibraryMember member = findMemberById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }
        if (member.getStatus() == MemberStatus.ARCHIVED) {
            return;
        }
        if (hasActiveLoans(memberId)) {
            throw new IllegalStateException(
                    "A member with active loans cannot be archived.");
        }

        MemberStatus previousStatus = member.getStatus();
        member.setStatus(MemberStatus.ARCHIVED);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            member.setStatus(previousStatus);
            throw exception;
        }
        notifyDataChanged();
    }

    // Borrow a book
    public void borrowBook(String loanId, String memberId, String isbn)
            throws BookUnavailableException, BorrowLimitExceededException {

        // Check if loan ID already exist
        if (findLoanById(loanId) != null) {
            throw new IllegalArgumentException("Loan ID already exists.");
        }

        // Find member
        LibraryMember member = findMemberById(memberId);

        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        // Find book
        Book book = findBookByIsbn(isbn);

        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }
        if (!member.canBorrow()) {
            throw new IllegalStateException(
                    "Only active members may borrow books.");
        }
        if (book.isArchived()) {
            throw new IllegalStateException(
                    "Archived books cannot be borrowed.");
        }

        // Check book availability
        if (!book.isAvailable()) {
            throw new BookUnavailableException(
                    "No available copies of this book.");
        }

        // Count active loans
        int activeLoans = 0;

        for (Loan loan : loans) {

            if (loan.getMemberId().equalsIgnoreCase(memberId)
                    && !loan.isReturned()) {

                activeLoans++;
            }
        }

        // Check borrowing limit
        if (activeLoans >= member.getMaxLoans()) {
            throw new BorrowLimitExceededException(
                    "Member has reached the borrowing limit.");
        }

        // Generate borrowing and due dates
        LocalDate borrowDate = LocalDate.now();

        LocalDate dueDate = borrowDate.plusDays(
                member.getLoanPeriodDays());

        Loan loan = new Loan(
                loanId,
                member.getMemberId(),
                book.getIsbn(),
                borrowDate,
                dueDate);

        book.borrowCopy();
        loans.add(loan);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            loans.remove(loan);
            book.returnCopy();
            throw exception;
        }
        notifyDataChanged();
    }

    public String generateLoanId() {
        String loanId;
        do {
            loanId = "LN-" + UUID.randomUUID().toString();
        } while (findLoanById(loanId) != null);
        return loanId;
    }

    // Return a book and persist its updated loan and inventory.
    public void returnBook(String loanId) {

        Loan loan = findLoanById(loanId);

        if (loan == null) {
            throw new IllegalArgumentException("Loan not found.");
        }

        if (loan.isReturned()) {
            throw new IllegalStateException("This book has already been returned.");
        }

        Book book = findBookByIsbn(loan.getBookIsbn());

        if (book == null) {
            throw new IllegalStateException(
                    "The book associated with this loan could not be found.");
        }

        LibraryMember member = findMemberById(loan.getMemberId());
        if (member == null) {
            throw new IllegalStateException(
                    "The member associated with this loan could not be found.");
        }

        double fine = loan.calculateFine(member.getFinePerDay());
        loan.returnBook(LocalDate.now(), fine);
        book.returnCopy();
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            loan.restoreActiveState();
            book.borrowCopy();
            throw exception;
        }
        notifyDataChanged();
    }

    private boolean hasActiveLoans(String memberId) {
        for (Loan loan : loans) {
            if (loan.getMemberId().equalsIgnoreCase(memberId.trim())
                    && !loan.isReturned()) {
                return true;
            }
        }
        return false;
    }

    // Delete a member using their ID
    public void deleteMember(String memberId) {

        LibraryMember member = findMemberById(memberId);

        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        for (Loan loan : loans) {

            if (loan.getMemberId().equalsIgnoreCase(memberId)
                    && !loan.isReturned()) {

                throw new IllegalStateException(
                        "Cannot delete a member with active loans.");
            }
        }

        for (Loan loan : loans) {
            if (loan.getMemberId().equalsIgnoreCase(memberId.trim())) {
                throw new IllegalStateException(
                        "Cannot delete a member with loan history.");
            }
        }

        int index = members.indexOf(member);
        members.remove(index);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            members.add(index, member);
            throw exception;
        }
        notifyDataChanged();
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

    public ArrayList<FinePayment> getFinePayments() {
        return new ArrayList<>(payments);
    }

    public ArrayList<Loan> getActiveLoansForMember(String memberId) {
        ArrayList<Loan> results = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getMemberId().equalsIgnoreCase(memberId.trim())
                    && !loan.isReturned()) {
                results.add(loan);
            }
        }
        return results;
    }

    public ArrayList<Loan> getLoanHistoryForMember(String memberId) {
        ArrayList<Loan> results = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getMemberId().equalsIgnoreCase(memberId.trim())) {
                results.add(loan);
            }
        }
        return results;
    }

    // Books sort
    public ArrayList<Book> getBooksSortedByTitle() {

        ArrayList<Book> sortedBooks = getBooks();

        sortedBooks.sort(
                Comparator.comparing(
                        Book::getTitle,
                        String.CASE_INSENSITIVE_ORDER));

        return sortedBooks;
    }

    // Gets overdue loans
    public ArrayList<Loan> getOverdueLoans() {

        ArrayList<Loan> overdueLoans = new ArrayList<>();

        for (Loan loan : loans) {

            if (!loan.isReturned() && loan.isOverdue()) {
                overdueLoans.add(loan);
            }
        }

        return overdueLoans;
    }

    // Calculate total unpaid fines from active loans
    public double calculateTotalOutstandingFines() {

        double totalFines = 0.0;

        for (Loan loan : loans) {
            totalFines += getOutstandingFine(loan.getLoanId());
        }

        return totalFines;
    }

    public double getOutstandingFine(String loanId) {
        Loan loan = findLoanById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan not found.");
        }

        LibraryMember member = findMemberById(loan.getMemberId());
        if (member == null) {
            throw new IllegalStateException(
                    "The member associated with this loan could not be found.");
        }

        double fine = loan.calculateFine(member.getFinePerDay());
        double paid = 0.0;
        for (FinePayment payment : payments) {
            if (payment.getLoanId().equalsIgnoreCase(loan.getLoanId())) {
                paid += payment.getAmount();
            }
        }
        return Math.max(0.0, roundCurrency(fine - paid));
    }

    public double getMemberOutstandingFines(String memberId) {
        double total = 0.0;
        for (Loan loan : getLoanHistoryForMember(memberId)) {
            total += getOutstandingFine(loan.getLoanId());
        }
        return roundCurrency(total);
    }

    public double getTotalFinePayments() {
        double total = 0.0;
        for (FinePayment payment : payments) {
            total += payment.getAmount();
        }
        return roundCurrency(total);
    }

    public void recordFinePayment(String loanId, double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero.");
        }

        double roundedAmount = roundCurrency(amount);
        double outstanding = getOutstandingFine(loanId);
        if (roundedAmount > outstanding) {
            throw new IllegalArgumentException(
                    "Payment exceeds the outstanding fine of "
                            + String.format(Locale.ROOT, "%.2f", outstanding));
        }

        Loan loan = findLoanById(loanId);
        FinePayment payment = new FinePayment(
                "FP-" + UUID.randomUUID().toString(),
                loan.getLoanId(),
                loan.getMemberId(),
                roundedAmount,
                LocalDate.now());
        payments.add(payment);
        try {
            saveAllData();
        } catch (RuntimeException exception) {
            payments.remove(payment);
            throw exception;
        }
        notifyDataChanged();
    }

    private double roundCurrency(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    public int getLoanCountForBook(String isbn) {
        return getLoanCountForBook(isbn, null, null);
    }

    public int getLoanCountForBook(
            String isbn,
            LocalDate startDate,
            LocalDate endDate) {
        int count = 0;
        for (Loan loan : loans) {
            if (loan.getBookIsbn().equalsIgnoreCase(isbn.trim())
                    && (startDate == null
                            || !loan.getBorrowDate().isBefore(startDate))
                    && (endDate == null
                            || !loan.getBorrowDate().isAfter(endDate))) {
                count++;
            }
        }
        return count;
    }

    // Get library statistics
    public String generateLibrarySummary() {

        int totalBooks = 0;
        int availableBooks = 0;
        int borrowedBooks = 0;
        int totalMembers = members.size();
        int activeLoans = 0;

        for (Book book : books) {

            totalBooks += book.getTotalCopies();
            availableBooks += book.getAvailableCopies();
        }

        borrowedBooks = totalBooks - availableBooks;

        for (Loan loan : loans) {

            if (!loan.isReturned()) {
                activeLoans++;
            }
        }

        return "LIBRARY SUMMARY\n"
                + "-------------------------\n"
                + "Total Book Copies: " + totalBooks + "\n"
                + "Available Copies: " + availableBooks + "\n"
                + "Borrowed Copies: " + borrowedBooks + "\n"
                + "Registered Members: " + totalMembers + "\n"
                + "Active Loans: " + activeLoans + "\n"
                + "Overdue Loans: " + getOverdueLoans().size() + "\n"
                + "Outstanding Fines: ₱"
                + String.format("%.2f", calculateTotalOutstandingFines());
    }

    public List<String> getDataIntegrityIssues() {
        List<String> issues = new ArrayList<>();
        Set<String> bookIds = new HashSet<>();
        Set<String> memberIds = new HashSet<>();
        Set<String> loanIds = new HashSet<>();
        Map<String, Loan> loansById = new HashMap<>();
        Map<String, Integer> activeLoansByBook = new HashMap<>();

        validateBooks(bookIds, issues);
        validateMembers(memberIds, issues);
        validateLoans(
                bookIds,
                memberIds,
                loanIds,
                loansById,
                activeLoansByBook,
                issues);
        validateInventory(activeLoansByBook, issues);
        validatePayments(loansById, issues);
        return issues;
    }

    private void validateBooks(Set<String> bookIds, List<String> issues) {
        for (Book book : books) {
            if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
                issues.add("A book record has an empty ISBN.");
            }
            if (!bookIds.add(normalizeId(book.getIsbn()))) {
                issues.add(
                        "Duplicate book ISBN: " + book.getIsbn());
            }

            if (book.getTotalCopies() < 0
                    || book.getAvailableCopies() > book.getTotalCopies()) {
                issues.add(
                        "Available copies exceed total copies for ISBN: "
                                + book.getIsbn());
            }

            if (book.getAvailableCopies() < 0) {
                issues.add(
                        "Negative available copies for ISBN: "
                                + book.getIsbn());
            }
        }
    }

    private void validateMembers(
            Set<String> memberIds,
            List<String> issues) {
        for (LibraryMember member : members) {
            if (member.getMemberId() == null
                    || member.getMemberId().trim().isEmpty()) {
                issues.add("A member record has an empty member ID.");
            }
            if (!memberIds.add(normalizeId(member.getMemberId()))) {
                issues.add(
                        "Duplicate member ID: " + member.getMemberId());
            }
        }
    }

    private void validateLoans(
            Set<String> bookIds,
            Set<String> memberIds,
            Set<String> loanIds,
            Map<String, Loan> loansById,
            Map<String, Integer> activeLoansByBook,
            List<String> issues) {
        for (Loan loan : loans) {
            if (loan.getLoanId() == null || loan.getLoanId().trim().isEmpty()) {
                issues.add("A loan record has an empty loan ID.");
            }
            String loanId = normalizeId(loan.getLoanId());
            if (!loanIds.add(loanId)) {
                issues.add(
                        "Duplicate loan ID: " + loan.getLoanId());
            }
            loansById.putIfAbsent(loanId, loan);

            if (!memberIds.contains(normalizeId(loan.getMemberId()))) {
                issues.add(
                        "Loan " + loan.getLoanId()
                                + " references a missing member.");
            }

            if (!bookIds.contains(normalizeId(loan.getBookIsbn()))) {
                issues.add(
                        "Loan " + loan.getLoanId()
                                + " references a missing book.");
            }

            if (loan.getBorrowDate() == null || loan.getDueDate() == null
                    || loan.getDueDate().isBefore(loan.getBorrowDate())) {
                issues.add(
                        "Loan " + loan.getLoanId()
                                + " has invalid borrow or due dates.");
            }

            if (loan.isReturned() != (loan.getReturnDate() != null)) {
                issues.add(
                        "Loan " + loan.getLoanId()
                                + " has inconsistent return status and date.");
            }

            if (loan.getReturnDate() != null
                    && loan.getBorrowDate() != null
                    && loan.getReturnDate().isBefore(loan.getBorrowDate())) {
                issues.add(
                        "Loan " + loan.getLoanId()
                                + " has a return date before its borrow date.");
            }

            if (!loan.isReturned() && loan.getBookIsbn() != null) {
                String bookId = normalizeId(loan.getBookIsbn());
                activeLoansByBook.put(
                        bookId,
                        activeLoansByBook.getOrDefault(bookId, 0) + 1);
            }
        }
    }

    private void validateInventory(
            Map<String, Integer> activeLoansByBook,
            List<String> issues) {
        for (Book book : books) {
            int activeLoanCount = book.getIsbn() == null
                    ? 0
                    : activeLoansByBook.getOrDefault(
                            normalizeId(book.getIsbn()), 0);
            int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
            if (activeLoanCount != borrowedCopies) {
                issues.add(
                        "Inventory mismatch for "
                                + book.getTitle()
                                + " (ISBN: " + book.getIsbn() + "). "
                                + "Borrowed copies: " + borrowedCopies
                                + ", active loans: " + activeLoanCount);
            }
            if (book.isArchived() && activeLoanCount > 0) {
                issues.add(
                        "Archived book ISBN " + book.getIsbn()
                                + " still has active loans.");
            }
        }
    }

    private void validatePayments(
            Map<String, Loan> loansById,
            List<String> issues) {
        Set<String> paymentIds = new HashSet<>();
        Set<String> checkedPaymentLoanIds = new HashSet<>();
        Map<String, Double> paidByLoan = new HashMap<>();
        for (FinePayment payment : payments) {
            String loanId = normalizeId(payment.getLoanId());
            paidByLoan.put(
                    loanId,
                    paidByLoan.getOrDefault(loanId, 0.0)
                            + payment.getAmount());
        }

        for (FinePayment payment : payments) {
            if (!Double.isFinite(payment.getAmount())
                    || payment.getAmount() <= 0.0
                    || payment.getPaymentDate() == null) {
                issues.add(
                        "Payment " + payment.getPaymentId()
                                + " has an invalid amount or date.");
            }
            if (!paymentIds.add(normalizeId(payment.getPaymentId()))) {
                issues.add(
                        "Duplicate fine payment ID: "
                                + payment.getPaymentId());
            }
            String loanId = normalizeId(payment.getLoanId());
            Loan loan = loansById.get(loanId);
            if (loan == null
                    || !normalizeId(loan.getMemberId()).equals(
                            normalizeId(payment.getMemberId()))) {
                issues.add(
                        "Payment " + payment.getPaymentId()
                                + " references a missing or mismatched loan.");
            } else if (loan.isReturned()
                    && loan.hasAssessedFine()
                    && checkedPaymentLoanIds.add(loanId)) {
                double paidForLoan = paidByLoan.getOrDefault(loanId, 0.0);
                if (paidForLoan > loan.getAssessedFine() + 0.005) {
                    issues.add(
                            "Fine payments exceed the assessed fine for loan "
                                    + loan.getLoanId() + ".");
                }
            }
        }

    }

    private String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }
}
