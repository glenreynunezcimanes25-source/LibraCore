package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import gui.RoundedComponents.RoundedPanel;
import models.FacultyMember;
import models.LibraryMember;
import models.LibraryMember.MemberStatus;
import models.Loan;
import models.StudentMember;
import services.LibraryService;

public class MembersPanel extends JPanel {

        private static final String FONT_FAMILY = "Segoe UI";

        private final LibraryService library;

        private final Color BACKGROUND = new Color(245, 244, 239);
        private final Color DARK = new Color(38, 50, 56);
        private final Color GREEN = new Color(22, 63, 58);
        private final Color GOLD = new Color(208, 163, 78);
        private final Color MUTED = new Color(113, 128, 150);
        private final Color WHITE = Color.WHITE;

        private JTable memberTable;
        private DefaultTableModel tableModel;
        private JPanel memberTableContent;
        private CardLayout memberTableContentLayout;
        private JLabel memberTableEmptyMessage;
        private JPanel memberDetailsContent;
        private CardLayout memberDetailsLayout;
        private JTextField searchField;
        private JComboBox<String> typeFilter;
        private JComboBox<String> statusFilter;
        private JButton editButton;
        private JButton historyButton;
        private JButton archiveButton;
        private JButton previousPageButton;
        private JButton nextPageButton;
        private JLabel pageLabel;
        private JLabel selectedMemberHeading;
        private JLabel maxLoansValue;
        private JLabel loanPeriodValue;
        private JLabel finesValue;
        private JLabel registrationDateValue;
        private JLabel historyValue;
        private JPanel paginationPanel;
        private final ArrayList<LibraryMember> filteredMembers = new ArrayList<>();
        private final ArrayList<LibraryMember> visibleMembers = new ArrayList<>();
        private final Map<String, Integer> activeLoanCounts = new HashMap<>();
        private static final int PAGE_SIZE = 25;
        private int currentPage;
        private int sortColumn = 1;
        private boolean sortAscending = true;

        private JLabel totalMembersValue;
        private JLabel studentsValue;
        private JLabel facultyValue;

        public MembersPanel(LibraryService library) {

                this.library = library;

                setLayout(new BorderLayout());
                setBackground(BACKGROUND);

                setBorder(
                                BorderFactory.createEmptyBorder(28, 32, 28, 32));

                createInterface();
                refreshTable();
        }

