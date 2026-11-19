package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import gui.RoundedComponents.RoundedPanel;
import models.Book;
import models.FinePayment;
import models.LibraryMember;
import models.Loan;
import models.StudentMember;
import services.LibraryService;

public class ReportsPanel extends JPanel {

        private final LibraryService library;

        // Theme
        private final Color BACKGROUND = new Color(245, 244, 239);
        private final Color DARK = new Color(38, 50, 56);
        private final Color GREEN = new Color(24, 58, 55);
        private final Color GOLD = new Color(198, 156, 77);
        private final Color MUTED = new Color(113, 128, 150);
        private final Color WHITE = Color.WHITE;

        // Statistic labels
        private JLabel totalBooksValue;
        private JLabel availableBooksValue;
        private JLabel borrowedBooksValue;
        private JLabel membersValue;
        private JLabel activeLoansValue;
        private JLabel overdueLoansValue;
        private JLabel returnedLoansValue;
        private JLabel finesValue;
        private JLabel collectedFinesValue;
        private JComboBox<String> periodFilter;
        private JTextField startDateField;
        private JTextField endDateField;
        private LocalDate customStartDate;
        private LocalDate customEndDate;

        // Tables
        private JTable inventoryTable;
        private DefaultTableModel inventoryModel;

        private JTable loanTable;
        private JTable memberTable;
        private JTable paymentTable;
        private JTable popularTable;
        private DefaultTableModel loanModel;
        private DefaultTableModel memberModel;
        private DefaultTableModel paymentModel;
        private DefaultTableModel popularModel;

        public ReportsPanel(LibraryService library) {

                this.library = library;

                setLayout(new BorderLayout());
                setBackground(BACKGROUND);

                setBorder(
                                BorderFactory.createEmptyBorder(
                                                28, 32, 28, 32));

                createInterface();
                refreshReports();
        }

