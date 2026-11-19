package gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import models.Book;
import models.LibraryMember;
import models.Loan;
import services.LibraryService;

public class DashboardPanel extends JPanel {

    private final LibraryService library;

    private final Color BACKGROUND = new Color(245, 244, 239);
    private final Color WHITE = Color.WHITE;
    private final Color DARK = new Color(38, 50, 56);
    private final Color MUTED = new Color(113, 128, 150);
    private final Color GREEN = new Color(57, 134, 106);
    private final Color GOLD = new Color(198, 156, 77);
    private final Color RED = new Color(198, 91, 88);

    private JComboBox<String> activitySortFilter;
    private String activitySortOption = "Newest first";
    private String activitySortSelection = "Sort By";
    private String activeActivityTab = "BORROWED";

    public DashboardPanel(LibraryService library) {

        this.library = library;

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        createDashboard();
    }

    private void createDashboard() {

        JPanel mainContent = new JPanel(new BorderLayout(0, 18));
        mainContent.setOpaque(false);

        JPanel heading = new JPanel(new BorderLayout(16, 0));
        heading.setOpaque(false);

        JPanel headingText = new JPanel(new GridLayout(2, 1, 0, 4));
        headingText.setOpaque(false);

        JLabel title = new JLabel("Library Overview");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "A clear snapshot of your collection and lending activity.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        headingText.add(title);
        headingText.add(subtitle);
        heading.add(headingText, BorderLayout.WEST);
        heading.add(createDateBadge(), BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(createOverviewBanner(), BorderLayout.NORTH);

        JPanel dashboardContent = new JPanel(new BorderLayout(0, 16));
        dashboardContent.setOpaque(false);
        dashboardContent.add(createStatistics(), BorderLayout.NORTH);

        JPanel lowerContent = new JPanel(new GridBagLayout());
        lowerContent.setOpaque(false);
        GridBagConstraints lowerConstraints = new GridBagConstraints();
        lowerConstraints.gridy = 0;
        lowerConstraints.fill = GridBagConstraints.BOTH;
        lowerConstraints.anchor = GridBagConstraints.NORTHWEST;
        lowerConstraints.weightx = 0.4;
        lowerConstraints.weighty = 1;
        lowerConstraints.insets = new Insets(0, 0, 0, 8);
        lowerContent.add(createInventoryPanel(), lowerConstraints);
        lowerConstraints.gridx = 1;
        lowerConstraints.weightx = 0.6;
        lowerConstraints.insets = new Insets(0, 8, 0, 0);
        lowerContent.add(createRecentActivityPanel(), lowerConstraints);
        dashboardContent.add(lowerContent, BorderLayout.CENTER);

        body.add(dashboardContent, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(body);
        RoundedComponents.styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainContent.add(heading, BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    public void refreshDashboard() {
        removeAll();
        createDashboard();
        revalidate();
        repaint();
    }

    private JPanel createDateBadge() {

        RoundedComponents.RoundedPanel badge =
                new RoundedComponents.RoundedPanel(18, WHITE);
        badge.setLayout(new BorderLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));

        JLabel date = new JLabel(
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("EEEE, MMM d")));
        date.setFont(new Font("Arial", Font.BOLD, 11));
        date.setForeground(DARK);
        badge.add(date, BorderLayout.CENTER);

        return badge;
    }

    private JPanel createOverviewBanner() {

        DashboardSurface banner =
                new DashboardSurface(22, new Color(24, 58, 55));
        banner.setLayout(new BorderLayout(24, 0));
        banner.setBorder(
                BorderFactory.createEmptyBorder(14, 24, 18, 28));

        JPanel copy = new JPanel(new GridLayout(3, 1, 0, 3));
        copy.setOpaque(false);

        JLabel eyebrow = new JLabel("WELCOME BACK");
        eyebrow.setFont(new Font("Arial", Font.BOLD, 10));
        eyebrow.setForeground(new Color(226, 190, 122));

        JLabel title = new JLabel("Your library is ready.");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel description = new JLabel(
                "Review collection availability and recent activity below.");
        description.setFont(new Font("Arial", Font.PLAIN, 11));
        description.setForeground(new Color(203, 213, 207));

        copy.add(eyebrow);
        copy.add(title);
        copy.add(description);

        banner.add(copy, BorderLayout.CENTER);

        return banner;
    }

    private JPanel createStatistics() {

        JPanel statistics = new JPanel(new GridLayout(1, 4, 14, 0));
        statistics.setOpaque(false);

        statistics.add(createStatCard(
                "TOTAL COPIES",
                String.valueOf(getTotalBookCopies()),
                "Across registered titles",
                new Color(24, 58, 55),
                "library_books.png"));
        statistics.add(createStatCard(
                "MEMBERS",
                String.valueOf(library.getMembers().size()),
                "Student and faculty readers",
                GOLD,
                "groups.png"));
        statistics.add(createStatCard(
                "ACTIVE LOANS",
                String.valueOf(getActiveLoans()),
                "Currently checked out",
                new Color(76, 116, 173),
                "history.png"));
        statistics.add(createStatCard(
                "OVERDUE",
                String.valueOf(library.getOverdueLoans().size()),
                "Past their due date",
                RED,
                "warning.png"));

        return statistics;
    }

    private JPanel createStatCard(
            String title,
            String value,
            String description,
            Color accent,
            String iconFileName) {

        DashboardSurface card = new DashboardSurface(18, WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 18));
        card.setPreferredSize(new Dimension(180, 104));

        JPanel details = new JPanel(new BorderLayout(0, 1));
        details.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 31));
        valueLabel.setForeground(accent);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        descriptionLabel.setForeground(MUTED);

