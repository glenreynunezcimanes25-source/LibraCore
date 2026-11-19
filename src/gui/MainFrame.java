package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import models.Loan;
import services.LibraryFileManager;
import services.LibraryService;

public final class MainFrame {

    private final JFrame frame;
    private final LibraryService library;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private JPanel sidebar;

    private JButton activeButton;
    private String activeScreen = "DASHBOARD";
    private DashboardPanel dashboardPanel;
    private BooksPanel booksPanel;
    private MembersPanel membersPanel;
    private BorrowReturnPanel borrowReturnPanel;
    private ReportsPanel reportsPanel;
    private JLabel sidebarMemberCount;
    private JLabel sidebarActiveLoanCount;
    private JLabel sidebarOverdueCount;

    // Application colors
    private final Color SIDEBAR_COLOR = new Color(24, 58, 55);
    private final Color SIDEBAR_HOVER = new Color(36, 75, 69);
    private final Color PRIMARY_COLOR = new Color(198, 156, 77);

    private final Color BACKGROUND_COLOR = new Color(245, 244, 239);
    private final Color SECONDARY_TEXT = new Color(148, 163, 158);

    public static void launch() {
        try {
            new MainFrame();
        } catch (IllegalStateException exception) {
            boolean dataReadFailure = exception.getMessage() != null
                    && exception.getMessage().contains(
                            "Unable to read library data from");
            int recover = dataReadFailure
                    ? JOptionPane.showConfirmDialog(
                            null,
                            exception.getMessage()
                                    + "\n\nTry restoring the last "
                                    + "known-good library snapshot? "
                                    + "The current file will be preserved.",
                            "Unable to start Libracore",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE)
                    : JOptionPane.NO_OPTION;
            if (recover == JOptionPane.YES_OPTION) {
                try {
                    new LibraryFileManager().restoreBackup();
                    new MainFrame();
                } catch (IOException | IllegalStateException recoveryError) {
                    JOptionPane.showMessageDialog(
                            null,
                            recoveryError.getMessage(),
                            "Recovery failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        exception.getMessage(),
                        "Unable to start Libracore",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public MainFrame() {

        library = new LibraryService();

        frame = new JFrame("Libracore | Library Management System");
        frame.setSize(1250, 780);
        frame.setMinimumSize(new Dimension(1000, 650));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setLayout(new BorderLayout());

        createSidebar();
        createMainArea();

        frame.setVisible(true);
    }

    // Creates the left navigation sidebar
    private void createSidebar() {

        sidebar = new JPanel(new BorderLayout(0, 18));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setMinimumSize(new Dimension(230, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 18, 22, 18));

        // Application branding
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logo = new JLabel(
                "LIBRACORE",
                loadIcon("library_books.png"),
                SwingConstants.LEFT);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        logo.setIconTextGap(9);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("LIBRARY MANAGEMENT SYSTEM");
        tagline.setForeground(SECONDARY_TEXT);
        tagline.setFont(new Font("Arial", Font.PLAIN, 9));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(6));
        brandPanel.add(tagline);

        topPanel.add(brandPanel);
        topPanel.add(Box.createVerticalStrut(26));

        // Navigation section title
        JLabel menuLabel = new JLabel("NAVIGATION");
        menuLabel.setForeground(SECONDARY_TEXT);
        menuLabel.setFont(new Font("Arial", Font.BOLD, 10));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        topPanel.add(menuLabel);
        topPanel.add(Box.createVerticalStrut(10));

        sidebar.add(topPanel, BorderLayout.NORTH);

        // Navigation buttons
        JPanel navigationPanel = new JPanel();
        navigationPanel.setOpaque(false);
        navigationPanel.setLayout(
                new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));

        addNavigationButton(navigationPanel, "Dashboard", "DASHBOARD");
        addNavigationButton(navigationPanel, "Books", "BOOKS");
        addNavigationButton(navigationPanel, "Members", "MEMBERS");
        addNavigationButton(navigationPanel, "Borrow / Return", "BORROW");
        addNavigationButton(navigationPanel, "Reports", "REPORTS");

        JPanel navigationSection = new JPanel(new BorderLayout(0, 18));
        navigationSection.setOpaque(false);
        navigationSection.add(navigationPanel, BorderLayout.NORTH);
        navigationSection.add(createLibraryAtGlancePanel(), BorderLayout.SOUTH);
        sidebar.add(navigationSection, BorderLayout.CENTER);

        // Bottom application information
        JLabel versionLabel = new JLabel("LIBRACORE  |  VERSION 2.4.0");
        versionLabel.setForeground(SECONDARY_TEXT);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(versionLabel, BorderLayout.SOUTH);

        frame.add(sidebar, BorderLayout.WEST);
        refreshSidebarSummary();
    }

    private JPanel createLibraryAtGlancePanel() {
        RoundedComponents.RoundedPanel panel =
                new RoundedComponents.RoundedPanel(14, SIDEBAR_HOVER);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel heading = new JLabel("LIBRARY AT A GLANCE");
        heading.setFont(new Font("Arial", Font.BOLD, 9));
        heading.setForeground(SECONDARY_TEXT);

        JPanel statistics = new JPanel(new java.awt.GridLayout(3, 1, 0, 10));
        statistics.setOpaque(false);
        sidebarMemberCount = createSidebarCountLabel();
        sidebarActiveLoanCount = createSidebarCountLabel();
        sidebarOverdueCount = createSidebarCountLabel();
        statistics.add(createSidebarStatRow("Members", sidebarMemberCount));
        statistics.add(createSidebarStatRow(
                "Active loans", sidebarActiveLoanCount));
        statistics.add(createSidebarStatRow("Overdue", sidebarOverdueCount));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(statistics, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSidebarStatRow(String title, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(new Color(203, 213, 225));
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.CENTER);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JLabel createSidebarCountLabel() {
        JLabel count = new JLabel("0");
        count.setFont(new Font("Arial", Font.BOLD, 12));
        count.setForeground(Color.WHITE);
        return count;
    }

    private void refreshSidebarSummary() {
        int activeLoans = 0;
        for (Loan loan : library.getLoans()) {
            if (!loan.isReturned()) {
                activeLoans++;
            }
        }

        sidebarMemberCount.setText(
                String.valueOf(library.getMembers().size()));
        sidebarActiveLoanCount.setText(String.valueOf(activeLoans));
        sidebarOverdueCount.setText(
                String.valueOf(library.getOverdueLoans().size()));
    }

    // Creates a navigation button
    private void addNavigationButton(
            JPanel navigationPanel,
            String text,
            String screenName) {

        JButton button = new RoundedComponents.RoundedButton(text);

        String iconFile = "";

        switch (screenName) {
            case "DASHBOARD":
                iconFile = "dashboard.png";
                break;
            case "BOOKS":
                iconFile = "library_books.png";
                break;
            case "MEMBERS":
                iconFile = "groups.png";
                break;
            case "BORROW":
                iconFile = "sync.png";
                break;
            case "REPORTS":
                iconFile = "chart.png";
                break;
        }

        button.setIcon(loadIcon(iconFile));

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(new Color(203, 213, 225));
        button.setBackground(SIDEBAR_COLOR);

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(12);

        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setPreferredSize(new Dimension(210, 50));
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 50));
        button.setMargin(new Insets(0, 14, 0, 8));
        button.setCursor(new java.awt.Cursor(
                java.awt.Cursor.HAND_CURSOR));

        button.addActionListener(event -> {
            activeScreen = screenName;
            cardLayout.show(contentPanel, screenName);
            refreshScreen(screenName);
            updateActiveButton(button);
        });

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent event) {
                if (button != activeButton) {
                    button.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                if (button != activeButton) {
                    button.setBackground(SIDEBAR_COLOR);
                }
            }
        });

        navigationPanel.add(button);
        navigationPanel.add(Box.createVerticalStrut(8));

        if (screenName.equals("DASHBOARD")) {
            activeButton = button;
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(new Color(24, 58, 55));
        }
    } // Changes the appearance of the selected navigation button

    private void updateActiveButton(JButton selectedButton) {

        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_COLOR);
            activeButton.setForeground(new Color(203, 213, 225));
        }

