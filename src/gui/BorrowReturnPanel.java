package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import exceptions.BookUnavailableException;
import exceptions.BorrowLimitExceededException;
import models.Book;
import models.LibraryMember;
import models.Loan;
import services.LibraryService;

public class BorrowReturnPanel extends JPanel {

    private final LibraryService library;

    private final Color BACKGROUND = new Color(245, 244, 239);
    private final Color DARK = new Color(38, 50, 56);
    private final Color GREEN = new Color(24, 58, 55);
    private final Color GOLD = new Color(198, 156, 77);
    private final Color MUTED = new Color(113, 128, 150);
    private final Color WHITE = Color.WHITE;

    private JTextField memberIdField;
    private JTextField isbnField;
    private JTextField loanIdField;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton returnButton;

    private JTable loanTable;
    private DefaultTableModel tableModel;
    private JLabel recordsHint;

    private JLabel activeValue;
    private JLabel overdueValue;
    private JLabel returnedValue;

    public BorrowReturnPanel(LibraryService library) {

        this.library = library;

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        setBorder(
                BorderFactory.createEmptyBorder(18, 24, 18, 24));

        createInterface();
        refreshTable();
    }

    private void createInterface() {

        JPanel mainPanel = new JPanel(
                new BorderLayout(0, 14));
        mainPanel.setOpaque(false);

        // Heading
        JPanel heading = new JPanel(
                new GridLayout(2, 1, 0, 5));
        heading.setOpaque(false);

        JLabel title = new JLabel("Borrow / Return");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Manage book borrowing, returns, and loan records.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(subtitle);

        mainPanel.add(heading, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(
                new BorderLayout(0, 12));
        content.setOpaque(false);

        // Statistics
        JPanel statistics = new JPanel(
                new GridLayout(1, 3, 10, 0));
        statistics.setOpaque(false);

        activeValue = new JLabel("0");
        overdueValue = new JLabel("0");
        returnedValue = new JLabel("0");

        statistics.add(createStatCard(
                "ACTIVE LOANS", activeValue,
                "Currently checked out", GREEN, "history.png"));

        statistics.add(createStatCard(
                "OVERDUE LOANS", overdueValue,
                "Past their due date",
                new Color(198, 91, 88), "warning.png"));

        statistics.add(createStatCard(
                "RETURNED BOOKS", returnedValue,
                "Completed returns",
                new Color(57, 134, 106), "return.png"));

        // Borrow and Return forms
        JPanel formsPanel = new JPanel(
                new GridLayout(1, 2, 10, 0));
        formsPanel.setOpaque(false);

        formsPanel.add(createBorrowPanel());
        formsPanel.add(createReturnPanel());

        // Upper section
        JPanel upperPanel = new JPanel(
                new BorderLayout(0, 12));
        upperPanel.setOpaque(false);

        upperPanel.add(statistics, BorderLayout.NORTH);
        upperPanel.add(formsPanel, BorderLayout.CENTER);

        content.add(upperPanel, BorderLayout.NORTH);

        // Loan records
        RoundedComponents.RoundedPanel recordsPanel =
                new RoundedComponents.RoundedPanel(18, WHITE);

        recordsPanel.setLayout(
                new BorderLayout(0, 10));

        recordsPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        14, 16, 16, 16));

        JPanel recordsTop = new JPanel(new BorderLayout(0, 10));
        recordsTop.setOpaque(false);
        JPanel recordsHeading = new JPanel(new BorderLayout(12, 0));
        recordsHeading.setOpaque(false);

        JLabel recordsTitle = new JLabel("Loan Records");
        recordsTitle.setFont(
                new Font("Arial", Font.BOLD, 16));
        recordsTitle.setForeground(DARK);

        recordsHint = new JLabel();
        recordsHint.setFont(new Font("Arial", Font.PLAIN, 11));
        recordsHint.setForeground(MUTED);

        recordsHeading.add(recordsTitle, BorderLayout.WEST);
        recordsHeading.add(recordsHint, BorderLayout.EAST);
        recordsTop.add(recordsHeading, BorderLayout.NORTH);