        private void createInterface() {

                JPanel mainPanel = new JPanel(
                                new BorderLayout(0, 22));

                mainPanel.setOpaque(false);

                // HEADER

                JPanel titlePanel = new JPanel(
                                new GridLayout(2, 1, 0, 5));

                titlePanel.setOpaque(false);

                JLabel title = new JLabel("Reports & Analytics");

                title.setFont(
                                new Font("Arial", Font.BOLD, 25));

                title.setForeground(DARK);

                JLabel subtitle = new JLabel(
                                "Monitor library performance, inventory, and borrowing activity.");

                subtitle.setFont(
                                new Font("Arial", Font.PLAIN, 13));

                subtitle.setForeground(MUTED);

                titlePanel.add(title);
                titlePanel.add(subtitle);

                JButton checkButton = createButton(
                                "Check Data", "check.png", GREEN);

                JButton refreshButton = createButton(
                                "Refresh", "refresh.png", GREEN);

                JButton exportButton = createButton(
                                "Export CSV", "save.png", GOLD);
                JButton paymentButton = createButton(
                                "Record Payment", "receipt.png", GREEN);
                paymentButton.setPreferredSize(new Dimension(160, 40));

                exportButton.setForeground(GREEN);
                exportButton.setIcon(loadIcon("save.png", GREEN));

                checkButton.addActionListener(
                                event -> checkDataIntegrity());

                refreshButton.addActionListener(
                                event -> refreshReports());

                exportButton.addActionListener(
                                event -> exportReports());
                paymentButton.addActionListener(
                                event -> recordPayment());

                JPanel actions = new JPanel(
                                new FlowLayout(FlowLayout.RIGHT, 8, 0));

                actions.setOpaque(false);

                actions.add(checkButton);
                actions.add(paymentButton);
                actions.add(refreshButton);
                actions.add(exportButton);
                JPanel heading = new JPanel(new BorderLayout(16, 0));
                heading.setOpaque(false);
                heading.add(titlePanel, BorderLayout.WEST);
                heading.add(actions, BorderLayout.EAST);

                JPanel reportHeader = new JPanel(new BorderLayout(0, 10));
                reportHeader.setOpaque(false);
                reportHeader.add(heading, BorderLayout.NORTH);

                JPanel dateControls = new RoundedPanel(18, WHITE);
                dateControls.setLayout(new BorderLayout(0, 10));
                dateControls.setBorder(
                                BorderFactory.createEmptyBorder(14, 18, 14, 18));

                JPanel dateHeading = new JPanel(new GridLayout(2, 1, 0, 4));
                dateHeading.setOpaque(false);
                JLabel dateTitle = new JLabel("REPORT PERIOD");
                dateTitle.setFont(new Font("Arial", Font.BOLD, 10));
                dateTitle.setForeground(GREEN);
                JLabel dateDescription = new JLabel(
                                "Choose a period to filter loan activity and fine payments.");
                dateDescription.setFont(new Font("Arial", Font.PLAIN, 11));
                dateDescription.setForeground(MUTED);
                dateHeading.add(dateTitle);
                dateHeading.add(dateDescription);
                dateControls.add(dateHeading, BorderLayout.NORTH);

                JPanel dateFields = new JPanel(
                                new FlowLayout(FlowLayout.LEFT, 8, 0));
                dateFields.setOpaque(false);
                periodFilter = new JComboBox<>(new String[] {
                                "All time", "Today", "This week", "This month", "This year",
                                "Custom"
                });
                stylePeriodFilter(periodFilter);
                periodFilter.addActionListener(event -> {
                        if (!"Custom".equals(periodFilter.getSelectedItem())) {
                                refreshReports();
                        }
                });
                startDateField = new JTextField(10);
                endDateField = new JTextField(10);
                styleDateField(startDateField);
                styleDateField(endDateField);
                startDateField.setToolTipText("Start date, YYYY-MM-DD.");
                endDateField.setToolTipText("End date, YYYY-MM-DD.");
                JButton applyRangeButton = createButton(
                                "Apply Range", "filter.png", GREEN);
                applyRangeButton.setPreferredSize(new Dimension(130, 38));
                applyRangeButton.addActionListener(event -> applyCustomDateRange());
                dateFields.add(periodFilter);
                dateFields.add(createDateLabel("From"));
                dateFields.add(startDateField);
                dateFields.add(createDateLabel("To"));
                dateFields.add(endDateField);
                dateFields.add(applyRangeButton);
                dateControls.add(dateFields, BorderLayout.CENTER);
                reportHeader.add(dateControls, BorderLayout.SOUTH);

                mainPanel.add(reportHeader, BorderLayout.NORTH);

                // REPORT CONTENT

                JPanel reportContent = new JPanel(
                                new BorderLayout(0, 20));

                reportContent.setOpaque(false);

                // Summary cards
                JPanel summaryPanel = new JPanel(
                                new GridLayout(3, 3, 14, 14));

                summaryPanel.setOpaque(false);

                totalBooksValue = new JLabel("0");
                availableBooksValue = new JLabel("0");
                borrowedBooksValue = new JLabel("0");
                membersValue = new JLabel("0");

                activeLoansValue = new JLabel("0");
                overdueLoansValue = new JLabel("0");
                returnedLoansValue = new JLabel("0");
                finesValue = new JLabel("₱0.00");
                collectedFinesValue = new JLabel("₱0.00");

                summaryPanel.add(
                                createStatCard(
                                                "TOTAL BOOK COPIES",
                                                totalBooksValue,
                                                "Across all registered titles",
                                                GREEN,
                                                "library_books.png"));

                summaryPanel.add(
                                createStatCard(
                                                "AVAILABLE COPIES",
                                                availableBooksValue,
                                                "Ready to borrow",
                                                new Color(57, 134, 106),
                                                "check.png"));

                summaryPanel.add(
                                createStatCard(
                                                "BORROWED COPIES",
                                                borrowedBooksValue,
                                                "Currently checked out",
                                                GOLD,
                                                "sync.png"));

                summaryPanel.add(
                                createStatCard(
                                                "REGISTERED MEMBERS",
                                                membersValue,
                                                "Student and faculty readers",
                                                GREEN,
                                                "groups.png"));

                summaryPanel.add(
                                createStatCard(
                                                "ACTIVE LOANS",
                                                activeLoansValue,
                                                "Currently in progress",
                                                GREEN,
                                                "history.png"));

                summaryPanel.add(
                                createStatCard(
                                                "OVERDUE LOANS",
                                                overdueLoansValue,
                                                "Past their due date",
                                                new Color(198, 91, 88),
                                                "warning.png"));

                summaryPanel.add(
                                createStatCard(
                                                "RETURNED BOOKS",
                                                returnedLoansValue,
                                                "Completed transactions",
                                                new Color(57, 134, 106),
                                                "return.png"));

                summaryPanel.add(
                                createStatCard(
                                                "OUTSTANDING FINES",
                                                finesValue,
                                                "Unpaid member balances",
                                                new Color(198, 91, 88),
                                                "receipt.png"));
                summaryPanel.add(
                                createStatCard(
                                                "FINES COLLECTED",
                                                collectedFinesValue,
                                                "Payments recorded",
                                                new Color(57, 134, 106),
                                                "save.png"));

                // INVENTORY TABLE

                RoundedPanel inventoryPanel = createRoundedSection();

                String[] inventoryColumns = {
                                "ISBN",
                                "BOOK TITLE",
                                "CATEGORY",
                                "TOTAL COPIES",
                                "AVAILABLE",
                                "BORROWED"
                };

                inventoryModel = new DefaultTableModel(
                                inventoryColumns, 0) {

                        @Override
                        public boolean isCellEditable(
                                        int row, int column) {
                                return false;
                        }
                };

                inventoryTable = createTable(inventoryModel);
                inventoryPanel.add(
                                createTableView(inventoryTable, "Inventory Overview"),
                                BorderLayout.CENTER);

                // LOAN ACTIVITY TABLE
                RoundedPanel loanPanel = createRoundedSection();

                String[] loanColumns = {
                                "LOAN ID",
                                "MEMBER ID",
                                "BOOK ISBN",
                                "BORROW DATE",
                                "DUE DATE",
                                "STATUS",
                                "FINE"
                };

                loanModel = new DefaultTableModel(
                                loanColumns, 0) {

                        @Override
                        public boolean isCellEditable(
                                        int row, int column) {
                                return false;
                        }
                };

                loanTable = createTable(loanModel);
                loanPanel.add(
                                createTableView(loanTable, "Borrowing Activity"),
                                BorderLayout.CENTER);

                RoundedPanel membersPanel = createRoundedSection();
                memberModel = new DefaultTableModel(
                                new String[] {
                                                "MEMBER ID", "NAME", "TYPE", "STATUS",
                                                "ACTIVE LOANS", "OUTSTANDING FINES"
                                }, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };
                memberTable = createTable(memberModel);
                membersPanel.add(
                                createTableView(memberTable, "Member Summary"),
                                BorderLayout.CENTER);

                RoundedPanel paymentsPanel = createRoundedSection();
                paymentModel = new DefaultTableModel(
                                new String[] {
                                                "PAYMENT ID", "LOAN ID", "MEMBER ID", "AMOUNT", "DATE"
                                }, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };
                paymentTable = createTable(paymentModel);
                paymentsPanel.add(
                                createTableView(paymentTable, "Fine Payment History"),
                                BorderLayout.CENTER);

                RoundedPanel popularPanel = createRoundedSection();
                popularModel = new DefaultTableModel(
                                new String[] { "ISBN", "BOOK TITLE", "LOAN COUNT" }, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };
                popularTable = createTable(popularModel);
                popularPanel.add(
                                createTableView(popularTable, "Popular Books"),
                                BorderLayout.CENTER);

                // TABLE CONTAINER
                JPanel tablesPanel = new JPanel();
                tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
                tablesPanel.setOpaque(false);

                inventoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                loanPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                membersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                paymentsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                popularPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                tablesPanel.add(inventoryPanel);
                tablesPanel.add(Box.createVerticalStrut(18));
                tablesPanel.add(loanPanel);
                tablesPanel.add(Box.createVerticalStrut(18));
                tablesPanel.add(membersPanel);
                tablesPanel.add(Box.createVerticalStrut(18));
                tablesPanel.add(paymentsPanel);
                tablesPanel.add(Box.createVerticalStrut(18));
                tablesPanel.add(popularPanel);

                reportContent.add(
                                summaryPanel,
                                BorderLayout.NORTH);

                reportContent.add(
                                tablesPanel,
                                BorderLayout.CENTER);

                JScrollPane mainScroll = new JScrollPane(
                                reportContent);
                RoundedComponents.styleScrollPane(mainScroll);

                mainScroll.setBorder(null);
                mainScroll.setOpaque(false);
                mainScroll.getViewport().setOpaque(false);

                mainScroll.getVerticalScrollBar().setUnitIncrement(16);

                mainPanel.add(
                                mainScroll,
                                BorderLayout.CENTER);

                add(mainPanel, BorderLayout.CENTER);
        }