        activeButton = selectedButton;
        selectedButton.setBackground(PRIMARY_COLOR);
        selectedButton.setForeground(new Color(24, 58, 55));
    }

    // Creates the main application area
    private void createMainArea() {

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(BACKGROUND_COLOR);

        // Screen container
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Application screens
        dashboardPanel = new DashboardPanel(library);
        booksPanel = new BooksPanel(library);
        membersPanel = new MembersPanel(library);
        borrowReturnPanel = new BorrowReturnPanel(library);
        reportsPanel = new ReportsPanel(library);

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(booksPanel, "BOOKS");
        contentPanel.add(membersPanel, "MEMBERS");
        contentPanel.add(borrowReturnPanel, "BORROW");
        contentPanel.add(reportsPanel, "REPORTS");

        mainArea.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainArea, BorderLayout.CENTER);
        library.addLibraryDataListener(event -> {
            try {
                refreshSidebarSummary();
                refreshScreen(activeScreen);
            } catch (IllegalArgumentException | IllegalStateException exception) {
                RoundedComponents.showMessageDialog(
                        frame,
                        "Library data saved",
                        "The change was saved, but the current screen could "
                                + "not be refreshed: " + exception.getMessage(),
                        RoundedComponents.DialogType.WARNING);
            }
        });
    }

    private void refreshScreen(String screenName) {
        switch (screenName) {
            case "DASHBOARD":
                dashboardPanel.refreshDashboard();
                break;
            case "BOOKS":
                booksPanel.refreshTable();
                break;
            case "MEMBERS":
                membersPanel.refreshTable();
                break;
            case "BORROW":
                borrowReturnPanel.refreshTable();
                break;
            case "REPORTS":
                reportsPanel.refreshReports();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown screen: " + screenName);
        }
    }

    private ImageIcon loadIcon(String fileName) {

        try {

            BufferedImage original = ImageIO.read(
                    new File("resources/icons/" + fileName));
            if (original == null) {
                System.out.println(
                        "Unable to load icon: " + fileName);
                return null;
            }

            BufferedImage tinted = new BufferedImage(
                    original.getWidth(),
                    original.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            Color iconColor = new Color(245, 244, 239);

            for (int y = 0; y < original.getHeight(); y++) {

                for (int x = 0; x < original.getWidth(); x++) {

                    int pixel = original.getRGB(x, y);
                    int alpha = (pixel >> 24) & 0xff;

                    Color newColor = new Color(
                            iconColor.getRed(),
                            iconColor.getGreen(),
                            iconColor.getBlue(),
                            alpha);

                    tinted.setRGB(x, y, newColor.getRGB());
                }
            }

            Image resized = tinted.getScaledInstance(
                    22, 22, Image.SCALE_SMOOTH);

            return new ImageIcon(resized);

        } catch (IOException | RuntimeException exception) {

            System.out.println(
                    "Unable to load icon: " + fileName);

            return null;
        }
    }

}
