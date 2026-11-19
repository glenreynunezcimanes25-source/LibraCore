# Libracore UML Class Diagram

This diagram reflects the classes and relationships in `src/`. The GUI panels call `LibraryService`; the service applies library rules and delegates persistence to `LibraryFileManager`. `Loan` and `FinePayment` store ISBNs and IDs as strings, so those links are references in the saved data rather than direct object fields.

```mermaid
classDiagram
    direction TB

    class Main {
        +main(args: String[]) void
    }

    class MainFrame {
        -library: LibraryService
        -dashboardPanel: DashboardPanel
        -booksPanel: BooksPanel
        -membersPanel: MembersPanel
        -borrowReturnPanel: BorrowReturnPanel
        -reportsPanel: ReportsPanel
        +MainFrame()
        +launch() void
    }

    class DashboardPanel {
        -library: LibraryService
        +DashboardPanel(library: LibraryService)
        +refreshDashboard() void
    }

    class BooksPanel {
        -library: LibraryService
        +BooksPanel(library: LibraryService)
        +refreshTable() void
    }

    class MembersPanel {
        -library: LibraryService
        +MembersPanel(library: LibraryService)
        +refreshTable() void
    }

    class BorrowReturnPanel {
        -library: LibraryService
        +BorrowReturnPanel(library: LibraryService)
        +refreshTable() void
    }

    class ReportsPanel {
        -library: LibraryService
        +ReportsPanel(library: LibraryService)
        +refreshReports() void
    }

    class RoundedComponents {
        <<utility>>
        +enableCellCopy(table: JTable) void
        +configureTableSorter(table: JTable) void
    }

    class JPanel {
        <<Swing base class>>
    }

    class LibraryService {
        -books: ArrayList~Book~
        -members: ArrayList~LibraryMember~
        -loans: ArrayList~Loan~
        -payments: ArrayList~FinePayment~
        -fileManager: LibraryFileManager
        +addBook(book: Book) void
        +addMember(member: LibraryMember) void
        +deleteBook(isbn: String) void
        +deleteMember(memberId: String) void
        +borrowBook(loanId: String, memberId: String, isbn: String) void
        +returnBook(loanId: String) void
        +generateLoanId() String
        +recordFinePayment(loanId: String, amount: double) void
        +getDataIntegrityIssues() List~String~
    }

    class LibraryFileManager {
        -dataDirectory: Path
        +loadData() LibraryData
        +saveData(data: LibraryData) void
        +restoreBackup() void
    }

    class LibraryData {
        -version: int
        -books: ArrayList~Book~
        -members: ArrayList~LibraryMember~
        -loans: ArrayList~Loan~
        -payments: ArrayList~FinePayment~
        +getBooks() ArrayList~Book~
        +getMembers() ArrayList~LibraryMember~
        +getLoans() ArrayList~Loan~
        +getPayments() ArrayList~FinePayment~
    }

    class Book {
        -isbn: String
        -title: String
        -author: String
        -category: String
        -totalCopies: int
        -availableCopies: int
        -archived: boolean
        +Book(isbn, title, author, category, totalCopies, availableCopies)
        +isAvailable() boolean
        +borrowCopy() void
        +returnCopy() void
        +setArchived(archived: boolean) void
    }

    class BorrowingPolicy {
        <<interface>>
        +getMaxLoans() int
        +getLoanPeriodDays() int
        +getFinePerDay() double
    }

    class LibraryMember {
        <<abstract>>
        -memberId: String
        -fullname: String
        -contactNumber: String
        -status: Object
        -registrationDate: LocalDate
        +getRegistrationDate() LocalDate
        +getStatus() MemberStatus
        +setStatus(status: MemberStatus) void
        +canBorrow() boolean
    }

    class StudentMember {
        +StudentMember(memberId, fullName, contactNumber)
        +getMaxLoans() int
        +getLoanPeriodDays() int
        +getFinePerDay() double
    }

    class FacultyMember {
        +FacultyMember(memberId, fullName, contactNumber)
        +getMaxLoans() int
        +getLoanPeriodDays() int
        +getFinePerDay() double
    }

    class Loan {
        -loanId: String
        -memberId: String
        -bookIsbn: String
        -borrowDate: LocalDate
        -dueDate: LocalDate
        -returnDate: LocalDate
        -returned: boolean
        -fineAssessed: boolean
        -assessedFine: double
        +Loan(loanId, memberId, bookIsbn, borrowDate, dueDate)
        +isOverdue() boolean
        +calculateFine(finePerDay: double) double
        +returnBook() void
        +returnBook(date: LocalDate, fineAmount: double) void
    }

    class FinePayment {
        -paymentId: String
        -loanId: String
        -memberId: String
        -amount: double
        -paymentDate: LocalDate
        +FinePayment(paymentId, loanId, memberId, amount, paymentDate)
    }

    class BookUnavailableException {
        +BookUnavailableException(message: String)
    }

    class BorrowLimitExceededException {
        +BorrowLimitExceededException(message: String)
    }

    Main ..> MainFrame : starts
    DashboardPanel --|> JPanel
    BooksPanel --|> JPanel
    MembersPanel --|> JPanel
    BorrowReturnPanel --|> JPanel
    ReportsPanel --|> JPanel
    MainFrame *-- DashboardPanel
    MainFrame *-- BooksPanel
    MainFrame *-- MembersPanel
    MainFrame *-- BorrowReturnPanel
    MainFrame *-- ReportsPanel
    MainFrame --> LibraryService : shares

    DashboardPanel --> LibraryService : reads
    BooksPanel --> LibraryService : manages books
    MembersPanel --> LibraryService : manages members
    BorrowReturnPanel --> LibraryService : borrows and returns
    ReportsPanel --> LibraryService : reports and payments
    BooksPanel ..> RoundedComponents
    MembersPanel ..> RoundedComponents
    BorrowReturnPanel ..> RoundedComponents
    ReportsPanel ..> RoundedComponents

    LibraryService --> LibraryFileManager : loads and saves
    LibraryFileManager ..> LibraryData : serializes
    LibraryService "1" o-- "0..*" Book : books
    LibraryService "1" o-- "0..*" LibraryMember : members
    LibraryService "1" o-- "0..*" Loan : loans
    LibraryService "1" o-- "0..*" FinePayment : payments
    LibraryData "1" o-- "0..*" Book : snapshot
    LibraryData "1" o-- "0..*" LibraryMember : snapshot
    LibraryData "1" o-- "0..*" Loan : snapshot
    LibraryData "1" o-- "0..*" FinePayment : snapshot

    LibraryMember ..|> BorrowingPolicy
    StudentMember --|> LibraryMember
    FacultyMember --|> LibraryMember

    Loan ..> Book : bookIsbn
    Loan ..> LibraryMember : memberId
    FinePayment ..> Loan : loanId
    FinePayment ..> LibraryMember : memberId

    LibraryService ..> BookUnavailableException : throws
    LibraryService ..> BorrowLimitExceededException : throws
```

`MemberStatus` is a nested enum inside `LibraryMember`. The status field is declared as `Object` to keep older serialized values readable, then converted back to `MemberStatus` by `getStatus()`. `MainFrame.launch()` is static. The diagram omits private UI widgets and helper methods to keep the design readable; it shows the main model, service, persistence, and screen relationships.