        // STATISTIC CARD
        private JPanel createStatCard(
                        String title,
                        JLabel value,
                        String description,
                        Color accent,
                        String iconName) {
                return RoundedComponents.createStatCard(
                                title, value, description, accent,
                                loadIcon(iconName, accent),
                                18, 88, 24, 8, 26, 8, 12, 10, 12);
        }

        // ROUNDED SECTION
        private RoundedPanel createRoundedSection() {

                RoundedPanel panel = new RoundedPanel(
                                18, WHITE);

                panel.setLayout(
                                new BorderLayout(0, 12));

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                18, 20, 20, 20));

                return panel;
        }

        // TABLE DESIG
        private JTable createTable(
                        DefaultTableModel model) {

                JTable table = new JTable(model);
                RoundedComponents.enableCellCopy(table);

                table.setFont(
                                new Font("Arial", Font.PLAIN, 12));

                table.setForeground(DARK);
                table.setBackground(WHITE);

                table.setRowHeight(42);

                table.setShowVerticalLines(false);
                table.setGridColor(new Color(238, 239, 235));
                table.setIntercellSpacing(new Dimension(0, 1));
                table.setFillsViewportHeight(true);
                table.setBorder(BorderFactory.createEmptyBorder());

                table.setSelectionBackground(
                                new Color(232, 222, 198));

                table.setSelectionForeground(DARK);
                table.setDefaultRenderer(Object.class,
                                new DefaultTableCellRenderer() {
                                        private static final long serialVersionUID = 1L;

                                        @Override
                                        public Component getTableCellRendererComponent(
                                                        JTable source,
                                                        Object value,
                                                        boolean isSelected,
                                                        boolean hasFocus,
                                                        int row,
                                                        int column) {
                                                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                                                                source, value, isSelected,
                                                                hasFocus, row, column);
                                                cell.setBorder(BorderFactory.createEmptyBorder(
                                                                0, 10, 0, 10));
                                                if (isSelected) {
                                                        cell.setBackground(
                                                                        new Color(232, 222, 198));
                                                        cell.setForeground(DARK);
                                                } else {
                                                        cell.setBackground(row % 2 == 0
                                                                        ? WHITE
                                                                        : new Color(248, 248, 244));
                                                        cell.setForeground(DARK);
                                                }
                                                return cell;
                                        }
                                });

                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                for (int column = 0; column < model.getColumnCount(); column++) {
                        String columnName = model.getColumnName(column);
                        if (isNumericColumn(columnName)) {
                                sorter.setComparator(column, (first, second) -> Integer.compare(
                                                Integer.parseInt(first.toString()),
                                                Integer.parseInt(second.toString())));
                        } else if (isAmountColumn(columnName)) {
                                sorter.setComparator(column, (first, second) -> Double.compare(
                                                parseReportAmount(first),
                                                parseReportAmount(second)));
                        } else if (columnName.contains("DATE")) {
                                sorter.setComparator(column, (first, second) -> compareReportDates(first, second));
                        }
                }
                table.setRowSorter(sorter);

                JTableHeader header = table.getTableHeader();

                header.setFont(
                                new Font("Arial", Font.BOLD, 10));

                header.setForeground(MUTED);

                header.setBackground(
                                new Color(248, 248, 244));

                header.setPreferredSize(
                                new Dimension(0, 42));
                header.setBorder(BorderFactory.createMatteBorder(
                                0, 0, 1, 0, new Color(230, 231, 226)));
                header.setReorderingAllowed(false);

                return table;
        }

        private JPanel createTableView(JTable table, String title) {
                JPanel tableView = new JPanel(new BorderLayout(0, 8));
                tableView.setOpaque(false);

                JPanel heading = new JPanel(new BorderLayout(12, 0));
                heading.setOpaque(false);
                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                titleLabel.setForeground(DARK);
                heading.add(titleLabel, BorderLayout.CENTER);
                heading.add(RoundedComponents.createSortComboBox(
                                table, new Dimension(150, 36)), BorderLayout.EAST);

                JPanel tableContents = new JPanel(new BorderLayout());
                tableContents.setOpaque(false);
                tableContents.add(table.getTableHeader(), BorderLayout.NORTH);
                tableContents.add(table, BorderLayout.CENTER);

                tableView.add(heading, BorderLayout.NORTH);
                tableView.add(tableContents, BorderLayout.CENTER);
                return tableView;
        }

        private boolean isNumericColumn(String columnName) {
                return "TOTAL COPIES".equals(columnName)
                                || "AVAILABLE".equals(columnName)
                                || "BORROWED".equals(columnName)
                                || "ACTIVE LOANS".equals(columnName)
                                || "LOAN COUNT".equals(columnName);
        }

        private boolean isAmountColumn(String columnName) {
                return columnName.contains("FINE") || "AMOUNT".equals(columnName);
        }

        private double parseReportAmount(Object value) {
                return Double.parseDouble(
                                value.toString().replace("₱", "").replace(",", "").trim());
        }

        private int compareReportDates(Object first, Object second) {
                LocalDate firstDate = (LocalDate) first;
                LocalDate secondDate = (LocalDate) second;
                if (firstDate == null) {
                        return secondDate == null ? 0 : -1;
                }
                return secondDate == null ? 1 : firstDate.compareTo(secondDate);
        }

        private void styleDateField(JTextField field) {
                field.setFont(new Font("Arial", Font.PLAIN, 12));
                field.setForeground(DARK);
                field.setPreferredSize(new Dimension(112, 36));
                field.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(225, 225, 218)),
                                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        }

        private void stylePeriodFilter(JComboBox<String> filter) {
                RoundedComponents.styleComboBox(filter);
        }

        private JLabel createDateLabel(String text) {
                JLabel label = new JLabel(text);
                label.setFont(new Font("Arial", Font.BOLD, 11));
                label.setForeground(MUTED);
                return label;
        }

        private void updateTableHeight(JTable table) {
                int height = Math.max(1, table.getRowCount()) * table.getRowHeight();
                table.setPreferredSize(
                                new Dimension(table.getPreferredSize().width, height));
        }

        // BUTTON DESIGN
        private JButton createButton(
                        String text,
                        String iconName,
                        Color background) {

                JButton button = new RoundedComponents.RoundedButton(text);

                button.setFont(
                                new Font("Arial", Font.BOLD, 12));

                button.setForeground(WHITE);
                button.setBackground(background);

                button.setFocusPainted(false);
                button.setBorderPainted(false);

                button.setPreferredSize(
                                new Dimension(140, 40));

                button.setCursor(
                                new java.awt.Cursor(
                                                java.awt.Cursor.HAND_CURSOR));

                button.setIcon(loadIcon(iconName, button.getForeground()));
                button.setIconTextGap(8);

                return button;
        }

        private javax.swing.ImageIcon loadIcon(String fileName, Color tint) {
                File file = new File("resources/icons/" + fileName);
                if (!file.exists()) {
                        return null;
                }

                javax.swing.ImageIcon original = new javax.swing.ImageIcon(file.getPath());
                BufferedImage tinted = new BufferedImage(
                                18, 18, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = tinted.createGraphics();
                try {
                        graphics.setRenderingHint(
                                        RenderingHints.KEY_INTERPOLATION,
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        graphics.drawImage(original.getImage(), 0, 0, 18, 18, null);
                        graphics.setComposite(java.awt.AlphaComposite.SrcIn);
                        graphics.setColor(tint);
                        graphics.fillRect(0, 0, 18, 18);
                } finally {
                        graphics.dispose();
                }

                return new javax.swing.ImageIcon((Image) tinted);
        }

        // REFRESH REPORTS
        public void refreshReports() {

                List<Book> books = library.getBooks();

                List<LibraryMember> members = library.getMembers();

                List<Loan> loans = library.getLoans();
                LocalDate[] range = getSelectedDateRange();

                int totalCopies = 0;
                int availableCopies = 0;
                int borrowedCopies = 0;

                int active = 0;
                int overdue = 0;
                int returned = 0;

                double totalFines = library.calculateTotalOutstandingFines();

                // Inventory calculations
                inventoryModel.setRowCount(0);

                for (Book book : books) {

                        int borrowed = book.getTotalCopies()
                                        - book.getAvailableCopies();

                        totalCopies += book.getTotalCopies();

                        availableCopies += book.getAvailableCopies();

                        borrowedCopies += borrowed;

                        inventoryModel.addRow(
                                        new Object[] {
                                                        book.getIsbn(),
                                                        book.getTitle(),
                                                        book.getCategory(),
                                                        book.getTotalCopies(),
                                                        book.getAvailableCopies(),
                                                        borrowed
                                        });
                }

                memberModel.setRowCount(0);
                for (LibraryMember member : members) {
                        memberModel.addRow(new Object[] {
                                        member.getMemberId(),
                                        member.getFullname(),
                                        member instanceof StudentMember ? "Student" : "Faculty",
                                        member.getStatus(),
                                        library.getActiveLoansForMember(member.getMemberId()).size(),
                                        String.format(
                                                        Locale.ROOT, "₱%.2f",
                                                        library.getMemberOutstandingFines(member.getMemberId()))
                        });
                }

                // Loan calculations
                loanModel.setRowCount(0);

                for (Loan loan : loans) {
                        if (!isInRange(loan.getBorrowDate(), range)) {
                                continue;
                        }

                        LibraryMember member = library.findMemberById(
                                        loan.getMemberId());

                        double fine = member == null
                                        ? 0.0
                                        : library.getOutstandingFine(loan.getLoanId());

                        String status;

                        if (loan.isReturned()) {

                                status = "Returned";
                                returned++;

                        } else if (!loan.isReturned() && loan.isOverdue()) {

                                status = "Overdue";
                                overdue++;

                        } else {

                                status = "Active";
                                active++;
                        }

                        loanModel.addRow(
                                        new Object[] {
                                                        loan.getLoanId(),
                                                        loan.getMemberId(),
                                                        loan.getBookIsbn(),
                                                        loan.getBorrowDate(),
                                                        loan.getDueDate(),
                                                        status,
                                                        String.format(Locale.ROOT, "₱%.2f", fine)
                                        });
                }

                paymentModel.setRowCount(0);
                for (FinePayment payment : library.getFinePayments()) {
                        if (!isInRange(payment.getPaymentDate(), range)) {
                                continue;
                        }
                        paymentModel.addRow(new Object[] {
                                        payment.getPaymentId(),
                                        payment.getLoanId(),
                                        payment.getMemberId(),
                                        String.format(Locale.ROOT, "₱%.2f", payment.getAmount()),
                                        payment.getPaymentDate()
                        });
                }

                popularModel.setRowCount(0);
                ArrayList<Book> popularBooks = new ArrayList<>(books);
                popularBooks.sort(
                                Comparator.comparingInt(
                                                (Book book) -> library.getLoanCountForBook(
                                                                book.getIsbn(),
                                                                range == null ? null : range[0],
                                                                range == null ? null : range[1]))
                                                .reversed()
                                                .thenComparing(
                                                                Book::getTitle,
                                                                String.CASE_INSENSITIVE_ORDER));
                for (Book book : popularBooks) {
                        int loanCount = library.getLoanCountForBook(
                                        book.getIsbn(),
                                        range == null ? null : range[0],
                                        range == null ? null : range[1]);
                        if (loanCount > 0) {
                                popularModel.addRow(new Object[] {
                                                book.getIsbn(), book.getTitle(), loanCount
                                });
                        }
                }

                updateTableHeight(inventoryTable);
                updateTableHeight(loanTable);
                updateTableHeight(memberTable);
                updateTableHeight(paymentTable);
                updateTableHeight(popularTable);

                // Update cards
                totalBooksValue.setText(
                                String.valueOf(totalCopies));

                availableBooksValue.setText(
                                String.valueOf(availableCopies));

                borrowedBooksValue.setText(
                                String.valueOf(borrowedCopies));

                membersValue.setText(
                                String.valueOf(members.size()));

                activeLoansValue.setText(
                                String.valueOf(active));

                overdueLoansValue.setText(
                                String.valueOf(overdue));

                returnedLoansValue.setText(
                                String.valueOf(returned));

                finesValue.setText(
                                String.format("₱%.2f", totalFines));
                collectedFinesValue.setText(
                                String.format(
                                                Locale.ROOT, "₱%.2f",
                                                library.getTotalFinePayments()));
        }

        private LocalDate[] getSelectedDateRange() {
                String period = periodFilter.getSelectedItem().toString();
                LocalDate today = LocalDate.now();
                LocalDate start;
                switch (period) {
                        case "Today":
                                start = today;
                                break;
                        case "This week":
                                start = today.with(DayOfWeek.MONDAY);
                                break;
                        case "This month":
                                start = today.withDayOfMonth(1);
                                break;
                        case "This year":
                                start = today.withDayOfYear(1);
                                break;
                        case "Custom":
                                if (customStartDate == null || customEndDate == null) {
                                        return null;
                                }
                                return new LocalDate[] { customStartDate, customEndDate };
                        default:
                                return null;
                }
                return new LocalDate[] { start, today };
        }

        private boolean isInRange(LocalDate date, LocalDate[] range) {
                return range == null
                                || (date != null
                                                && !date.isBefore(range[0])
                                                && !date.isAfter(range[1]));
        }

        private void applyCustomDateRange() {
                try {
                        LocalDate start = LocalDate.parse(startDateField.getText().trim());
                        LocalDate end = LocalDate.parse(endDateField.getText().trim());
                        if (end.isBefore(start)) {
                                throw new IllegalArgumentException(
                                                "The end date must be on or after the start date.");
                        }
                        customStartDate = start;
                        customEndDate = end;
                        periodFilter.setSelectedItem("Custom");
                        refreshReports();
                } catch (java.time.format.DateTimeParseException exception) {
                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Invalid date range",
                                        "Enter both dates using YYYY-MM-DD.",
                                        RoundedComponents.DialogType.ERROR);
                } catch (IllegalArgumentException exception) {
                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Invalid date range",
                                        exception.getMessage(),
                                        RoundedComponents.DialogType.ERROR);
                }
        }

        private void recordPayment() {
                int selectedRow = loanTable.getSelectedRow();
                if (selectedRow < 0) {
                        showReportError("Select a loan with an outstanding fine first.");
                        return;
                }

                int modelRow = loanTable.convertRowIndexToModel(selectedRow);
                String loanId = loanModel.getValueAt(modelRow, 0).toString();
                double outstanding = library.getOutstandingFine(loanId);
                if (outstanding <= 0.0) {
                        showReportError("The selected loan has no outstanding fine.");
                        return;
                }

                String amountText = JOptionPane.showInputDialog(
                                this,
                                "Outstanding balance: "
                                                + String.format(Locale.ROOT, "₱%.2f", outstanding)
                                                + "\nEnter the payment amount:",
                                "Record Fine Payment",
                                JOptionPane.QUESTION_MESSAGE);
                if (amountText == null) {
                        return;
                }

                try {
                        double amount = Double.parseDouble(amountText.trim());
                        library.recordFinePayment(loanId, amount);
                        refreshReports();
                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Payment recorded",
                                        "The fine payment was recorded successfully.",
                                        RoundedComponents.DialogType.SUCCESS);
                } catch (NumberFormatException exception) {
                        showReportError("Enter a valid payment amount.");
                } catch (IllegalArgumentException | IllegalStateException exception) {
                        showReportError(exception.getMessage());
                }
        }

        private void showReportError(String message) {
                RoundedComponents.showMessageDialog(
                                this,
                                "Unable to complete action",
                                message,
                                RoundedComponents.DialogType.ERROR);
        }

        // EXPORT REPORTS
        private void exportReports() {

                Window owner = SwingUtilities.getWindowAncestor(this);
                FileDialog chooser;
                if (owner instanceof Dialog) {
                        chooser = new FileDialog(
                                        (Dialog) owner,
                                        "Save Libracore Report",
                                        FileDialog.SAVE);
                } else {
                        Frame frameOwner = owner instanceof Frame
                                        ? (Frame) owner
                                        : null;
                        chooser = new FileDialog(
                                        frameOwner,
                                        "Save Libracore Report",
                                        FileDialog.SAVE);
                }

                chooser.setFile("Libracore_Report.csv");
                chooser.setVisible(true);

                String selectedName = chooser.getFile();
                if (selectedName == null || selectedName.trim().isEmpty()) {
                        return;
                }

                String directory = chooser.getDirectory();
                File file = directory == null
                                ? new File(selectedName)
                                : new File(directory, selectedName);

                if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {

                        file = new File(
                                        file.getAbsolutePath() + ".csv");
                }

                if (file.exists()) {

                        boolean confirm = RoundedComponents.showWarningDialog(
                                        this,
                                        "Replace existing report?",
                                        "The file \"" + file.getName()
                                                        + "\" already exists. Replace it?",
                                        "Replace File");

                        if (!confirm) {
                                return;
                        }
                }

                try (PrintWriter writer = new PrintWriter(
                                Files.newBufferedWriter(
                                                file.toPath(), StandardCharsets.UTF_8))) {

                        writer.println("LIBRACORE LIBRARY REPORT");
                        LocalDate[] range = getSelectedDateRange();
                        writer.println("Loan/payment date range,"
                                        + (range == null
                                                        ? "All time"
                                                        : range[0] + " through " + range[1]));
                        writer.println();

                        writer.println("INVENTORY REPORT");
                        writer.println(
                                        "ISBN,Title,Category,Total Copies,Available,Borrowed");

                        for (Book book : library.getBooks()) {

                                int borrowed = book.getTotalCopies()
                                                - book.getAvailableCopies();

                                writer.println(
                                                csv(book.getIsbn()) + ","
                                                                + csv(book.getTitle()) + ","
                                                                + csv(book.getCategory()) + ","
                                                                + book.getTotalCopies() + ","
                                                                + book.getAvailableCopies() + ","
                                                                + borrowed);
                        }

                        writer.println();
                        writer.println("MEMBER REPORT");
                        writer.println(
                                        "Member ID,Name,Type,Status,Active Loans,Outstanding Fine");
                        for (LibraryMember member : library.getMembers()) {
                                writer.println(
                                                csv(member.getMemberId()) + ","
                                                                + csv(member.getFullname()) + ","
                                                                + (member instanceof StudentMember
                                                                                ? "Student"
                                                                                : "Faculty")
                                                                + ","
                                                                + member.getStatus() + ","
                                                                + library.getActiveLoansForMember(
                                                                                member.getMemberId()).size()
                                                                + ","
                                                                + String.format(
                                                                                Locale.ROOT, "%.2f",
                                                                                library.getMemberOutstandingFines(
                                                                                                member.getMemberId())));
                        }

                        writer.println("LOAN REPORT");

                        writer.println(
                                        "Loan ID,Member ID,Book ISBN,Borrow Date,"
                                                        + "Due Date,Return Date,Status,Fine");

                        for (Loan loan : library.getLoans()) {
                                if (!isInRange(loan.getBorrowDate(), range)) {
                                        continue;
                                }

                                LibraryMember member = library.findMemberById(
                                                loan.getMemberId());

                                double fine = member == null
                                                ? 0.0
                                                : library.getOutstandingFine(loan.getLoanId());

                                String status;

                                if (loan.isReturned()) {
                                        status = "Returned";
                                } else if (!loan.isReturned() && loan.isOverdue()) {
                                        status = "Overdue";
                                } else {
                                        status = "Active";
                                }

                                writer.println(
                                                csv(loan.getLoanId()) + ","
                                                                + csv(loan.getMemberId()) + ","
                                                                + csv(loan.getBookIsbn()) + ","
                                                                + loan.getBorrowDate() + ","
                                                                + loan.getDueDate() + ","
                                                                + (loan.getReturnDate() == null
                                                                                ? ""
                                                                                : loan.getReturnDate())
                                                                + ","
                                                                + status + ","
                                                                + String.format(Locale.ROOT, "%.2f", fine));
                        }

                        writer.println();
                        writer.println("FINE PAYMENT REPORT");
                        writer.println("Payment ID,Loan ID,Member ID,Amount,Payment Date");
                        for (FinePayment payment : library.getFinePayments()) {
                                if (!isInRange(payment.getPaymentDate(), range)) {
                                        continue;
                                }
                                writer.println(
                                                csv(payment.getPaymentId()) + ","
                                                                + csv(payment.getLoanId()) + ","
                                                                + csv(payment.getMemberId()) + ","
                                                                + String.format(
                                                                                Locale.ROOT, "%.2f",
                                                                                payment.getAmount())
                                                                + ","
                                                                + payment.getPaymentDate());
                        }

                        writer.println();
                        writer.println("POPULAR BOOKS REPORT");
                        writer.println("ISBN,Title,Loan Count");
                        List<Book> popularBooks = new ArrayList<>(library.getBooks());
                        popularBooks.sort(
                                        Comparator.comparingInt(
                                                        (Book book) -> library.getLoanCountForBook(
                                                                        book.getIsbn(),
                                                                        range == null ? null : range[0],
                                                                        range == null ? null : range[1]))
                                                        .reversed());
                        for (Book book : popularBooks) {
                                int count = library.getLoanCountForBook(
                                                book.getIsbn(),
                                                range == null ? null : range[0],
                                                range == null ? null : range[1]);
                                if (count > 0) {
                                        writer.println(
                                                        csv(book.getIsbn()) + ","
                                                                        + csv(book.getTitle()) + "," + count);
                                }
                        }

                        writer.flush();
                        if (writer.checkError()) {
                                throw new IOException("The report could not be written completely.");
                        }

                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Report exported",
                                        "Report successfully exported!\n\n"
                                                        + file.getAbsolutePath(),
                                        RoundedComponents.DialogType.SUCCESS);

                } catch (IOException | SecurityException exception) {
                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Export Error",
                                        "Unable to export report:\n"
                                                        + exception.getMessage(),
                                        RoundedComponents.DialogType.ERROR);
                } catch (IllegalArgumentException | IllegalStateException exception) {
                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Export Error",
                                        "Unable to generate report:\n" + exception.getMessage(),
                                        RoundedComponents.DialogType.ERROR);
                }
        }

        // Escape CSV values containing commas or quotes
        private String csv(String value) {

                if (value == null) {
                        return "";
                }

                return "\""
                                + value.replace("\"", "\"\"")
                                + "\"";
        }

        private void checkDataIntegrity() {
                List<String> issues = library.getDataIntegrityIssues();
                boolean passed = issues.isEmpty();
                String title = passed
                                ? "Data Check Passed"
                                : "Data Integrity Issues";
                String summary = passed
                                ? "All library records are consistent."
                                : issues.size()
                                                + (issues.size() == 1
                                                                ? " issue was found."
                                                                : " issues were found.");

                JDialog dialog = new JDialog(
                                SwingUtilities.getWindowAncestor(this),
                                title,
                                Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setResizable(true);

                JPanel content = new JPanel(new BorderLayout(0, 14));
                content.setBackground(BACKGROUND);
                content.setBorder(
                                BorderFactory.createEmptyBorder(20, 22, 18, 22));

                JPanel heading = new JPanel(new GridLayout(2, 1, 0, 5));
                heading.setOpaque(false);
                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
                titleLabel.setForeground(DARK);
                JLabel summaryLabel = new JLabel(summary);
                summaryLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                summaryLabel.setForeground(
                                passed ? new Color(57, 134, 106) : new Color(154, 107, 35));
                heading.add(titleLabel);
                heading.add(summaryLabel);

                JTextArea details = new JTextArea(formatIntegrityIssues(issues));
                details.setFont(new Font("Arial", Font.PLAIN, 13));
                details.setForeground(DARK);
                details.setBackground(WHITE);
                details.setEditable(false);
                details.setLineWrap(true);
                details.setWrapStyleWord(true);
                details.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                details.setCaretPosition(0);

                JScrollPane detailsScroll = new JScrollPane(
                                details,
                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                RoundedComponents.styleScrollPane(detailsScroll);
                detailsScroll.setBorder(BorderFactory.createEmptyBorder());
                detailsScroll.setOpaque(false);
                detailsScroll.getViewport().setOpaque(false);

                RoundedPanel resultsCard = new RoundedPanel(18, WHITE);
                resultsCard.setLayout(new BorderLayout());
                resultsCard.setBorder(
                                BorderFactory.createEmptyBorder(14, 16, 14, 16));
                resultsCard.add(detailsScroll, BorderLayout.CENTER);

                JButton closeButton = createButton(
                                "Close", "close.png", GREEN);
                closeButton.setPreferredSize(new Dimension(112, 38));
                closeButton.addActionListener(event -> dialog.dispose());
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                actions.setOpaque(false);
                actions.add(closeButton);

                content.add(heading, BorderLayout.NORTH);
                content.add(resultsCard, BorderLayout.CENTER);
                content.add(actions, BorderLayout.SOUTH);
                dialog.setContentPane(content);

                Rectangle screenBounds = GraphicsEnvironment
                                .getLocalGraphicsEnvironment()
                                .getMaximumWindowBounds();
                int dialogWidth = Math.min(
                                720, Math.max(360, screenBounds.width - 80));
                int dialogHeight = Math.min(
                                560, Math.max(300, screenBounds.height - 100));
                detailsScroll.setPreferredSize(
                                new Dimension(
                                                dialogWidth - 76,
                                                Math.max(120, dialogHeight - 190)));
                dialog.setMinimumSize(
                                new Dimension(
                                                Math.min(420, dialogWidth),
                                                Math.min(320, dialogHeight)));
                dialog.setSize(dialogWidth, dialogHeight);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
        }

        private String formatIntegrityIssues(List<String> issues) {
                if (issues.isEmpty()) {
                        return "No inconsistencies were found in the library records.";
                }

                StringBuilder report = new StringBuilder();
                for (int i = 0; i < issues.size(); i++) {
                        if (i > 0) {
                                report.append("\n\n");
                        }
                        report.append(i + 1)
                                        .append(". ")
                                        .append(issues.get(i));
                }
                return report.toString();
        }

}
