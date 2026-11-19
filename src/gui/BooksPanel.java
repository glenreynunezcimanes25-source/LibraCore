package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import models.Book;
import services.LibraryService;

public class BooksPanel extends JPanel {

    private final LibraryService library;

    private final Color BACKGROUND = new Color(245, 244, 239);
    private final Color DARK = new Color(38, 50, 56);
    private final Color GREEN = new Color(24, 58, 55);
    private final Color GOLD = new Color(198, 156, 77);
    private final Color MUTED = new Color(113, 128, 150);
    private final Color WHITE = Color.WHITE;

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> availabilityFilter;
    private JButton editBookButton;
    private JButton archiveBookButton;
    private boolean updatingFilters;

    private JLabel totalBooksValue;
    private JLabel availableBooksValue;
    private JLabel borrowedBooksValue;
    private JLabel totalTitlesValue;

    public BooksPanel(LibraryService library) {

        this.library = library;

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        setBorder(
                BorderFactory.createEmptyBorder(28, 32, 28, 32));

        createInterface();
        refreshTable();
    }

    private void createInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 22));
        mainPanel.setOpaque(false);

        // Heading
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JLabel title = new JLabel("Books Management");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Manage, organize, and monitor your library collection.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        JPanel headingText = new JPanel(new GridLayout(2, 1, 0, 5));
        headingText.setOpaque(false);
        headingText.add(title);
        headingText.add(subtitle);

        heading.add(headingText, BorderLayout.WEST);

        mainPanel.add(heading, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);

        // Statistics
        JPanel statistics = new JPanel(
                new GridLayout(1, 4, 15, 0));
        statistics.setOpaque(false);

        totalBooksValue = new JLabel("0");
        availableBooksValue = new JLabel("0");
        borrowedBooksValue = new JLabel("0");
        totalTitlesValue = new JLabel("0");

        statistics.add(createStatCard(
                "TOTAL COPIES", totalBooksValue,
                "Across all registered titles", GREEN, "library_books.png"));
        statistics.add(createStatCard(
                "AVAILABLE", availableBooksValue,
                "Ready to borrow", new Color(57, 134, 106), "check.png"));
        statistics.add(createStatCard(
                "BORROWED", borrowedBooksValue,
                "Currently checked out", GOLD, "sync.png"));
        statistics.add(createStatCard(
                "REGISTERED TITLES", totalTitlesValue,
                "Unique books in the catalog", new Color(76, 116, 173),
                "menu_book.png"));

        content.add(statistics, BorderLayout.NORTH);

        // Book table section
        RoundedComponents.RoundedPanel tablePanel =
                new RoundedComponents.RoundedPanel(18, WHITE);
        tablePanel.setLayout(new BorderLayout(0, 18));

        tablePanel.setBorder(
                BorderFactory.createEmptyBorder(20, 22, 22, 22));

        // Search and buttons
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(350, 40));

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 225, 218)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        searchField.setToolTipText(
                "Search by ISBN, title, or author");

        JButton searchButton = createButton(
                "Search", "search.png", GREEN, WHITE);

        searchButton.addActionListener(event -> searchBooks());
        searchField.addActionListener(event -> searchBooks());
        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent event) {
                        searchBooks();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent event) {
                        searchBooks();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent event) {
                        searchBooks();
                    }
                });

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        toolbar.add(searchPanel, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        categoryFilter = new JComboBox<>(new String[] { "All categories" });
        availabilityFilter = new JComboBox<>(
                new String[] { "All books", "Available", "Unavailable", "Archived" });
        RoundedComponents.styleComboBox(
                categoryFilter, new Dimension(160, 40));
        RoundedComponents.styleComboBox(
                availabilityFilter, new Dimension(150, 40));
        categoryFilter.addActionListener(event -> {
            if (!updatingFilters) {
                refreshTable();
            }
        });
        availabilityFilter.addActionListener(event -> refreshTable());
        filters.add(categoryFilter);
        filters.add(availabilityFilter);
        toolbar.add(filters, BorderLayout.CENTER);

        JPanel actionButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);

        JButton refreshButton = createButton(
                "Refresh", "refresh.png", GREEN, WHITE);

        refreshButton.addActionListener(event -> {
            searchField.setText("");
            refreshTable();
        });

        JButton addButton = createButton(
                "Add Book", "add.png", GOLD, GREEN);

        addButton.addActionListener(event -> addBook());

        actionButtons.add(refreshButton);
        actionButtons.add(addButton);

        toolbar.add(actionButtons, BorderLayout.EAST);

        tablePanel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "ISBN",
                "BOOK TITLE",
                "AUTHOR",
                "CATEGORY",
                "TOTAL COPIES",
                "AVAILABLE",
                "STATUS"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);

        bookTable.setFont(new Font("Arial", Font.PLAIN, 12));
        bookTable.setForeground(DARK);
        bookTable.setBackground(WHITE);
        bookTable.setRowHeight(42);
        bookTable.setShowVerticalLines(false);
        bookTable.setGridColor(new Color(235, 235, 230));
        bookTable.setSelectionBackground(
                new Color(232, 222, 198));
        bookTable.setSelectionForeground(DARK);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        RoundedComponents.enableCellCopy(bookTable);
        bookTable.setAutoCreateRowSorter(true);
        RoundedComponents.configureTableSorter(bookTable);

        JTableHeader header = bookTable.getTableHeader();

        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 248, 244));
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        RoundedComponents.styleScrollPane(scrollPane);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(235, 235, 230)));
        scrollPane.getViewport().setBackground(WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom actions
        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        editBookButton = createButton(
                "Edit Selected", "save.png", GREEN, WHITE);

        archiveBookButton = createButton(
                "Archive Selected", "delete.png",
                new Color(198, 91, 88), WHITE);
        archiveBookButton.setPreferredSize(new Dimension(155, 40));
        editBookButton.setEnabled(false);
        archiveBookButton.setEnabled(false);

        editBookButton.addActionListener(event -> editBook());
        archiveBookButton.addActionListener(event -> archiveBook());
        bookTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateBookActionButtons();
            }
        });

        bottomPanel.add(editBookButton);
        bottomPanel.add(archiveBookButton);

        tablePanel.add(bottomPanel, BorderLayout.SOUTH);

        content.add(tablePanel, BorderLayout.CENTER);

        mainPanel.add(content, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(
            String title,
            JLabel value,
            String description,
            Color accent,
            String iconName) {
        return RoundedComponents.createStatCard(
                title, value, description, accent, loadIcon(iconName, accent));
    }

    private JButton createButton(
            String text,
            String iconName,
            Color background,
            Color foreground) {

        JButton button = new RoundedComponents.RoundedButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);

        button.setIcon(loadIcon(iconName, foreground));
        button.setIconTextGap(8);

        button.setPreferredSize(new Dimension(135, 40));
        button.setCursor(
                new java.awt.Cursor(
                        java.awt.Cursor.HAND_CURSOR));

        return button;
    }

    private ImageIcon loadIcon(String fileName, Color tint) {

        java.io.File file = new java.io.File(
                "resources/icons/" + fileName);

        if (!file.exists()) {
            return null;
        }

        ImageIcon original = new ImageIcon(file.getPath());
        BufferedImage tinted = new BufferedImage(
                18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = tinted.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(
                    original.getImage(), 0, 0, 18, 18, null);
            graphics.setComposite(java.awt.AlphaComposite.SrcIn);
            graphics.setColor(tint);
            graphics.fillRect(0, 0, 18, 18);
        } finally {
            graphics.dispose();
        }

        return new ImageIcon((Image) tinted);
    }

    public void refreshTable() {

        tableModel.setRowCount(0);

        ArrayList<Book> books = library.getBooks();
        refreshCategoryOptions(books);
        String keyword = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        for (Book book : books) {

            if (matchesBookFilters(book, keyword)) {
                addBookRow(book);
            }
        }

        updateBookActionButtons();
        updateStatistics();
    }

    private void updateBookActionButtons() {
        boolean hasSelection = bookTable.getSelectedRow() >= 0;
        editBookButton.setEnabled(hasSelection);
        archiveBookButton.setEnabled(hasSelection);
    }

    private void refreshCategoryOptions(ArrayList<Book> books) {
        String selected = categoryFilter.getSelectedItem() == null
                ? "All categories"
                : categoryFilter.getSelectedItem().toString();
        java.util.TreeSet<String> categories = new java.util.TreeSet<>(
                String.CASE_INSENSITIVE_ORDER);
        for (Book book : books) {
            categories.add(book.getCategory());
        }

        updatingFilters = true;
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All categories");
        for (String category : categories) {
            categoryFilter.addItem(category);
        }
        categoryFilter.setSelectedItem(
                categories.contains(selected) ? selected : "All categories");
        updatingFilters = false;
    }

    private boolean matchesBookFilters(Book book, String keyword) {
        if (!matchesSearch(book, keyword)) {
            return false;
        }

        String selectedCategory = categoryFilter.getSelectedItem() == null
                ? "All categories"
                : categoryFilter.getSelectedItem().toString();
        if (!"All categories".equals(selectedCategory)
                && !book.getCategory().equalsIgnoreCase(selectedCategory)) {
            return false;
        }

        String availability = availabilityFilter.getSelectedItem() == null
                ? "All books"
                : availabilityFilter.getSelectedItem().toString();
        if ("Archived".equals(availability)) {
            return book.isArchived();
        }
        if (book.isArchived()) {
            return false;
        }
        if ("Available".equals(availability)) {
            return book.isAvailable();
        }
        if ("Unavailable".equals(availability)) {
            return !book.isAvailable();
        }
        return true;
    }

    private void updateStatistics() {

        int total = 0;
        int available = 0;

        for (Book book : library.getBooks()) {

            total += book.getTotalCopies();
            available += book.getAvailableCopies();
        }

        totalBooksValue.setText(String.valueOf(total));
        availableBooksValue.setText(String.valueOf(available));
        borrowedBooksValue.setText(String.valueOf(total - available));
        totalTitlesValue.setText(
                String.valueOf(library.getBooks().size()));
    }

    private void searchBooks() {

        refreshTable();
    }

    private boolean matchesSearch(Book book, String keyword) {

        return book.getIsbn().toLowerCase(Locale.ROOT).contains(keyword)
                || book.getTitle().toLowerCase(Locale.ROOT).contains(keyword)
                || book.getAuthor().toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void addBookRow(Book book) {

        String status = book.isArchived()
                ? "Archived"
                : book.getAvailableCopies() > 0
                        ? "Available"
                        : "Unavailable";

        tableModel.addRow(new Object[] {
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                status
        });
    }

    private void addBook() {

        JTextField isbnField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField copiesField = new JTextField();

        JPanel form = createBookForm(
                isbnField,
                titleField,
                authorField,
                categoryField,
                copiesField);

        if (!showBookFormDialog(
                "Add New Book",
                "Add the book details to your collection.",
                "Save Book",
                form)) {
            return;
        }

        try {

            int copies = Integer.parseInt(
                    copiesField.getText().trim());

            Book book = new Book(
                    isbnField.getText(),
                    titleField.getText(),
                    authorField.getText(),
                    categoryField.getText(),
                    copies, copies);

            library.addBook(book);

            refreshTable();

            RoundedComponents.showMessageDialog(
                    this,
                    "Book added",
                    "Book successfully added.",
                    RoundedComponents.DialogType.SUCCESS);

        } catch (NumberFormatException exception) {

            showError("Please enter a valid number of copies.");

        } catch (IllegalArgumentException | IllegalStateException exception) {

            showError(exception.getMessage());
        }
    }

    private void editBook() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a book to edit.");
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(
                selectedRow);

        String isbn = tableModel.getValueAt(
                modelRow, 0).toString();

        Book book = library.findBookByIsbn(isbn);

        if (book == null) {
            showError("The selected book could not be found.");
            refreshTable();
            return;
        }

        JTextField isbnField = new JTextField(book.getIsbn());
        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());
        JTextField categoryField = new JTextField(book.getCategory());
        JTextField copiesField = new JTextField(
                String.valueOf(book.getTotalCopies()));

        // ISBN is the book's identifier and cannot be changed here.
        isbnField.setEditable(false);

        JPanel form = createBookForm(
                isbnField,
                titleField,
                authorField,
                categoryField,
                copiesField);

        if (!showBookFormDialog(
                "Edit Book",
                "Update the book details. ISBN cannot be changed.",
                "Save Changes",
                form)) {
            return;
        }

        try {

            int copies = Integer.parseInt(
                    copiesField.getText().trim());

            library.updateBook(
                    isbn,
                    titleField.getText(),
                    authorField.getText(),
                    categoryField.getText(),
                    copies);

            refreshTable();

            RoundedComponents.showMessageDialog(
                    this,
                    "Book updated",
                    "Book information updated successfully.",
                    RoundedComponents.DialogType.SUCCESS);

        } catch (NumberFormatException exception) {

            showError("Please enter a valid number of copies.");

        } catch (IllegalArgumentException | IllegalStateException exception) {

            showError(exception.getMessage());
        }
    }

    private void archiveBook() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a book to archive.");
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(
                selectedRow);

        String isbn = tableModel.getValueAt(
                modelRow, 0).toString();

        String title = tableModel.getValueAt(
                modelRow, 1).toString();

        boolean confirmation = RoundedComponents.showWarningDialog(
                this,
                "Archive book?",
                "Archive \"" + title
                        + "\"? Its loan history will be retained.",
                "Archive Book");

        if (!confirmation) {
            return;
        }

        try {

            library.archiveBook(isbn);

            refreshTable();

            RoundedComponents.showMessageDialog(
                    this,
                    "Book archived",
                    "Book archived; its history has been retained.",
                    RoundedComponents.DialogType.SUCCESS);

        } catch (IllegalArgumentException | IllegalStateException exception) {

            showError(exception.getMessage());
        }
    }

    private JPanel createBookForm(
            JTextField isbn,
            JTextField title,
            JTextField author,
            JTextField category,
            JTextField copies) {

        JPanel form = new JPanel(new GridLayout(5, 2, 12, 12));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(390, 220));

        String[] labels = {
                "ISBN",
                "Book Title",
                "Author",
                "Category",
                "Total Copies"
        };
        JTextField[] fields = {
                isbn,
                title,
                author,
                category,
                copies
        };

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setForeground(DARK);

            JTextField field = fields[i];
            field.setFont(new Font("Arial", Font.PLAIN, 13));
            field.setBackground(
                    field.isEditable() ? WHITE : BACKGROUND);
            field.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    new Color(225, 225, 218)),
                            BorderFactory.createEmptyBorder(0, 10, 0, 10)));

            form.add(label);
            form.add(field);
        }

        return form;
    }

    //
    private boolean showBookFormDialog(
            String title,
            String subtitle,
            String saveText,
            JPanel form) {

        boolean[] confirmed = { false };
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                title,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(BACKGROUND);
        content.setBorder(
                BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(
                heading, javax.swing.BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 21));
        titleLabel.setForeground(DARK);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED);

        heading.add(titleLabel);
        heading.add(javax.swing.Box.createVerticalStrut(6));
        heading.add(subtitleLabel);

        RoundedComponents.RoundedPanel formCard =
                new RoundedComponents.RoundedPanel(18, WHITE);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(
                BorderFactory.createEmptyBorder(18, 18, 18, 18));
        formCard.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton cancelButton = createDialogButton(
                "Cancel", new Color(232, 233, 226), DARK);
        cancelButton.addActionListener(event -> dialog.dispose());

        JButton saveButton = createDialogButton(
                saveText, GREEN, WHITE);
        saveButton.addActionListener(event -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        actions.add(cancelButton);
        actions.add(saveButton);

        content.add(heading, BorderLayout.NORTH);
        content.add(formCard, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return confirmed[0];
    }

    private JButton createDialogButton(
            String text,
            Color background,
            Color foreground) {

        JButton button = new RoundedComponents.RoundedButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setPreferredSize(new Dimension(130, 40));
        button.setCursor(
                new java.awt.Cursor(
                        java.awt.Cursor.HAND_CURSOR));

        return button;
    }

    private void showError(String message) {

        RoundedComponents.showMessageDialog(
                this,
                "Unable to complete action",
                message,
                RoundedComponents.DialogType.ERROR);
    }

}