        private void createInterface() {

                JPanel mainPanel = new JPanel(new BorderLayout(0, 18));
                mainPanel.setOpaque(false);

                // Heading
                JPanel heading = new JPanel(new BorderLayout());
                heading.setOpaque(false);

                JPanel headingText = new JPanel(
                                new GridLayout(2, 1, 0, 5));
                headingText.setOpaque(false);

                JLabel title = new JLabel("Members Management");
                title.setFont(new Font(FONT_FAMILY, Font.BOLD, 25));
                title.setForeground(DARK);

                JLabel subtitle = new JLabel(
                                "Manage student and faculty library memberships.");
                subtitle.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
                subtitle.setForeground(MUTED);

                headingText.add(title);
                headingText.add(subtitle);

                heading.add(headingText, BorderLayout.WEST);

                mainPanel.add(heading, BorderLayout.NORTH);

                // Main content
                JPanel content = new JPanel(new BorderLayout(0, 18));
                content.setOpaque(false);

                // Statistics
                JPanel statistics = new JPanel(new GridLayout(1, 3, 12, 0));
                statistics.setOpaque(false);

                totalMembersValue = new JLabel("0");
                studentsValue = new JLabel("0");
                facultyValue = new JLabel("0");

                statistics.add(createMemberStatisticCard(
                                "TOTAL MEMBERS", totalMembersValue,
                                "Registered library members", GREEN, "groups.png"));
                statistics.add(createMemberStatisticCard(
                                "STUDENT MEMBERS", studentsValue,
                                "Student accounts", new Color(57, 134, 106), "school.png"));
                statistics.add(createMemberStatisticCard(
                                "FACULTY MEMBERS", facultyValue,
                                "Faculty accounts", GOLD, "person_add.png"));

                content.add(statistics, BorderLayout.NORTH);

                // Table panel
                RoundedPanel tablePanel = new RoundedPanel(18, WHITE);

                tablePanel.setLayout(new BorderLayout(0, 18));

                tablePanel.setBorder(
                                BorderFactory.createEmptyBorder(20, 22, 22, 22));

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
                                                BorderFactory.createEmptyBorder(
                                                                8, 12, 8, 12)));

                searchField.setToolTipText(
                                "Search by Member ID or Full Name");

                searchField.addActionListener(event -> searchMembers());
                searchField.getDocument().addDocumentListener(
                                new DocumentListener() {

                                        @Override
                                        public void insertUpdate(DocumentEvent event) {
                                                searchMembers();
                                        }

                                        @Override
                                        public void removeUpdate(DocumentEvent event) {
                                                searchMembers();
                                        }

                                        @Override
                                        public void changedUpdate(DocumentEvent event) {
                                                searchMembers();
                                        }
                                });

                JButton searchButton = createToolbarButton(
                                "Search",
                                "search.png",
                                new Color(24, 58, 55),
                                WHITE);
                searchButton.addActionListener(event -> searchMembers());
                searchPanel.add(searchField, BorderLayout.CENTER);
                searchPanel.add(searchButton, BorderLayout.EAST);
                toolbar.add(searchPanel, BorderLayout.WEST);

                typeFilter = new JComboBox<>(
                                new String[] { "All types", "Student", "Faculty" });
                statusFilter = new JComboBox<>(
                                new String[] { "All statuses", "Active", "Inactive",
                                                "Suspended", "Archived" });
                RoundedComponents.styleComboBox(typeFilter, new Dimension(160, 40));
                RoundedComponents.styleComboBox(statusFilter, new Dimension(150, 40));
                typeFilter.setToolTipText("Filter members by type.");
                statusFilter.setToolTipText("Filter members by status.");
                typeFilter.addActionListener(event -> resetAndRefreshTable());
                statusFilter.addActionListener(event -> resetAndRefreshTable());

                JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                filters.setOpaque(false);
                filters.add(typeFilter);
                filters.add(statusFilter);
                toolbar.add(filters, BorderLayout.CENTER);

                JButton addButton = createToolbarButton(
                                "Add Member",
                                "person_add.png",
                                new Color(198, 156, 77),
                                new Color(24, 58, 55));
                addButton.addActionListener(event -> addMember());

                JButton refreshButton = createToolbarButton(
                                "Refresh",
                                "refresh.png",
                                new Color(24, 58, 55),
                                WHITE);
                refreshButton.addActionListener(event -> {
                        searchField.setText("");
                        typeFilter.setSelectedIndex(0);
                        statusFilter.setSelectedIndex(0);
                        resetAndRefreshTable();
                });

                JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                actionButtons.setOpaque(false);
                actionButtons.add(refreshButton);
                actionButtons.add(addButton);
                toolbar.add(actionButtons, BorderLayout.EAST);

                tablePanel.add(toolbar, BorderLayout.NORTH);

                String[] columns = {
                                "MEMBER ID",
                                "FULL NAME",
                                "MEMBER TYPE",
                                "CONTACT NUMBER",
                                "STATUS",
                                "ACTIVE LOANS"
                };

                tableModel = new DefaultTableModel(columns, 0) {

                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };

                memberTable = new JTable(tableModel);

                memberTable.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
                memberTable.setForeground(DARK);
                memberTable.setBackground(WHITE);
                memberTable.setRowHeight(42);
                memberTable.setShowVerticalLines(false);
                memberTable.setGridColor(new Color(235, 235, 230));
                memberTable.setSelectionBackground(
                                new Color(218, 234, 224));
                memberTable.setSelectionForeground(DARK);
                memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                RoundedComponents.enableCellCopy(memberTable);
                memberTable.setFillsViewportHeight(true);
                memberTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                memberTable.setDefaultRenderer(
                                Object.class,
                                new AlternatingTableCellRenderer(
                                                WHITE, new Color(249, 249, 246)));
                memberTable.getColumnModel().getColumn(0).setPreferredWidth(115);
                memberTable.getColumnModel().getColumn(1).setPreferredWidth(190);
                memberTable.getColumnModel().getColumn(2).setPreferredWidth(115);
                memberTable.getColumnModel().getColumn(3).setPreferredWidth(145);
                memberTable.getColumnModel().getColumn(4).setPreferredWidth(100);
                memberTable.getColumnModel().getColumn(5).setPreferredWidth(100);
                memberTable.getColumnModel().getColumn(4).setCellRenderer(
                                new StatusBadgeRenderer());

                JTableHeader header = memberTable.getTableHeader();
                header.setFont(new Font(FONT_FAMILY, Font.BOLD, 11));
                header.setForeground(MUTED);
                header.setBackground(new Color(248, 248, 244));
                header.setPreferredSize(new Dimension(0, 42));
                header.setReorderingAllowed(false);
                header.setToolTipText("Click a column heading to sort.");
                header.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent event) {
                                if (!SwingUtilities.isLeftMouseButton(event)) {
                                        return;
                                }
                                int viewColumn = header.columnAtPoint(event.getPoint());
                                if (viewColumn >= 0) {
                                        sortByColumn(
                                                        memberTable.convertColumnIndexToModel(viewColumn));
                                }
                        }
                });
                updateSortIndicator();

                memberTable.getSelectionModel().addListSelectionListener(event -> {
                        if (!event.getValueIsAdjusting()) {
                                updateSelectedMember();
                        }
                });

                JScrollPane scrollPane = new JScrollPane(memberTable);
                RoundedComponents.styleScrollPane(scrollPane);
                scrollPane.setBorder(
                                BorderFactory.createLineBorder(
                                                new Color(235, 235, 230)));
                scrollPane.getViewport().setBackground(WHITE);
                memberTableContentLayout = new CardLayout();
                memberTableContent = new JPanel(memberTableContentLayout);
                memberTableContent.setOpaque(false);
                memberTableContent.add(scrollPane, "TABLE");
                memberTableContent.add(
                                createMemberTableEmptyState(), "EMPTY");
                tablePanel.add(memberTableContent, BorderLayout.CENTER);

                JPanel footer = new JPanel(new BorderLayout(0, 10));
                footer.setOpaque(false);
                footer.add(createMemberDetailsPanel(), BorderLayout.NORTH);

                paginationPanel = new JPanel(
                                new FlowLayout(FlowLayout.LEFT, 6, 0));
                paginationPanel.setOpaque(false);
                previousPageButton = createButton(
                                "\u2039 Previous", null,
                                new Color(232, 233, 226), DARK);
                nextPageButton = createButton(
                                "Next \u203A", null,
                                new Color(232, 233, 226), DARK);
                pageLabel = new JLabel();
                pageLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 11));
                pageLabel.setForeground(MUTED);
                previousPageButton.setPreferredSize(new Dimension(92, 40));
                nextPageButton.setPreferredSize(new Dimension(76, 40));
                previousPageButton.addActionListener(event -> changePage(-1));
                nextPageButton.addActionListener(event -> changePage(1));
                paginationPanel.add(previousPageButton);
                paginationPanel.add(pageLabel);
                paginationPanel.add(nextPageButton);

                JPanel footerActions = new JPanel(
                                new FlowLayout(FlowLayout.RIGHT, 8, 0));
                footerActions.setOpaque(false);
                editButton = createButton("Edit Selected", "save.png", GREEN);
                historyButton = createButton(
                                "View History", "history.png", GOLD, DARK);
                archiveButton = createButton(
                                "Archive Selected",
                                "delete.png",
                                new Color(198, 91, 88), WHITE);
                editButton.setPreferredSize(new Dimension(160, 40));
                historyButton.setPreferredSize(new Dimension(160, 40));
                archiveButton.setPreferredSize(new Dimension(160, 40));

                editButton.addActionListener(event -> editMember());
                archiveButton.addActionListener(event -> deleteMember());
                historyButton.addActionListener(event -> viewMemberHistory());
                editButton.setEnabled(false);
                historyButton.setEnabled(false);
                archiveButton.setEnabled(false);
                footerActions.add(historyButton);
                footerActions.add(editButton);
                footerActions.add(archiveButton);

                footer.add(paginationPanel, BorderLayout.CENTER);
                footer.add(footerActions, BorderLayout.SOUTH);
                tablePanel.add(footer, BorderLayout.SOUTH);

                content.add(tablePanel, BorderLayout.CENTER);

                mainPanel.add(content, BorderLayout.CENTER);

                add(mainPanel, BorderLayout.CENTER);
        }

        private JPanel createMemberStatisticCard(
                        String title,
                        JLabel value,
                        String description,
                        Color accent,
                        String iconName) {
                JPanel card = RoundedComponents.createStatCard(
                                title,
                                value,
                                description,
                                accent,
                                loadIcon(iconName, accent),
                                12);
                applyFontFamily(card);
                return card;
        }

        private void applyFontFamily(Component component) {
                Font currentFont = component.getFont();
                if (currentFont != null) {
                        component.setFont(new Font(
                                        FONT_FAMILY,
                                        currentFont.getStyle(),
                                        currentFont.getSize()));
                }
                if (component instanceof Container) {
                        for (Component child : ((Container) component).getComponents()) {
                                applyFontFamily(child);
                        }
                }
        }

        private JPanel createMemberDetailsPanel() {
                RoundedPanel panel = new RoundedPanel(12, new Color(248, 248, 244));
                panel.setLayout(new BorderLayout(0, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

                selectedMemberHeading = new JLabel("Member details");
                selectedMemberHeading.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
                selectedMemberHeading.setForeground(DARK);

                memberDetailsLayout = new CardLayout();
                memberDetailsContent = new JPanel(memberDetailsLayout);
                memberDetailsContent.setOpaque(false);
                memberDetailsContent.add(
                                createMemberEmptyState(
                                                "groups.png",
                                                "Select a member to view their account details."),
                                "EMPTY");

                JPanel details = new JPanel(new GridLayout(1, 5, 8, 0));
                details.setOpaque(false);
                maxLoansValue = new JLabel("-");
                loanPeriodValue = new JLabel("-");
                finesValue = new JLabel("-");
                registrationDateValue = new JLabel("-");
                historyValue = new JLabel("-");
                details.add(createMemberDetailItem("MAXIMUM LOANS", maxLoansValue));
                details.add(createMemberDetailItem("LOAN PERIOD", loanPeriodValue));
                details.add(createMemberDetailItem("OUTSTANDING FINES", finesValue));
                details.add(createMemberDetailItem(
                                "REGISTRATION DATE", registrationDateValue));
                details.add(createMemberDetailItem("BORROWING HISTORY", historyValue));
                memberDetailsContent.add(details, "DETAILS");

                panel.add(selectedMemberHeading, BorderLayout.NORTH);
                panel.add(memberDetailsContent, BorderLayout.CENTER);
                return panel;
        }

        private JPanel createMemberDetailItem(String title, JLabel value) {
                RoundedPanel detail = new RoundedPanel(10, WHITE);
                detail.setLayout(new GridLayout(2, 1, 0, 4));
                detail.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 9));
                titleLabel.setForeground(MUTED);
                value.setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
                value.setForeground(DARK);

                detail.add(titleLabel);
                detail.add(value);
                return detail;
        }

        private JPanel createMemberTableEmptyState() {
                JPanel emptyState = new JPanel(new BorderLayout());
                emptyState.setOpaque(false);

                JPanel content = new JPanel(
                                new FlowLayout(FlowLayout.CENTER, 10, 6));
                content.setOpaque(false);
                content.add(new JLabel(loadIcon("groups.png", MUTED)));
                memberTableEmptyMessage = new JLabel();
                memberTableEmptyMessage.setFont(
                                new Font(FONT_FAMILY, Font.PLAIN, 13));
                memberTableEmptyMessage.setForeground(MUTED);
                content.add(memberTableEmptyMessage);
                emptyState.add(content, BorderLayout.CENTER);
                return emptyState;
        }

        private JPanel createMemberEmptyState(String iconName, String message) {
                JPanel emptyState = new JPanel(new BorderLayout());
                emptyState.setOpaque(false);

                JPanel content = new JPanel(
                                new FlowLayout(FlowLayout.CENTER, 10, 6));
                content.setOpaque(false);
                JLabel icon = new JLabel(loadIcon(iconName, MUTED));
                JLabel messageLabel = new JLabel(message);
                messageLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
                messageLabel.setForeground(MUTED);
                content.add(icon);
                content.add(messageLabel);
                emptyState.add(content, BorderLayout.CENTER);
                return emptyState;
        }

        private void updateSelectedMember() {
                int selectedRow = memberTable.getSelectedRow();
                LibraryMember member = selectedRow < 0
                                || selectedRow >= visibleMembers.size()
                                                ? null
                                                : visibleMembers.get(selectedRow);
                boolean hasSelection = member != null;

                editButton.setEnabled(hasSelection);
                historyButton.setEnabled(hasSelection);
                archiveButton.setEnabled(hasSelection);
                memberDetailsLayout.show(
                                memberDetailsContent, hasSelection ? "DETAILS" : "EMPTY");
                if (!hasSelection) {
                        selectedMemberHeading.setText("Member details");
                        maxLoansValue.setText("-");
                        loanPeriodValue.setText("-");
                        finesValue.setText("-");
                        registrationDateValue.setText("-");
                        historyValue.setText("-");
                        return;
                }

                selectedMemberHeading.setText(
                                "MEMBER DETAILS  \u00B7  " + member.getFullname());
                maxLoansValue.setText(String.valueOf(member.getMaxLoans()));
                loanPeriodValue.setText(member.getLoanPeriodDays() + " days");
                finesValue.setText(String.format(
                                Locale.ROOT, "\u20B1%.2f",
                                library.getMemberOutstandingFines(member.getMemberId())));
                LocalDate registrationDate = member.getRegistrationDate();
                registrationDateValue.setText(registrationDate == null
                                ? "Not recorded"
                                : registrationDate.format(
                                                DateTimeFormatter.ofPattern("MMM d, yyyy")));
                historyValue.setText(
                                library.getLoanHistoryForMember(member.getMemberId()).size()
                                                + " transactions");
        }

        private void changePage(int offset) {
                int pageCount = Math.max(
                                1, (filteredMembers.size() + PAGE_SIZE - 1) / PAGE_SIZE);
                int nextPage = currentPage + offset;
                if (nextPage < 0 || nextPage >= pageCount) {
                        return;
                }
                currentPage = nextPage;
                refreshPageRows();
        }

        private void sortByColumn(int column) {
                if (sortColumn == column) {
                        sortAscending = !sortAscending;
                } else {
                        sortColumn = column;
                        sortAscending = true;
                }
                currentPage = 0;
                // Sort before paging so the order stays consistent across page boundaries.
                sortFilteredMembers();
                refreshPageRows();
        }

        private void updateSortIndicator() {
                String[] titles = {
                                "MEMBER ID", "FULL NAME", "MEMBER TYPE",
                                "CONTACT NUMBER", "STATUS", "ACTIVE LOANS"
                };
                for (int column = 0; column < titles.length; column++) {
                        String title = titles[column];
                        if (column == sortColumn) {
                                title += sortAscending ? " \u25B2" : " \u25BC";
                        }
                        memberTable.getColumnModel().getColumn(column)
                                        .setHeaderValue(title);
                }
                memberTable.getTableHeader().repaint();
        }

        private void sortFilteredMembers() {
                Comparator<LibraryMember> comparator;
                switch (sortColumn) {
                        case 0:
                                comparator = Comparator.comparing(
                                                LibraryMember::getMemberId,
                                                String.CASE_INSENSITIVE_ORDER);
                                break;
                        case 1:
                                comparator = Comparator.comparing(
                                                LibraryMember::getFullname,
                                                String.CASE_INSENSITIVE_ORDER);
                                break;
                        case 2:
                                comparator = Comparator.comparing(
                                                this::getMemberType,
                                                String.CASE_INSENSITIVE_ORDER);
                                break;
                        case 3:
                                comparator = Comparator.comparing(
                                                LibraryMember::getContactNumber,
                                                String.CASE_INSENSITIVE_ORDER);
                                break;
                        case 4:
                                comparator = Comparator.comparing(
                                                member -> member.getStatus().name());
                                break;
                        case 5:
                                comparator = Comparator.comparingInt(
                                                member -> activeLoanCounts.getOrDefault(
                                                                member.getMemberId().toLowerCase(Locale.ROOT), 0));
                                break;
                        default:
                                throw new IllegalArgumentException(
                                                "Unknown member table column: " + sortColumn);
                }
                if (!sortAscending) {
                        comparator = comparator.reversed();
                }
                filteredMembers.sort(comparator.thenComparing(
                                LibraryMember::getMemberId,
                                String.CASE_INSENSITIVE_ORDER));
        }

        private String getMemberType(LibraryMember member) {
                if (member instanceof StudentMember) {
                        return "Student";
                }
                if (member instanceof FacultyMember) {
                        return "Faculty";
                }
                return "Other";
        }

        private JButton createButton(
                        String text,
                        String iconName,
                        Color background) {
                return createButton(text, iconName, background, WHITE);
        }

        private JButton createButton(
                        String text,
                        String iconName,
                        Color background,
                        Color foreground) {
                JButton button = new RoundedComponents.RoundedButton(text, 10);
                button.setFont(new Font(FONT_FAMILY, Font.BOLD, 11));
                button.setMargin(new Insets(0, 4, 0, 4));
                button.setBackground(background);
                button.setForeground(foreground);
                if (iconName != null) {
                        button.setIcon(loadIcon(iconName, foreground));
                        button.setIconTextGap(6);
                }
                button.setPreferredSize(new Dimension(130, 40));
                button.setCursor(
                                new java.awt.Cursor(
                                                java.awt.Cursor.HAND_CURSOR));
                return button;
        }

        private JButton createToolbarButton(
                        String text,
                        String iconName,
                        Color background,
                        Color foreground) {
                JButton button = new RoundedComponents.RoundedButton(text, 14);
                button.setFont(new Font("Arial", Font.BOLD, 12));
                button.setMargin(new Insets(0, 4, 0, 4));
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
                java.awt.image.BufferedImage tinted = new java.awt.image.BufferedImage(
                                18, 18,
                                java.awt.image.BufferedImage.TYPE_INT_ARGB);
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

        // Refresh table
        public void refreshTable() {

                String keyword = searchField.getText()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                activeLoanCounts.clear();
                for (Loan loan : library.getLoans()) {
                        if (!loan.isReturned()) {
                                String memberId = loan.getMemberId().toLowerCase(Locale.ROOT);
                                activeLoanCounts.put(
                                                memberId,
                                                activeLoanCounts.getOrDefault(memberId, 0) + 1);
                        }
                }

                filteredMembers.clear();
                for (LibraryMember member : library.getMembers()) {
                        if (matchesMemberFilters(member, keyword)) {
                                filteredMembers.add(member);
                        }
                }

                sortFilteredMembers();
                int pageCount = Math.max(
                                1, (filteredMembers.size() + PAGE_SIZE - 1) / PAGE_SIZE);
                currentPage = Math.min(currentPage, pageCount - 1);
                refreshPageRows();
                updateStatistics();
        }

        private void refreshPageRows() {
                tableModel.setRowCount(0);
                visibleMembers.clear();
                int start = currentPage * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, filteredMembers.size());
                for (int index = start; index < end; index++) {
                        LibraryMember member = filteredMembers.get(index);
                        visibleMembers.add(member);
                        addMemberRow(member);
                }

                memberTableEmptyMessage.setText(
                                library.getMembers().isEmpty()
                                                ? "No members have been registered yet."
                                                : "No members match your current filters.");
                memberTableContentLayout.show(
                                memberTableContent,
                                filteredMembers.isEmpty() ? "EMPTY" : "TABLE");
                updateSortIndicator();
                int pageCount = Math.max(
                                1, (filteredMembers.size() + PAGE_SIZE - 1) / PAGE_SIZE);
                pageLabel.setText("Page " + (currentPage + 1) + " of " + pageCount);
                paginationPanel.setVisible(filteredMembers.size() > PAGE_SIZE);
                previousPageButton.setEnabled(currentPage > 0);
                nextPageButton.setEnabled(currentPage + 1 < pageCount);
                updateSelectedMember();
        }

        private void resetAndRefreshTable() {
                currentPage = 0;
                refreshTable();
        }

        private void addMemberRow(LibraryMember member) {
                tableModel.addRow(new Object[] {
                                member.getMemberId(),
                                member.getFullname(),
                                getMemberType(member),
                                member.getContactNumber(),
                                displayStatus(member.getStatus()),
                                activeLoanCounts.getOrDefault(
                                                member.getMemberId().toLowerCase(Locale.ROOT), 0)
                });
        }

        private void updateStatistics() {

                int students = 0;
                int faculty = 0;

                for (LibraryMember member : library.getMembers()) {

                        if (member instanceof StudentMember) {
                                students++;
                        } else if (member instanceof FacultyMember) {
                                faculty++;
                        }
                }

                totalMembersValue.setText(
                                String.valueOf(students + faculty));

                studentsValue.setText(String.valueOf(students));
                facultyValue.setText(String.valueOf(faculty));
        }

        // Search members
        private void searchMembers() {
                resetAndRefreshTable();
        }

        private boolean matchesSearch(
                        LibraryMember member,
                        String keyword) {

                return member.getMemberId().toLowerCase(Locale.ROOT).contains(keyword)
                                || member.getFullname().toLowerCase(Locale.ROOT).contains(keyword);
        }

        private boolean matchesMemberFilters(
                        LibraryMember member,
                        String keyword) {
                if (!matchesSearch(member, keyword)) {
                        return false;
                }

                String selectedType = typeFilter.getSelectedItem().toString();
                if ("Student".equals(selectedType)
                                && !(member instanceof StudentMember)) {
                        return false;
                }
                if ("Faculty".equals(selectedType)
                                && !(member instanceof FacultyMember)) {
                        return false;
                }

                String selectedStatus = statusFilter.getSelectedItem().toString();
                return "All statuses".equals(selectedStatus)
                                || displayStatus(member.getStatus()).equals(selectedStatus);
        }

        private String displayStatus(MemberStatus status) {
                String value = status.name().toLowerCase(Locale.ROOT);
                return Character.toUpperCase(value.charAt(0)) + value.substring(1);
        }

        // Add member
        private void addMember() {

                JTextField idField = new JTextField();
                JTextField nameField = new JTextField();
                JTextField contactField = new JTextField();

                JComboBox<String> typeBox = new JComboBox<>(
                                new String[] { "Student", "Faculty" });
                JComboBox<String> statusBox = new JComboBox<>(
                                new String[] { "Active", "Inactive", "Suspended" });

                JPanel form = createMemberForm(
                                idField,
                                nameField,
                                contactField,
                                typeBox,
                                statusBox);

                if (!showMemberFormDialog(
                                "Register New Member",
                                "Add a student or faculty member to the library.",
                                "Register Member",
                                form)) {
                        return;
                }

                try {

                        String id = idField.getText();
                        String name = nameField.getText();
                        String contact = contactField.getText();

                        LibraryMember member;

                        if ("Student".equals(typeBox.getSelectedItem())) {

                                member = new StudentMember(
                                                id, name, contact);

                        } else {

                                member = new FacultyMember(
                                                id, name, contact);
                        }

                        member.setStatus(parseStatus(statusBox.getSelectedItem().toString()));
                        library.addMember(member);

                        refreshTable();

                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Member added",
                                        "Member successfully registered.",
                                        RoundedComponents.DialogType.SUCCESS);

                } catch (IllegalArgumentException | IllegalStateException exception) {

                        showError(exception.getMessage());
                }
        }

        // Edit member
        private void editMember() {

                int selectedRow = memberTable.getSelectedRow();

                if (selectedRow == -1) {
                        showError("Please select a member to edit.");
                        return;
                }

                int modelRow = memberTable.convertRowIndexToModel(
                                selectedRow);

                String memberId = tableModel.getValueAt(
                                modelRow, 0).toString();

                LibraryMember member = library.findMemberById(memberId);

                if (member == null) {
                        showError("The selected member could not be found.");
                        refreshTable();
                        return;
                }

                JTextField idField = new JTextField(member.getMemberId());
                JTextField nameField = new JTextField(member.getFullname());
                JTextField contactField = new JTextField(
                                member.getContactNumber());

                JComboBox<String> typeBox = new JComboBox<>(
                                new String[] { "Student", "Faculty" });
                JComboBox<String> statusBox = new JComboBox<>(
                                new String[] { "Active", "Inactive", "Suspended", "Archived" });

                if (member instanceof FacultyMember) {
                        typeBox.setSelectedItem("Faculty");
                } else {
                        typeBox.setSelectedItem("Student");
                }
                statusBox.setSelectedItem(displayStatus(member.getStatus()));

                // Do not change the member ID.
                idField.setEditable(false);

                // Prevent changing member type to avoid losing borrowing policy.
                typeBox.setEnabled(false);

                JPanel form = createMemberForm(
                                idField,
                                nameField,
                                contactField,
                                typeBox,
                                statusBox);

                if (!showMemberFormDialog(
                                "Edit Member",
                                "Update the member's contact information.",
                                "Save Changes",
                                form)) {
                        return;
                }

                try {

                        library.updateMember(
                                        memberId,
                                        nameField.getText(),
                                        contactField.getText(),
                                        parseStatus(statusBox.getSelectedItem().toString()));

                        refreshTable();

                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Member updated",
                                        "Member information updated successfully.",
                                        RoundedComponents.DialogType.SUCCESS);

                } catch (IllegalArgumentException | IllegalStateException exception) {

                        showError(exception.getMessage());
                }
        }

        // Delete member
        private void deleteMember() {

                int selectedRow = memberTable.getSelectedRow();

                if (selectedRow == -1) {
                        showError("Please select a member to delete.");
                        return;
                }

                int modelRow = memberTable.convertRowIndexToModel(
                                selectedRow);

                String memberId = tableModel.getValueAt(
                                modelRow, 0).toString();

                String memberName = tableModel.getValueAt(
                                modelRow, 1).toString();

                boolean confirmation = RoundedComponents.showWarningDialog(
                                this,
                                "Archive member?",
                                "Archive \"" + memberName
                                                + "\"? Their borrowing history will be retained.",
                                "Archive Member");

                if (!confirmation) {
                        return;
                }

                try {

                        library.archiveMember(memberId);

                        refreshTable();

                        RoundedComponents.showMessageDialog(
                                        this,
                                        "Member archived",
                                        "Member archived; their history has been retained.",
                                        RoundedComponents.DialogType.SUCCESS);

                } catch (IllegalArgumentException | IllegalStateException exception) {

                        showError(exception.getMessage());
                }
        }

        private MemberStatus parseStatus(String value) {
                return MemberStatus.valueOf(value.toUpperCase(Locale.ROOT));
        }

        private void viewMemberHistory() {
                int selectedRow = memberTable.getSelectedRow();
                if (selectedRow < 0) {
                        showError("Please select a member to view their history.");
                        return;
                }

                int modelRow = memberTable.convertRowIndexToModel(selectedRow);
                String memberId = tableModel.getValueAt(modelRow, 0).toString();
                LibraryMember member = library.findMemberById(memberId);
                if (member == null) {
                        showError("The selected member could not be found.");
                        refreshTable();
                        return;
                }

                String[] columns = {
                                "LOAN ID", "BOOK ISBN", "BORROWED", "DUE",
                                "RETURNED", "STATUS", "FINE BALANCE"
                };
                DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };
                for (models.Loan loan : library.getLoanHistoryForMember(memberId)) {
                        String status = loan.isReturned()
                                        ? "Returned"
                                        : loan.isOverdue() ? "Overdue" : "Active";
                        historyModel.addRow(new Object[] {
                                        loan.getLoanId(),
                                        loan.getBookIsbn(),
                                        loan.getBorrowDate(),
                                        loan.getDueDate(),
                                        loan.getReturnDate() == null ? "-" : loan.getReturnDate(),
                                        status,
                                        String.format(
                                                        Locale.ROOT, "₱%.2f",
                                                        library.getOutstandingFine(loan.getLoanId()))
                        });
                }

                JTable historyTable = new JTable(historyModel);
                RoundedComponents.enableCellCopy(historyTable);
                historyTable.setAutoCreateRowSorter(true);
                RoundedComponents.configureTableSorter(historyTable);
                historyTable.setFillsViewportHeight(true);
                historyTable.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
                historyTable.setForeground(DARK);
                historyTable.setRowHeight(32);
                historyTable.setShowVerticalLines(false);
                historyTable.setSelectionBackground(new Color(226, 238, 230));
                historyTable.setSelectionForeground(DARK);
                historyTable.setDefaultRenderer(
                                Object.class,
                                new AlternatingTableCellRenderer(
                                                WHITE, new Color(249, 249, 246)));
                historyTable.getColumnModel().getColumn(5).setCellRenderer(
                                new StatusBadgeRenderer());
                JTableHeader historyHeader = historyTable.getTableHeader();
                historyHeader.setFont(new Font(FONT_FAMILY, Font.BOLD, 10));
                historyHeader.setForeground(MUTED);
                historyHeader.setBackground(new Color(248, 248, 244));
                historyHeader.setPreferredSize(new Dimension(0, 36));
                historyHeader.setReorderingAllowed(false);
                JScrollPane historyScroll = new JScrollPane(historyTable);
                historyScroll.setPreferredSize(new Dimension(760, 260));
                RoundedComponents.styleScrollPane(historyScroll);
                historyScroll.setBorder(
                                BorderFactory.createLineBorder(
                                                new Color(235, 235, 230)));

                JPanel historyPanel = new JPanel(new BorderLayout(0, 10));
                historyPanel.setOpaque(false);
                JLabel outstandingFines = new JLabel(
                                "Outstanding fines: "
                                                + String.format(
                                                                Locale.ROOT, "₱%.2f",
                                                                library.getMemberOutstandingFines(memberId)));
                outstandingFines.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
                outstandingFines.setForeground(GREEN);
                historyPanel.add(outstandingFines, BorderLayout.NORTH);
                historyPanel.add(historyScroll, BorderLayout.CENTER);

                RoundedComponents.showContentDialog(
                                this,
                                "Borrowing History",
                                member.getFullname(),
                                historyPanel);
        }

        private final class AlternatingTableCellRenderer
                        extends DefaultTableCellRenderer {

                private static final long serialVersionUID = 1L;
                private final Color evenRow;
                private final Color oddRow;

                private AlternatingTableCellRenderer(Color evenRow, Color oddRow) {
                        this.evenRow = evenRow;
                        this.oddRow = oddRow;
                        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }

                @Override
                public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column);
                        label.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
                        label.setForeground(DARK);
                        label.setBackground(isSelected
                                        ? table.getSelectionBackground()
                                        : row % 2 == 0 ? evenRow : oddRow);
                        return label;
                }
        }

        final class StatusBadgeRenderer
                        extends JPanel implements TableCellRenderer {

                private static final long serialVersionUID = 1L;
                private final JLabel label = new JLabel("", JLabel.CENTER);
                private Color badgeBackground = new Color(231, 244, 237);

                private StatusBadgeRenderer() {
                        setLayout(new BorderLayout());
                        setOpaque(true);
                        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                        label.setOpaque(false);
                        label.setFont(new Font(FONT_FAMILY, Font.BOLD, 10));
                        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                        add(label, BorderLayout.CENTER);
                }

                @Override
                public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column) {
                        String status = value == null ? "" : value.toString();
                        label.setText(status);
                        setBackground(isSelected
                                        ? table.getSelectionBackground()
                                        : row % 2 == 0
                                                        ? Color.WHITE
                                                        : new Color(249, 249, 246));

                        if ("Active".equalsIgnoreCase(status)) {
                                badgeBackground = new Color(231, 244, 237);
                                label.setForeground(new Color(45, 112, 82));
                        } else if ("Overdue".equalsIgnoreCase(status)
                                        || "Suspended".equalsIgnoreCase(status)) {
                                badgeBackground = new Color(252, 235, 233);
                                label.setForeground(new Color(169, 62, 58));
                        } else if ("Returned".equalsIgnoreCase(status)) {
                                badgeBackground = new Color(231, 244, 237);
                                label.setForeground(new Color(45, 112, 82));
                        } else if ("Archived".equalsIgnoreCase(status)
                                        || "Inactive".equalsIgnoreCase(status)) {
                                badgeBackground = new Color(239, 240, 235);
                                label.setForeground(new Color(98, 109, 105));
                        } else {
                                badgeBackground = new Color(248, 243, 229);
                                label.setForeground(new Color(142, 105, 40));
                        }
                        return this;
                }

                @Override
                protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        Graphics2D graphics2D = (Graphics2D) graphics.create();
                        graphics2D.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                        int badgeWidth = Math.min(
                                        getWidth() - 8,
                                        graphics2D.getFontMetrics(label.getFont())
                                                        .stringWidth(label.getText()) + 18);
                        int badgeHeight = Math.min(24, getHeight() - 6);
                        int x = (getWidth() - badgeWidth) / 2;
                        int y = (getHeight() - badgeHeight) / 2;
                        graphics2D.setColor(badgeBackground);
                        graphics2D.fillRoundRect(
                                        x, y, badgeWidth, badgeHeight, 12, 12);
                        graphics2D.dispose();
                }
        }

        private JPanel createMemberForm(
                        JTextField id,
                        JTextField name,
                        JTextField contact,
                        JComboBox<String> type,
                        JComboBox<String> status) {

                JPanel form = new JPanel(
                                new GridLayout(5, 2, 12, 12));

                form.setPreferredSize(
                                new Dimension(390, 225));

                form.setOpaque(false);

                JLabel idLabel = createFormLabel("Member ID");
                JLabel nameLabel = createFormLabel("Full Name");
                JLabel contactLabel = createFormLabel("Contact Number");
                JLabel typeLabel = createFormLabel("Member Type");
                JLabel statusLabel = createFormLabel("Status");

                styleFormField(id);
                styleFormField(name);
                styleFormField(contact);

                styleMemberTypeBox(type);
                styleMemberTypeBox(status);

                form.add(idLabel);
                form.add(id);

                form.add(nameLabel);
                form.add(name);

                form.add(contactLabel);
                form.add(contact);

                form.add(typeLabel);
                form.add(type);
                form.add(statusLabel);
                form.add(status);

                return form;
        }

        private JLabel createFormLabel(String text) {

                JLabel label = new JLabel(text);
                label.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
                label.setForeground(DARK);
                return label;
        }

        private void styleFormField(JTextField field) {

                field.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
                field.setBackground(field.isEditable() ? WHITE : BACKGROUND);
                field.setPreferredSize(new Dimension(0, 34));
                field.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(225, 225, 218)),
                                                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        }

        private void styleMemberTypeBox(JComboBox<String> type) {

                type.setUI(new BasicComboBoxUI() {

                        @Override
                        protected JButton createArrowButton() {
                                JButton arrow = new JButton("\u25BE");
                                arrow.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
                                arrow.setForeground(type.isEnabled() ? GREEN : MUTED);
                                arrow.setBackground(type.isEnabled() ? WHITE : BACKGROUND);
                                arrow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                                arrow.setContentAreaFilled(false);
                                arrow.setFocusPainted(false);
                                arrow.setFocusable(false);
                                return arrow;
                        }
                });

                type.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
                type.setBackground(type.isEnabled() ? WHITE : BACKGROUND);
                type.setOpaque(true);
                type.setPreferredSize(new Dimension(0, 34));
                type.setMaximumRowCount(2);
                type.setToolTipText("Choose a value.");
                type.setCursor(
                                new java.awt.Cursor(
                                                type.isEnabled()
                                                                ? java.awt.Cursor.HAND_CURSOR
                                                                : java.awt.Cursor.DEFAULT_CURSOR));
                type.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                new Color(225, 225, 218)),
                                                BorderFactory.createEmptyBorder(0, 10, 0, 4)));
                type.setRenderer(new DefaultListCellRenderer() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public java.awt.Component getListCellRendererComponent(
                                        JList<?> list,
                                        Object value,
                                        int index,
                                        boolean isSelected,
                                        boolean cellHasFocus) {

                                JLabel label = (JLabel) super.getListCellRendererComponent(
                                                list, value, index,
                                                isSelected, cellHasFocus);
                                label.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
                                label.setBorder(
                                                BorderFactory.createEmptyBorder(7, 10, 7, 10));

                                if (!type.isEnabled()) {
                                        label.setBackground(BACKGROUND);
                                        label.setForeground(MUTED);
                                } else if (isSelected) {
                                        label.setBackground(new Color(232, 240, 235));
                                        label.setForeground(GREEN);
                                } else {
                                        label.setBackground(WHITE);
                                        label.setForeground(DARK);
                                }

                                return label;
                        }
                });
        }

        private boolean showMemberFormDialog(
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
                heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 21));
                titleLabel.setForeground(DARK);

                JLabel subtitleLabel = new JLabel(subtitle);
                subtitleLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
                subtitleLabel.setForeground(MUTED);

                heading.add(titleLabel);
                heading.add(Box.createVerticalStrut(6));
                heading.add(subtitleLabel);

                RoundedComponents.RoundedPanel formCard = new RoundedComponents.RoundedPanel(18, WHITE);
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

                JButton button = new RoundedComponents.RoundedButton(text, 10);
                button.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
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