        JPanel filters = new JPanel(new BorderLayout(8, 0));
        filters.setOpaque(false);
        JPanel filterSelectors = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterSelectors.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Search by loan ID, member ID, or ISBN.");
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 225, 218)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        statusFilter = new JComboBox<>(
                new String[] { "All loans", "Active", "Overdue", "Returned" });
        RoundedComponents.styleComboBox(
                statusFilter, new Dimension(150, 40));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshTable();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshTable();
            }
        });
        statusFilter.addActionListener(event -> refreshTable());
        filters.add(searchField, BorderLayout.CENTER);
        filterSelectors.add(statusFilter);
        filters.add(filterSelectors, BorderLayout.EAST);
        recordsTop.add(filters, BorderLayout.CENTER);
        recordsPanel.add(recordsTop, BorderLayout.NORTH);

        String[] columns = {
                "LOAN ID",
                "MEMBER ID",
                "BOOK ISBN",
                "BORROW DATE",
                "DUE DATE",
                "RETURN DATE",
                "STATUS",
                "FINE"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loanTable = new JTable(tableModel);

        loanTable.setFont(new Font("Arial", Font.PLAIN, 11));
        loanTable.setForeground(DARK);
        loanTable.setBackground(WHITE);
        loanTable.setRowHeight(34);
        loanTable.setShowVerticalLines(false);
        loanTable.setGridColor(new Color(235, 235, 230));
        loanTable.setAutoCreateRowSorter(true);
        RoundedComponents.configureTableSorter(loanTable);
        loanTable.setFillsViewportHeight(true);
        loanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        RoundedComponents.enableCellCopy(loanTable);

        loanTable.setSelectionBackground(
                new Color(232, 222, 198));

        loanTable.setSelectionForeground(DARK);

        JTableHeader header = loanTable.getTableHeader();

        header.setFont(new Font("Arial", Font.BOLD, 10));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 248, 244));
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        loanTable.getColumnModel().getColumn(6).setCellRenderer(
                new DefaultTableCellRenderer() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        JLabel label = (JLabel) super
                                .getTableCellRendererComponent(
                                        table, value, isSelected,
                                        hasFocus, row, column);
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        label.setBorder(
                                BorderFactory.createEmptyBorder(0, 8, 0, 8));

                        String status = String.valueOf(value);
                        if (isSelected) {
                            label.setBackground(loanTable.getSelectionBackground());
                            label.setForeground(loanTable.getSelectionForeground());
                        } else if ("Overdue".equals(status)) {
                            label.setBackground(new Color(252, 235, 233));
                            label.setForeground(new Color(169, 62, 58));
                        } else if ("Returned".equals(status)) {
                            label.setBackground(new Color(231, 244, 237));
                            label.setForeground(new Color(45, 112, 82));
                        } else {
                            label.setBackground(new Color(248, 243, 229));
                            label.setForeground(new Color(142, 105, 40));
                        }

                        return label;
                    }
                });

        loanTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }

            int selectedRow = loanTable.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }

            int modelRow = loanTable.convertRowIndexToModel(selectedRow);
            String status = tableModel.getValueAt(modelRow, 6).toString();
            if ("Returned".equals(status)) {
                loanIdField.setText("");
                return;
            }

            loanIdField.setText(
                    tableModel.getValueAt(modelRow, 0).toString());
        });

        JScrollPane scrollPane = new JScrollPane(loanTable);
        RoundedComponents.styleScrollPane(scrollPane);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(235, 235, 230)));

        scrollPane.getViewport().setBackground(WHITE);

        recordsPanel.add(scrollPane, BorderLayout.CENTER);

        content.add(recordsPanel, BorderLayout.CENTER);

        mainPanel.add(content, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // Statistics card
    private JPanel createStatCard(
            String title,
            JLabel value,
            String description,
            Color accent,
            String iconName) {
        return RoundedComponents.createStatCard(
                title, value, description, accent,
                loadStatIcon(iconName, accent));
    }

    private ImageIcon loadStatIcon(String fileName, Color tint) {
        ImageIcon original = loadIcon(fileName);
        if (original == null) {
            return null;
        }

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
        return new ImageIcon(tinted);
    }

    // Borrow form
    private JPanel createBorrowPanel() {

        RoundedComponents.RoundedPanel panel =
                new RoundedComponents.RoundedPanel(18, WHITE);

        panel.setLayout(
                new BorderLayout(0, 9));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        13, 16, 13, 16));

        JPanel heading = createFormHeading(
                "Borrow a Book",
                "Enter a member ID and ISBN to create a loan.");

        JPanel fields = new JPanel(
                new GridLayout(1, 2, 10, 0));
        fields.setOpaque(false);

        memberIdField = new JTextField();
        isbnField = new JTextField();

        fields.add(createFieldPanel(
                "Member ID", memberIdField, "Enter the member's ID."));
        fields.add(createFieldPanel(
                "Book ISBN", isbnField, "Enter the book's ISBN."));

        JButton borrowButton = createButton(
                "Borrow Book", "book.png", GREEN);

        borrowButton.addActionListener(event -> borrowBook());
        memberIdField.addActionListener(event -> isbnField.requestFocusInWindow());
        isbnField.addActionListener(event -> borrowBook());

        panel.add(heading, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(createActionBar(borrowButton), BorderLayout.SOUTH);

        return panel;
    }

    // Return form
    private JPanel createReturnPanel() {

        RoundedComponents.RoundedPanel panel =
                new RoundedComponents.RoundedPanel(18, WHITE);

        panel.setLayout(
                new BorderLayout(0, 9));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        13, 16, 13, 16));

        JPanel heading = createFormHeading(
                "Return a Book",
                "Select an active loan below or enter its ID.");

        JPanel fields = new JPanel(new BorderLayout(0, 5));
        fields.setOpaque(false);

        loanIdField = new JTextField();

        fields.add(createFieldPanel(
                "Loan ID", loanIdField, "Enter or select an active loan ID."),
                BorderLayout.NORTH);

        JLabel selectionHint = new JLabel(
                "Click an active loan row to fill this field.");
        selectionHint.setFont(new Font("Arial", Font.PLAIN, 10));
        selectionHint.setForeground(MUTED);
        fields.add(selectionHint, BorderLayout.CENTER);

        returnButton = createButton(
                "Return Book", "return.png", GOLD);
        returnButton.setForeground(GREEN);
        returnButton.setEnabled(false);
        loanIdField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateReturnButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateReturnButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateReturnButtonState();
            }
        });

        returnButton.addActionListener(event -> returnBook());
        loanIdField.addActionListener(event -> returnBook());

        panel.add(heading, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(createActionBar(returnButton), BorderLayout.SOUTH);

        return panel;
    }

    private void updateReturnButtonState() {
        returnButton.setEnabled(!loanIdField.getText().trim().isEmpty());
    }

    private JPanel createFormHeading(String titleText, String description) {

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(GREEN);

        JLabel subtitle = new JLabel(description);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 10));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(subtitle);
        return heading;
    }

    private JPanel createFieldPanel(
            String labelText,
            JTextField field,
            String tooltip) {

        JPanel fieldPanel = new JPanel(new BorderLayout(0, 5));
        fieldPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(DARK);

        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBackground(WHITE);
        field.setPreferredSize(new Dimension(0, 34));
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 225, 218)),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        field.setToolTipText(tooltip);

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);
        return fieldPanel;
    }

    private JPanel createActionBar(JButton button) {

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(button);
        return actions;
    }

    private JButton createButton(
            String text,
            String iconName,
            Color background) {

        JButton button = new RoundedComponents.RoundedButton(text);

        button.setFont(
                new Font("Arial", Font.BOLD, 11));

        button.setForeground(WHITE);
        button.setBackground(background);
        button.setIcon(loadIcon(iconName));
        button.setIconTextGap(8);

        button.setPreferredSize(new Dimension(150, 40));

        button.setCursor(
                new java.awt.Cursor(
                        java.awt.Cursor.HAND_CURSOR));

        return button;
    }

    private ImageIcon loadIcon(String fileName) {

        java.io.File file = new java.io.File(
                "resources/icons/" + fileName);

        if (!file.exists()) {
            return null;
        }

        ImageIcon original = new ImageIcon(file.getPath());

        java.awt.Image resized = original.getImage()
                .getScaledInstance(
                        18, 18,
                        java.awt.Image.SCALE_SMOOTH);

        return new ImageIcon(resized);
    }

    // Borrow operation
    private void borrowBook() {

        String memberId = memberIdField.getText().trim();
        String isbn = isbnField.getText().trim();

        if (memberId.isEmpty() || isbn.isEmpty()) {
            showError("Please enter both Member ID and Book ISBN.");
            return;
        }

        try {

            String loanId = library.generateLoanId();

            LibraryMember member = library.findMemberById(memberId);

            Book book = library.findBookByIsbn(isbn);

            if (member == null) {
                showError("Member does not exist.");
                return;
            }

            if (book == null) {
                showError("Book does not exist.");
                return;
            }

            library.borrowBook(loanId, memberId, isbn);

            Loan loan = library.findLoanById(loanId);

            refreshTable();

            memberIdField.setText("");
            isbnField.setText("");

            RoundedComponents.showMessageDialog(
                    this,
                    "Book borrowed",
                    "Book successfully borrowed!\n\n"
                            + "Loan ID: " + loanId + "\n"
                            + "Member: " + member.getFullname() + "\n"
                            + "Book: " + book.getTitle() + "\n"
                            + "Due Date: " + loan.getDueDate(),
                    RoundedComponents.DialogType.SUCCESS);

        } catch (BookUnavailableException
                | BorrowLimitExceededException
                | IllegalArgumentException
                | IllegalStateException exception) {

            showError(exception.getMessage());
        }
    }

    // Return operation
    private void returnBook() {

        String loanId = loanIdField.getText().trim();

        if (loanId.isEmpty()) {
            showError("Please enter a Loan ID.");
            return;
        }

        try {

            Loan loan = library.findLoanById(loanId);

            if (loan == null) {
                showError("Loan record does not exist.");
                return;
            }

            if (loan.isReturned()) {
                showError("This book has already been returned.");
                return;
            }

            LibraryMember member = library.findMemberById(loan.getMemberId());
            if (member == null) {
                showError("The member for this loan could not be found.");
                return;
            }

            double fine = loan.calculateFine(
                    member.getFinePerDay());

            library.returnBook(loanId);

            refreshTable();

            loanIdField.setText("");

            RoundedComponents.showMessageDialog(
                    this,
                    "Book returned",
                    "Book successfully returned!\n\n"
                            + "Loan ID: " + loanId + "\n"
                            + "Overdue Fine: ₱"
                            + String.format("%.2f", fine),
                    RoundedComponents.DialogType.SUCCESS);

        } catch (IllegalArgumentException | IllegalStateException exception) {

            showError(exception.getMessage());
        }
    }

    // Refresh records
    public void refreshTable() {

        tableModel.setRowCount(0);

        String keyword = searchField.getText().trim().toLowerCase(
                java.util.Locale.ROOT);
        String selectedStatus = statusFilter.getSelectedItem().toString();
        int active = 0;
        int overdue = 0;
        int returned = 0;

        for (Loan loan : library.getLoans()) {

            LibraryMember member = library.findMemberById(loan.getMemberId());

            double fine = member == null
                    ? 0.0
                    : library.getOutstandingFine(loan.getLoanId());

            String status;

            if (loan.isReturned()) {

                status = "Returned";
                returned++;

            } else if (loan.isOverdue()) {

                status = "Overdue";
                overdue++;

            } else {

                status = "Active";
                active++;
            }

            String searchable = (
                    loan.getLoanId() + " "
                            + loan.getMemberId() + " "
                            + loan.getBookIsbn())
                    .toLowerCase(java.util.Locale.ROOT);
            boolean statusMatches = "All loans".equals(selectedStatus)
                    || ("Active".equals(selectedStatus) && !loan.isReturned())
                    || selectedStatus.equals(status);
            if (!searchable.contains(keyword) || !statusMatches) {
                continue;
            }

            String returnDate = loan.getReturnDate() == null
                    ? "-"
                    : loan.getReturnDate().toString();

            tableModel.addRow(new Object[] {
                    loan.getLoanId(),
                    loan.getMemberId(),
                    loan.getBookIsbn(),
                    loan.getBorrowDate(),
                    loan.getDueDate(),
                    returnDate,
                    status,
                    String.format("₱%.2f", fine)
            });
        }

        activeValue.setText(String.valueOf(active));
        overdueValue.setText(String.valueOf(overdue));
        returnedValue.setText(String.valueOf(returned));
        recordsHint.setText(
                library.getLoans().isEmpty()
                        ? "No loan records yet."
                        : tableModel.getRowCount() == 0
                                ? "No matching loans."
                        : "Select an active row to prepare a return.");
    }

    private void showError(String message) {

        RoundedComponents.showMessageDialog(
                this,
                "Unable to complete action",
                message,
                RoundedComponents.DialogType.ERROR);
    }
}