        RoundedComponents.RoundedPanel iconBadge =
                new RoundedComponents.RoundedPanel(
                        14, blendWithWhite(accent, 0.12));
        iconBadge.setLayout(new BorderLayout());
        iconBadge.setPreferredSize(new Dimension(30, 30));
        JLabel icon = new JLabel(loadStatIcon(iconFileName, accent));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        iconBadge.add(icon, BorderLayout.CENTER);

        JPanel titleRow = new JPanel(new BorderLayout(6, 0));
        titleRow.setOpaque(false);
        titleRow.add(titleLabel, BorderLayout.CENTER);
        titleRow.add(iconBadge, BorderLayout.EAST);

        details.add(titleRow, BorderLayout.NORTH);
        details.add(valueLabel, BorderLayout.CENTER);
        details.add(descriptionLabel, BorderLayout.SOUTH);
        card.add(details, BorderLayout.CENTER);

        return card;
    }

    private Color blendWithWhite(Color color, double amount) {
        return new Color(
                (int) (color.getRed() * amount + 255 * (1 - amount)),
                (int) (color.getGreen() * amount + 255 * (1 - amount)),
                (int) (color.getBlue() * amount + 255 * (1 - amount)));
    }

    private ImageIcon loadStatIcon(String fileName, Color tint) {
        File file = new File("resources/icons/" + fileName);
        if (!file.isFile()) {
            System.err.println("Unable to load dashboard icon: " + fileName);
            return null;
        }

        try {
            BufferedImage original = ImageIO.read(file);
            if (original == null) {
                System.err.println("Unable to load dashboard icon: " + fileName);
                return null;
            }

            BufferedImage tinted = new BufferedImage(
                    18, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = tinted.createGraphics();
            try {
                graphics.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.drawImage(original, 0, 0, 18, 18, null);
                graphics.setComposite(AlphaComposite.SrcIn);
                graphics.setColor(tint);
                graphics.fillRect(0, 0, 18, 18);
            } finally {
                graphics.dispose();
            }

            return new ImageIcon((Image) tinted);
        } catch (IOException exception) {
            System.err.println(
                    "Unable to load dashboard icon " + fileName + ": "
                            + exception.getMessage());
            return null;
        }
    }

    private static final class DashboardSurface extends JPanel {

        private static final long serialVersionUID = 1L;
        private final int cornerRadius;

        private DashboardSurface(int cornerRadius, Color background) {
            this.cornerRadius = cornerRadius;
            setBackground(background);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            for (int offset = 4; offset > 0; offset--) {
                graphics2D.setColor(new Color(15, 23, 42, 4));
                graphics2D.fillRoundRect(
                        1, offset, getWidth() - 5, getHeight() - 5,
                        cornerRadius, cornerRadius);
            }

            graphics2D.setColor(getBackground());
            graphics2D.fillRoundRect(
                    0, 0, getWidth() - 5, getHeight() - 5,
                    cornerRadius, cornerRadius);
            graphics2D.dispose();
        }
    }

    private static final class StackedInventoryBar extends JPanel {

        private static final long serialVersionUID = 1L;
        private final double availableRatio;
        private final boolean hasInventory;

        private StackedInventoryBar(
                double availableRatio,
                boolean hasInventory) {
            this.availableRatio = Math.max(0, Math.min(1, availableRatio));
            this.hasInventory = hasInventory;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(hasInventory
                    ? new Color(226, 190, 122)
                    : new Color(235, 237, 232));
            graphics2D.fillRoundRect(
                    0, 0, getWidth(), getHeight(),
                    getHeight(), getHeight());

            if (hasInventory) {
                java.awt.Shape previousClip = graphics2D.getClip();
                graphics2D.setClip(new java.awt.geom.RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(),
                        getHeight(), getHeight()));
                graphics2D.setColor(new Color(57, 134, 106));
                graphics2D.fillRect(
                        0, 0,
                        (int) Math.round(getWidth() * availableRatio),
                        getHeight());
                graphics2D.setClip(previousClip);
            }
            graphics2D.dispose();
        }
    }

    private JPanel createInventoryPanel() {
        RoundedComponents.RoundedPanel panel =
                new RoundedComponents.RoundedPanel(18, WHITE);
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel heading = new JLabel("Inventory Overview");
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(DARK);

        int total = getTotalBookCopies();
        int available = getAvailableCopies();
        int borrowed = Math.max(0, total - available);
        int availability = total == 0
                ? 0
                : (int) Math.round(available * 100.0 / total);
        ArrayList<Book> lowStockBooks = getLowStockBooks();

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints sectionConstraints = new GridBagConstraints();
        sectionConstraints.gridx = 0;
        sectionConstraints.weightx = 1;
        sectionConstraints.fill = GridBagConstraints.BOTH;

        JPanel availabilitySection =
                createAvailabilitySection(availability, available, total);
        sectionConstraints.gridy = 0;
        sectionConstraints.weighty = 0.36;
        sectionConstraints.insets = new Insets(0, 0, 8, 0);
        content.add(availabilitySection, sectionConstraints);

        JPanel statistics = new JPanel(new GridLayout(1, 2, 8, 0));
        statistics.setOpaque(false);
        statistics.add(createInventoryStatBox(
                "AVAILABLE", available, GREEN));
        statistics.add(createInventoryStatBox(
                "BORROWED", borrowed, GOLD));
        sectionConstraints.gridy = 1;
        sectionConstraints.weighty = 0.28;
        content.add(statistics, sectionConstraints);

        JPanel details = new JPanel(new GridLayout(2, 1, 0, 1));
        details.setOpaque(false);
        details.add(createInventorySummaryRow(
                "Registered titles",
                library.getBooks().size(),
                "library_books.png",
                new Color(76, 116, 173)));
        details.add(createInventorySummaryRow(
                "Low stock alerts",
                lowStockBooks.size(),
                "warning.png",
                lowStockBooks.isEmpty() ? GREEN : GOLD));
        sectionConstraints.gridy = 2;
        sectionConstraints.weighty = 0.22;
        content.add(details, sectionConstraints);

        JPanel stockAlert = createStockAlert(lowStockBooks);
        sectionConstraints.gridy = 3;
        sectionConstraints.weighty = 0.14;
        sectionConstraints.insets = new Insets(0, 0, 0, 0);
        content.add(stockAlert, sectionConstraints);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAvailabilitySection(
            int availability,
            int available,
            int total) {
        RoundedComponents.RoundedPanel section =
                new RoundedComponents.RoundedPanel(
                        12, new Color(248, 248, 244));
        section.setLayout(new BorderLayout(0, 6));
        section.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel label = new JLabel("COLLECTION AVAILABILITY");
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(MUTED);

        JPanel values = new JPanel(new BorderLayout());
        values.setOpaque(false);
        JLabel percentage = new JLabel(availability + "%");
        percentage.setFont(new Font("Arial", Font.BOLD, 28));
        percentage.setForeground(GREEN);
        JLabel description = new JLabel(
                available + " of " + total + " copies available");
        description.setFont(new Font("Arial", Font.PLAIN, 11));
        description.setForeground(MUTED);
        values.add(percentage, BorderLayout.WEST);
        values.add(description, BorderLayout.EAST);

        int borrowed = Math.max(0, total - available);
        StackedInventoryBar distribution = new StackedInventoryBar(
                total == 0 ? 0 : (double) available / total,
                total > 0);
        distribution.setPreferredSize(new Dimension(0, 12));

        section.add(label, BorderLayout.NORTH);
        section.add(values, BorderLayout.CENTER);
        section.add(distribution, BorderLayout.SOUTH);
        section.setToolTipText(
                available + " available, " + borrowed + " borrowed");
        return section;
    }

    private JPanel createInventoryStatBox(
            String title,
            int value,
            Color accent) {
        RoundedComponents.RoundedPanel box =
                new RoundedComponents.RoundedPanel(
                        12, blendWithWhite(accent, 0.1));
        box.setLayout(new BorderLayout(0, 2));
        box.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel count = new JLabel(String.valueOf(value));
        count.setFont(new Font("Arial", Font.BOLD, 22));
        count.setForeground(accent);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(MUTED);

        box.add(count, BorderLayout.CENTER);
        box.add(label, BorderLayout.SOUTH);
        return box;
    }

    private JPanel createInventorySummaryRow(
            String title,
            int value,
            String iconName,
            Color accent) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JPanel labelPanel = new JPanel(new BorderLayout(7, 0));
        labelPanel.setOpaque(false);
        JLabel icon = new JLabel(loadStatIcon(iconName, accent));
        labelPanel.add(icon, BorderLayout.WEST);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(MUTED);
        labelPanel.add(label, BorderLayout.CENTER);

        JLabel count = new JLabel(String.valueOf(value));
        count.setFont(new Font("Arial", Font.BOLD, 12));
        count.setForeground(accent);
        row.add(labelPanel, BorderLayout.CENTER);
        row.add(count, BorderLayout.EAST);
        return row;
    }

    private JPanel createStockAlert(ArrayList<Book> lowStockBooks) {
        boolean hasAlerts = !lowStockBooks.isEmpty();
        Color accent = hasAlerts ? new Color(154, 107, 35) : GREEN;
        Color background = hasAlerts
                ? new Color(252, 239, 214)
                : new Color(231, 244, 237);
        RoundedComponents.RoundedPanel alert =
                new RoundedComponents.RoundedPanel(12, background);
        alert.setLayout(new BorderLayout(8, 0));
        alert.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel icon = new JLabel(loadStatIcon(
                hasAlerts ? "warning.png" : "check.png", accent));
        JLabel message = new JLabel(hasAlerts
                ? lowStockBooks.size() + (lowStockBooks.size() == 1
                        ? " title has low availability."
                        : " titles have low availability.")
                : "All books have sufficient stock.");
        message.setFont(new Font("Arial", Font.BOLD, 11));
        message.setForeground(accent);

        alert.add(icon, BorderLayout.WEST);
        alert.add(message, BorderLayout.CENTER);
        if (hasAlerts) {
            StringBuilder titles = new StringBuilder();
            for (Book book : lowStockBooks) {
                if (titles.length() > 0) {
                    titles.append(", ");
                }
                titles.append(book.getTitle());
            }
            alert.setToolTipText("Low-stock titles: " + titles);
        }
        return alert;
    }

    private ArrayList<Book> getLowStockBooks() {
        ArrayList<Book> lowStockBooks = new ArrayList<>();
        for (Book book : library.getBooks()) {
            if (!book.isArchived() && book.getAvailableCopies() <= 1) {
                lowStockBooks.add(book);
            }
        }
        lowStockBooks.sort(
                Comparator.comparingInt(Book::getAvailableCopies)
                        .thenComparing(
                                Book::getTitle,
                                String.CASE_INSENSITIVE_ORDER));
        return lowStockBooks;
    }

    private JPanel createRecentActivityPanel() {

        RoundedComponents.RoundedPanel panel =
                new RoundedComponents.RoundedPanel(18, WHITE);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(
                BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel sectionHeader = new JPanel(new BorderLayout(10, 0));
        sectionHeader.setOpaque(false);

        JLabel heading = new JLabel("Recent Activity");
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(DARK);

        activitySortFilter = new JComboBox<>(new String[] {
                "Sort By",
                "Newest first",
                "Oldest first",
                "Book title A-Z",
                "Member name A-Z"
        });
        activitySortFilter.setSelectedItem(activitySortSelection);
        RoundedComponents.styleComboBox(
                activitySortFilter, new Dimension(160, 40));
        activitySortFilter.setToolTipText(
                "Sort recent activity.");
        activitySortFilter.addActionListener(event -> {
            activitySortSelection =
                    String.valueOf(activitySortFilter.getSelectedItem());
            activitySortOption = "Sort By".equals(activitySortSelection)
                    ? "Newest first"
                    : activitySortSelection;
            refreshDashboard();
        });

        sectionHeader.add(heading, BorderLayout.WEST);
        sectionHeader.add(activitySortFilter, BorderLayout.EAST);
        panel.add(sectionHeader, BorderLayout.NORTH);

        JPanel activityContent = new JPanel(
                new BorderLayout(0, 10));
        activityContent.setOpaque(false);

        JPanel tabs = new JPanel(
                new java.awt.FlowLayout(
                        java.awt.FlowLayout.LEFT, 6, 0));
        tabs.setOpaque(false);
        RoundedComponents.RoundedButton borrowedButton =
                createActivityTabButton(
                        "Borrowed", "BORROWED".equals(activeActivityTab));
        RoundedComponents.RoundedButton returnedButton =
                createActivityTabButton(
                        "Returned", "RETURNED".equals(activeActivityTab));
        tabs.add(borrowedButton);
        tabs.add(returnedButton);

        CardLayout activityLayout = new CardLayout();
        JPanel activityViews = new JPanel(activityLayout);
        activityViews.setOpaque(false);
        activityViews.add(createActivityTable(false), "BORROWED");
        activityViews.add(createActivityTable(true), "RETURNED");
        activityLayout.show(activityViews, activeActivityTab);

        borrowedButton.addActionListener(event -> {
            activeActivityTab = "BORROWED";
            activityLayout.show(activityViews, activeActivityTab);
            updateActivityTabButtons(borrowedButton, returnedButton);
        });
        returnedButton.addActionListener(event -> {
            activeActivityTab = "RETURNED";
            activityLayout.show(activityViews, activeActivityTab);
            updateActivityTabButtons(returnedButton, borrowedButton);
        });

        activityContent.add(tabs, BorderLayout.NORTH);
        activityContent.add(activityViews, BorderLayout.CENTER);
        panel.add(activityContent, BorderLayout.CENTER);
        return panel;
    }

    private RoundedComponents.RoundedButton createActivityTabButton(
            String text,
            boolean selected) {

        RoundedComponents.RoundedButton button =
                new RoundedComponents.RoundedButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setPreferredSize(new Dimension(92, 30));
        button.setCursor(new java.awt.Cursor(
                java.awt.Cursor.HAND_CURSOR));
        setActivityTabStyle(button, selected);
        return button;
    }

    private void updateActivityTabButtons(
            JButton selected,
            JButton unselected) {
        setActivityTabStyle(selected, true);
        setActivityTabStyle(unselected, false);
    }

    private void setActivityTabStyle(JButton button, boolean selected) {
        button.setBackground(selected
                ? new Color(24, 58, 55)
                : new Color(239, 240, 235));
        button.setForeground(selected ? WHITE : DARK);
    }

    private javax.swing.JComponent createActivityTable(boolean returnedOnly) {
        ArrayList<Loan> loans = new ArrayList<>(library.getLoans());
        loans.removeIf(loan -> loan.isReturned() != returnedOnly);

        Map<String, String> bookTitles = new HashMap<>();
        for (Book book : library.getBooks()) {
            bookTitles.put(book.getIsbn(), book.getTitle());
        }

        Map<String, String> memberNames = new HashMap<>();
        for (LibraryMember member : library.getMembers()) {
            memberNames.put(member.getMemberId(), member.getFullname());
        }

        loans.sort(createActivityComparator(
                returnedOnly, bookTitles, memberNames));

        if (loans.isEmpty()) {
            return createEmptyActivityState();
        }

        String[] columns = {
                "LOAN",
                "BOOK",
                "MEMBER",
                "DUE DATE",
                "STATUS"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? LocalDate.class : String.class;
            }
        };

        for (Loan loan : loans) {
            if (model.getRowCount() >= 8) {
                break;
            }
            String memberName = memberNames.getOrDefault(
                    loan.getMemberId(), "Unknown");
            String bookTitle = bookTitles.getOrDefault(
                    loan.getBookIsbn(), loan.getBookIsbn());
            String status = loan.isReturned()
                    ? "Returned"
                    : loan.isOverdue() ? "Overdue" : "Borrowed";

            model.addRow(new Object[] {
                    loan.getLoanId(),
                    bookTitle,
                    memberName,
                    loan.getDueDate(),
                    status
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setRowHeight(40);
        table.setForeground(DARK);
        table.setBackground(WHITE);
        table.setGridColor(new Color(239, 240, 235));
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        table.setSelectionBackground(new Color(232, 240, 235));
        table.setSelectionForeground(DARK);
        table.setFillsViewportHeight(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
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

                        JLabel label = (JLabel) super
                                .getTableCellRendererComponent(
                                        source, value, isSelected,
                                        hasFocus, row, column);
                        label.setBorder(
                                BorderFactory.createEmptyBorder(0, 8, 0, 8));
                        if (!isSelected) {
                            label.setBackground(row % 2 == 0
                                    ? WHITE
                                    : new Color(250, 250, 247));
                            label.setForeground(DARK);
                        }
                        return label;
                    }
                });
        table.getColumnModel().getColumn(3).setCellRenderer(
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

                        JLabel label = (JLabel) super
                                .getTableCellRendererComponent(
                                        source, value, isSelected,
                                        hasFocus, row, column);
                        label.setText(value instanceof LocalDate
                                ? ((LocalDate) value).format(
                                        DateTimeFormatter.ofPattern(
                                                "MMM d, yyyy"))
                                : "");
                        label.setBorder(
                                BorderFactory.createEmptyBorder(0, 8, 0, 8));
                        if (!isSelected) {
                            label.setBackground(row % 2 == 0
                                    ? WHITE
                                    : new Color(250, 250, 247));
                            label.setForeground(DARK);
                        }
                        return label;
                    }
                });

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 10));
        tableHeader.setBackground(new Color(248, 248, 244));
        tableHeader.setForeground(MUTED);
        tableHeader.setPreferredSize(new Dimension(0, 38));
        tableHeader.setOpaque(true);
        tableHeader.setReorderingAllowed(false);

        table.getColumnModel().getColumn(4).setCellRenderer(
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

                        JLabel label = (JLabel) super
                                .getTableCellRendererComponent(
                                        source, value, isSelected,
                                        hasFocus, row, column);
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        label.setBorder(
                                BorderFactory.createEmptyBorder(0, 4, 0, 4));

                        if (!isSelected) {
                            String status = String.valueOf(value);
                            if ("Overdue".equals(status)) {
                                label.setBackground(new Color(252, 235, 233));
                                label.setForeground(new Color(169, 62, 58));
                            } else if ("Returned".equals(status)) {
                                label.setBackground(new Color(231, 244, 237));
                                label.setForeground(new Color(45, 112, 82));
                            } else {
                                label.setBackground(new Color(248, 243, 229));
                                label.setForeground(new Color(142, 105, 40));
                            }
                        }

                        return label;
                    }
                });
        int[] preferredWidths = { 92, 145, 130, 105, 84 };
        for (int column = 0; column < preferredWidths.length; column++) {
            table.getColumnModel().getColumn(column)
                    .setPreferredWidth(preferredWidths[column]);
        }

        JScrollPane tableScroll = new JScrollPane(table);
        RoundedComponents.styleScrollPane(tableScroll);
        tableScroll.setBorder(BorderFactory.createLineBorder(
                new Color(232, 233, 226)));
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setBackground(
                new Color(248, 248, 244));
        tableScroll.setPreferredSize(new Dimension(0, 190));
        return tableScroll;
    }

    private Comparator<Loan> createActivityComparator(
            boolean returnedOnly,
            Map<String, String> bookTitles,
            Map<String, String> memberNames) {

        Comparator<LocalDate> newestDateFirst = Comparator.nullsLast(
                Comparator.reverseOrder());
        Comparator<Loan> byActivityDate = Comparator.comparing(
                (Loan loan) -> returnedOnly
                        ? loan.getReturnDate()
                        : loan.getBorrowDate(),
                newestDateFirst);

        if ("Oldest first".equals(activitySortOption)) {
            return Comparator.comparing(
                    (Loan loan) -> returnedOnly
                            ? loan.getReturnDate()
                            : loan.getBorrowDate(),
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("Book title A-Z".equals(activitySortOption)) {
            return Comparator.comparing(
                            (Loan loan) -> bookTitles.getOrDefault(
                                    loan.getBookIsbn(), loan.getBookIsbn()),
                            String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(byActivityDate);
        }
        if ("Member name A-Z".equals(activitySortOption)) {
            return Comparator.comparing(
                            (Loan loan) -> memberNames.getOrDefault(
                                    loan.getMemberId(), loan.getMemberId()),
                            String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(byActivityDate);
        }
        return byActivityDate;
    }

    private JPanel createEmptyActivityState() {

        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(false);

        JPanel copy = new JPanel(new GridLayout(2, 1, 0, 6));
        copy.setOpaque(false);
        JLabel title = new JLabel(
                "No borrowing activity yet", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(DARK);

        JLabel description = new JLabel(
                "New loan records will appear here.",
                SwingConstants.CENTER);
        description.setFont(new Font("Arial", Font.PLAIN, 11));
        description.setForeground(MUTED);

        copy.add(title);
        copy.add(description);
        emptyState.add(copy);
        return emptyState;
    }

    private int getTotalBookCopies() {

        int total = 0;
        for (Book book : library.getBooks()) {
            total += book.getTotalCopies();
        }
        return total;
    }

    private int getAvailableCopies() {

        int total = 0;
        for (Book book : library.getBooks()) {
            total += book.getAvailableCopies();
        }
        return total;
    }

    private int getActiveLoans() {

        int total = 0;
        for (Loan loan : library.getLoans()) {
            if (!loan.isReturned()) {
                total++;
            }
        }
        return total;
    }
}
