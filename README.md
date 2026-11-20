# Libracore
### Java Swing Desktop Library Management System

Libracore is a desktop-based Library Management System developed using **Java Swing**. It provides a graphical interface for managing books, library members, borrowing and returning transactions, inventory, and library reports.

The application uses Java object serialization for local data persistence, allowing library records to remain available between application sessions without requiring an external database.

---

## Features

### 1. Dashboard
- Displays total book copies.
- Shows available and borrowed copies.
- Displays registered members.
- Tracks active and overdue loans.
- Provides an overview of library activity.

### 2. Book Management
- Add new books.
- View registered books.
- Search books by ISBN, title, or author.
- Edit existing book information.
- Delete books that have no borrowing history.
- Filter by category and availability; sort table columns.
- Track total, available, and borrowed copies.
- Prevent duplicate ISBN entries.

### 3. Member Management
- Register library members.
- Support student and faculty membership types.
- View and search member records.
- Update member information.
- Set Active, Inactive, or Suspended status.
- Delete members that have no borrowing history.
- View a member's borrowing history and outstanding fines.
- Apply different borrowing limits and loan periods based on membership type.

### 4. Borrow and Return Management
- Record book borrowing transactions.
- Generate unique loan identifiers.
- Check book availability before borrowing.
- Enforce borrowing limits.
- Automatically calculate due dates.
- Process book returns.
- Track active, returned, and overdue loans.
- Calculate overdue fines.
- Record full or partial fine payments and retain payment history.

### 5. Reports
- Display library statistics.
- Generate inventory summaries.
- View borrowing activity.
- Calculate outstanding fines.
- Export report data to CSV.
- Filter loan and payment reports by common periods or a custom date range.
- View member summaries, popular books, and collected fines.
- Check data integrity and identify inconsistent records.

### 6. Data Persistence
- Uses Java object serialization.
- Saves books, members, loans, and fine payments locally.
- Saves books, members, loans, and fine payments in separate serialized files.
- Keeps the previous version of each file as a matching `.bak` file.
- Reads the earlier combined `library.dat` snapshot and switches to separate
  files on the first successful save.
- Loads previously saved records when the application starts.
- Creates the data directory when needed.
- Loads books, members, loans, and fine payments from their local data files.
- Reports unreadable/incompatible data without replacing it with empty records.

### 7. User Interface
- Built using Java Swing.
- Uses a sidebar navigation system.
- Provides separate screens for major library operations.
- Uses reusable rounded components.
- Includes a built-in icon set.

---

## Object-Oriented Programming Requirements

Libracore applies the following Java Object-Oriented Programming concepts:

| Requirement | Implementation |
|---|---|
| Classes and Objects | Book, LibraryMember, StudentMember, FacultyMember, Loan, and service classes |
| Encapsulation | Private fields with public getters and validating setters |
| Constructors | Constructors initialize objects with validated data |
| Inheritance | StudentMember and FacultyMember extend LibraryMember |
| Polymorphism | Membership types provide different borrowing policies |
| Abstraction | LibraryMember is an abstract class |
| Interfaces | BorrowingPolicy defines borrowing-related requirements |
| Method Overriding | StudentMember and FacultyMember provide their own borrowing policies |
| Exception Handling | Try-catch blocks, validation, and custom exceptions |
| Collections | ArrayList, HashSet, and List |
| File Handling | Object serialization for persistent local storage |

### Custom Exceptions
- `BookUnavailableException`
- `BorrowLimitExceededException`

---

## Project Structure

```text
Libracore/
│
├── src/
│   ├── exceptions/
│   │   ├── BookUnavailableException.java
│   │   └── BorrowLimitExceededException.java
│   │
│   ├── Main.java
│   │
│   ├── gui/
│   │   ├── MainFrame.java
│   │   ├── DashboardPanel.java
│   │   ├── BooksPanel.java
│   │   ├── MembersPanel.java
│   │   ├── BorrowReturnPanel.java
│   │   ├── ReportsPanel.java
│   │   └── RoundedComponents.java
│   │
│   ├── interfaces/
│   │   └── BorrowingPolicy.java
│   │
│   ├── models/
│   │   ├── Book.java
│   │   ├── FinePayment.java
│   │   ├── LibraryData.java
│   │   ├── LibraryMember.java
│   │   ├── StudentMember.java
│   │   ├── FacultyMember.java
│   │   └── Loan.java
│   │
│   └── services/
│       ├── LibraryService.java
│       └── LibraryFileManager.java
│
├── resources/
│   └── icons/
│
├── data/
│
├── bin/
│
├── README.md
└── UML.md
```

See [UML.md](UML.md) for the class diagram and a note on how the stored IDs connect loan, book, member, and payment records.

---

## Requirements

Before running Libracore, make sure you have:

- Java Development Kit (JDK) 8 or later installed.
- Java compiler (`javac`) available.
- Java runtime (`java`) available.
- Windows PowerShell or a compatible terminal.

---

## How to Compile

Open a terminal in the project directory and execute:

**Windows PowerShell:**

```powershell
javac -d bin (Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

This compiles all Java source files and places the compiled classes inside the `bin` directory.

## How to Run

After successful compilation, execute:

```powershell
java -cp bin Main
```

Run both commands from the Libracore project directory.
The entry point is `Main` in the default package.

---

## Data Storage

Libracore stores its records locally using Java serialization.

The active data files are:

- `books.dat` – Book records.
- `members.dat` – Member records.
- `loans.dat` – Loan records.
- `payments.dat` – Fine payment records.
- Each file has a matching `.bak` recovery copy after its first update.

These files are stored inside the `data` directory. Existing installations
using the combined `library.dat` snapshot continue to load it; the first
successful save creates the separate files. Existing separate `books.dat`,
`members.dat`, and `loans.dat` files are also supported. Updates replace each
file atomically and retain that file's previous contents in its `.bak` copy.
Each file replacement is atomic, while a save across all four files is not a
single transaction.

If data cannot be read, preserve the affected file and its backup. Libracore
reports the problem instead of silently starting with empty records. At
startup, you can explicitly choose to restore the validated previous data.
For a damaged combined snapshot, the file is preserved as
`library.dat.corrupt-<timestamp>` and the backup is never deleted. If no valid
backup exists, restore a known-good copy manually
before restarting.

The combined `data/library.dat` in this workspace is retained as a migration
source and contains sample data. When the app starts with no saved data, it
starts with an empty library.

Keep the `resources` folder alongside the compiled application directories so the icons can be loaded at runtime.

---

## Technologies Used

- **Programming Language:** Java
- **GUI Framework:** Java Swing
- **Data Storage:** Java Object Serialization
- **Collections:** Java Collections Framework
- **Development Environment:** Visual Studio Code
- **Version Control:** Git and GitHub

---

## Project Purpose

Libracore was developed as an academic project for CS108 to demonstrate the practical application of Java programming, Object-Oriented Programming principles, exception handling, collections, file handling, and graphical user interface development.

The project aims to provide a simple and organized solution for managing common library operations through a desktop application.
