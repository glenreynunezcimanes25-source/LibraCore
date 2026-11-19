package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.TableRowSorter;

public final class RoundedComponents {

        private static final Color DIALOG_BACKGROUND = new Color(245, 244, 239);
        private static final Color DIALOG_TEXT = new Color(38, 50, 56);
        private static final Color DIALOG_MUTED = new Color(113, 128, 150);
        private static final Color DIALOG_DANGER = new Color(198, 91, 88);

        public enum DialogType {
                SUCCESS,
                INFO,
                ERROR,
                WARNING
        }

        private RoundedComponents() {
        }

        public static boolean showWarningDialog(
                        Component parent,
                        String title,
                        String message,
                        String confirmText) {

                return showDialog(
                                parent, title, message, DialogType.WARNING,
                                confirmText, true);
        }

        public static void showMessageDialog(
                        Component parent,
                        String title,
                        String message,
                        DialogType type) {

                showDialog(parent, title, message, type, "OK", false);
        }

        public static void showContentDialog(
                        Component parent,
                        String title,
                        String subtitle,
                        JComponent body) {
                JDialog dialog = new JDialog(
                                SwingUtilities.getWindowAncestor(parent),
                                title,
                                Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setResizable(true);

                JPanel content = new JPanel(new BorderLayout(0, 14));
                content.setBackground(DIALOG_BACKGROUND);
                content.setBorder(
                                BorderFactory.createEmptyBorder(20, 22, 18, 22));

                JPanel heading = new JPanel(new java.awt.GridLayout(2, 1, 0, 5));
                heading.setOpaque(false);
                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
                titleLabel.setForeground(DIALOG_TEXT);
                JLabel subtitleLabel = new JLabel(subtitle);
                subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                subtitleLabel.setForeground(DIALOG_MUTED);
                heading.add(titleLabel);
                heading.add(subtitleLabel);

                RoundedPanel bodyCard = new RoundedPanel(18, Color.WHITE);
                bodyCard.setLayout(new BorderLayout());
                bodyCard.setBorder(
                                BorderFactory.createEmptyBorder(14, 16, 14, 16));
                bodyCard.add(body, BorderLayout.CENTER);

                JButton closeButton = createDialogButton(
                                "Close", new Color(24, 58, 55), Color.WHITE);
                closeButton.addActionListener(event -> dialog.dispose());
                JPanel actions = new JPanel(
                                new FlowLayout(FlowLayout.RIGHT, 0, 0));
                actions.setOpaque(false);
                actions.add(closeButton);

                content.add(heading, BorderLayout.NORTH);
                content.add(bodyCard, BorderLayout.CENTER);
                content.add(actions, BorderLayout.SOUTH);

                dialog.setContentPane(content);
                dialog.getRootPane().setDefaultButton(closeButton);
                dialog.pack();
                dialog.setLocationRelativeTo(parent);
                dialog.setVisible(true);
        }

        public static void styleScrollPane(JScrollPane scrollPane) {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                if (verticalScrollBar != null) {
                        styleScrollBar(verticalScrollBar);
                }
                JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
                if (horizontalScrollBar != null) {
                        styleScrollBar(horizontalScrollBar);
                }
                scrollPane.setWheelScrollingEnabled(true);
        }

        public static JPanel createStatCard(
                        String title,
                        JLabel value,
                        String description,
                        Color accent,
                        javax.swing.ImageIcon icon) {
                return createStatCard(
                                title, value, description, accent, icon, 18);
        }

        public static JPanel createStatCard(
                        String title,
                        JLabel value,
                        String description,
                        Color accent,
                        javax.swing.ImageIcon icon,
                        int cornerRadius) {
                return createStatCard(
                                title, value, description, accent, icon,
                                cornerRadius, 104, 30, 9, 30, 10, 14, 14, 16);
        }

        public static JPanel createStatCard(
                        String title,
                        JLabel value,
                        String description,
                        Color accent,
                        javax.swing.ImageIcon icon,
                        int cornerRadius,
                        int cardHeight,
                        int valueFontSize,
                        int titleFontSize,
                        int iconSize,
                        int topInset,
                        int sideInset,
                        int bottomInset,
                        int trailingInset) {
                StatCardSurface card = new StatCardSurface(cornerRadius, Color.WHITE);
                card.setLayout(new BorderLayout(10, 0));
                card.setBorder(BorderFactory.createEmptyBorder(
                                topInset, sideInset, bottomInset, trailingInset));
                card.setPreferredSize(new Dimension(180, cardHeight));

                JPanel accentBar = new JPanel();
                accentBar.setBackground(accent);
                accentBar.setPreferredSize(new Dimension(4, 1));

                JPanel details = new JPanel(new BorderLayout(0, 1));
                details.setOpaque(false);

                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font("Arial", Font.BOLD, titleFontSize));
                titleLabel.setForeground(new Color(113, 128, 150));

                value.setFont(new Font("Arial", Font.BOLD, valueFontSize));
                value.setForeground(accent);

                JLabel descriptionLabel = new JLabel(description);
                descriptionLabel.setFont(new Font("Arial", Font.PLAIN, titleFontSize));
                descriptionLabel.setForeground(new Color(113, 128, 150));

                JPanel titleRow = new JPanel(new BorderLayout(4, 0));
                titleRow.setOpaque(false);
                titleRow.add(titleLabel, BorderLayout.CENTER);

                RoundedPanel iconBadge = new RoundedPanel(
                                Math.max(10, iconSize / 2), blendWithWhite(accent, 0.12));
                iconBadge.setLayout(new BorderLayout());
                iconBadge.setPreferredSize(new Dimension(iconSize, iconSize));
                JLabel iconLabel = new JLabel(icon);
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                iconBadge.add(iconLabel, BorderLayout.CENTER);
                titleRow.add(iconBadge, BorderLayout.EAST);

                details.add(titleRow, BorderLayout.NORTH);
                details.add(value, BorderLayout.CENTER);
                details.add(descriptionLabel, BorderLayout.SOUTH);

                card.add(accentBar, BorderLayout.WEST);
                card.add(details, BorderLayout.CENTER);
                return card;
        }

        private static Color blendWithWhite(Color color, double amount) {
                return new Color(
                                (int) (color.getRed() * amount + 255 * (1 - amount)),
                                (int) (color.getGreen() * amount + 255 * (1 - amount)),
                                (int) (color.getBlue() * amount + 255 * (1 - amount)));
        }

        public static void styleComboBox(JComboBox<?> comboBox) {
                styleComboBox(comboBox, new Dimension(136, 36));
        }

        public static void styleComboBox(
                        JComboBox<?> comboBox,
                        Dimension preferredSize) {
                styleComboBox(
                                comboBox, preferredSize, new Color(24, 58, 55), "Arial");
        }

        public static void styleComboBox(
                        JComboBox<?> comboBox,
                        Dimension preferredSize,
                        String fontFamily) {
                styleComboBox(
                                comboBox, preferredSize, new Color(24, 58, 55), fontFamily);
        }

        public static void styleRoundedComboBox(
                        JComboBox<?> comboBox,
                        Dimension preferredSize,
                        String fontFamily,
                        int cornerRadius) {
                styleRoundedComboBox(
                                comboBox,
                                preferredSize,
                                fontFamily,
                                cornerRadius,
                                new Color(22, 63, 58));
        }

        public static void styleRoundedComboBox(
                        JComboBox<?> comboBox,
                        Dimension preferredSize,
                        String fontFamily,
                        int cornerRadius,
                        Color arrowForeground) {
                styleComboBox(
                                comboBox,
                                preferredSize,
                                arrowForeground,
                                fontFamily,
                                cornerRadius,
                                true);
        }

        public static void styleSortComboBox(
                        JComboBox<?> comboBox,
                        Dimension preferredSize) {
                styleComboBox(
                                comboBox, preferredSize, new Color(38, 50, 56), "Arial");
        }

        public static JComboBox<String> createSortComboBox(
                        JTable table,
                        Dimension preferredSize) {
                configureTableSorter(table);
                TableRowSorter<?> sorter = (TableRowSorter<?>) table.getRowSorter();

                String[] options = new String[table.getColumnCount() + 1];
                options[0] = "Sort By";
                for (int column = 0; column < table.getColumnCount(); column++) {
                        options[column + 1] = table.getColumnName(column);
                }

                JComboBox<String> sortBy = new JComboBox<>(options);
                styleSortComboBox(sortBy, preferredSize);
                sortBy.setToolTipText("Choose a column to sort by.");
                sortBy.addActionListener(event -> {
                        int selectedIndex = sortBy.getSelectedIndex();
                        if (selectedIndex <= 0) {
                                sorter.setSortKeys(Collections.emptyList());
                                return;
                        }
                        sorter.setSortKeys(Collections.singletonList(
                                        new RowSorter.SortKey(
                                                        table.convertColumnIndexToModel(
                                                                        selectedIndex - 1),
                                                        SortOrder.ASCENDING)));
                });
                return sortBy;
        }

        public static void configureTableSorter(JTable table) {
                if (!(table.getRowSorter() instanceof TableRowSorter<?>)) {
                        throw new IllegalStateException(
                                        "A table row sorter is required to configure sorting.");
                }

                TableRowSorter<?> sorter = (TableRowSorter<?>) table.getRowSorter();
                for (int column = 0; column < table.getColumnCount(); column++) {
                        sorter.setComparator(
                                        table.convertColumnIndexToModel(column),
                                        RoundedComponents::compareSortValues);
                }
        }

        private static int compareSortValues(Object first, Object second) {
                if (first == second) {
                        return 0;
                }
                if (isEmptySortValue(first)) {
                        return isEmptySortValue(second) ? 0 : 1;
                }
                if (isEmptySortValue(second)) {
                        return -1;
                }
                if (first instanceof LocalDate && second instanceof LocalDate) {
                        return ((LocalDate) first).compareTo((LocalDate) second);
                }

                String firstText = first.toString().trim();
                String secondText = second.toString().trim();
                Double firstNumber = parseSortNumber(first);
                Double secondNumber = parseSortNumber(second);
                if (firstNumber != null && secondNumber != null) {
                        return Double.compare(firstNumber, secondNumber);
                }

                LocalDate firstDate = parseSortDate(firstText);
                LocalDate secondDate = parseSortDate(secondText);
                if (firstDate != null && secondDate != null) {
                        return firstDate.compareTo(secondDate);
                }
                return firstText.compareToIgnoreCase(secondText);
        }

        private static boolean isEmptySortValue(Object value) {
                if (value == null) {
                        return true;
                }
                String text = value.toString().trim();
                return text.isEmpty() || "-".equals(text);
        }

        private static Double parseSortNumber(Object value) {
                if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                }

                String text = value.toString()
                                .replace("₱", "")
                                .replace(",", "")
                                .trim();
                try {
                        return Double.valueOf(text);
                } catch (NumberFormatException exception) {
                        int space = text.indexOf(' ');
                        if (space > 0) {
                                try {
                                        return Double.valueOf(
                                                        text.substring(0, space));
                                } catch (NumberFormatException ignored) {
                                        return null;
                                }
                        }
                        return null;
                }
        }

        private static LocalDate parseSortDate(String value) {
                try {
                        return LocalDate.parse(value);
                } catch (DateTimeParseException exception) {
                        return null;
                }
        }

        private static void styleComboBox(
                        JComboBox<?> comboBox,
                        Dimension preferredSize,
                        Color arrowForeground,
                        String fontFamily) {
                styleComboBox(
                                comboBox,
                                preferredSize,
                                arrowForeground,
                                fontFamily,
                                0,
                                false);
        }

        private static void styleComboBox(
                        JComboBox<?> comboBox,
                        Dimension preferredSize,
                        Color arrowForeground,
                        String fontFamily,
                        int cornerRadius,
                        boolean useVectorArrow) {
                comboBox.setUI(new BasicComboBoxUI() {
                        @Override
                        protected ComboPopup createPopup() {
                                return new BasicComboPopup(comboBox) {
                                        private static final long serialVersionUID = 1L;

                                        @Override
                                        protected JScrollPane createScroller() {
                                                JScrollPane scroller = super.createScroller();
                                                // Apply styling after the popup has initialized its scroll bars.
                                                SwingUtilities.invokeLater(
                                                                () -> styleScrollPane(scroller));
                                                return scroller;
                                        }
                                };
                        }

                        @Override
                        protected JButton createArrowButton() {
                                if (useVectorArrow) {
                                        JButton arrow = new JButton(
                                                        createDropdownArrowIcon(
                                                                        arrowForeground,
                                                                        comboBox)) {
                                                private static final long serialVersionUID = 1L;

                                                @Override
                                                public Dimension getPreferredSize() {
                                                        int comboWidth = comboBox.getWidth();
                                                        int width = comboWidth > 0
                                                                        && comboWidth < 160
                                                                                        ? 28
                                                                                        : 48;
                                                        return new Dimension(width, 50);
                                                }
                                        };
                                        arrow.setName("member-filter-dropdown-arrow");
                                        arrow.setMargin(new Insets(0, 0, 0, 0));
                                        arrow.setBorder(
                                                        BorderFactory.createMatteBorder(
                                                                        0,
                                                                        1,
                                                                        0,
                                                                        0,
                                                                        new Color(225, 225, 218)));
                                        arrow.setBackground(Color.WHITE);
                                        arrow.setOpaque(true);
                                        arrow.setContentAreaFilled(true);
                                        arrow.setFocusable(false);
                                        arrow.setFocusPainted(false);
                                        arrow.setRolloverEnabled(false);
                                        arrow.setToolTipText("Show filter options");
                                        return arrow;
                                }
                                JButton arrow = new JButton("\u25BE");
                                arrow.setFont(new Font("Arial", Font.BOLD, 14));
                                arrow.setForeground(arrowForeground);
                                arrow.setBackground(Color.WHITE);
                                arrow.setBorder(
                                                BorderFactory.createEmptyBorder(0, 8, 0, 8));
                                arrow.setContentAreaFilled(false);
                                arrow.setFocusPainted(false);
                                arrow.setFocusable(false);
                                return arrow;
                        }
                });

                int fontSize = useVectorArrow ? 14 : 12;
                comboBox.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
                comboBox.setForeground(new Color(38, 50, 56));
                comboBox.setBackground(
                                useVectorArrow
                                                ? new Color(238, 238, 238)
                                                : Color.WHITE);
                comboBox.setOpaque(true);
                comboBox.setPreferredSize(preferredSize);
                comboBox.setMaximumRowCount(6);
                comboBox.setCursor(new java.awt.Cursor(
                                java.awt.Cursor.HAND_CURSOR));
                Border comboBorder = cornerRadius > 0
                                ? new RoundedComboBoxBorder(
                                                new Color(225, 225, 218),
                                                cornerRadius)
                                : BorderFactory.createLineBorder(
                                                new Color(225, 225, 218));
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                                comboBorder,
                                BorderFactory.createEmptyBorder(
                                                0, 12, 0, 8)));
                comboBox.setRenderer(new DefaultListCellRenderer() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public Component getListCellRendererComponent(
                                        JList<?> list,
                                        Object value,
                                        int index,
                                        boolean isSelected,
                                        boolean cellHasFocus) {

                                JLabel label = (JLabel) super.getListCellRendererComponent(
                                                list, value, index,
                                                isSelected, cellHasFocus);
                                label.setFont(
                                                new Font(
                                                                fontFamily,
                                                                Font.PLAIN,
                                                                fontSize));
                                label.setBorder(
                                                BorderFactory.createEmptyBorder(6, 9, 6, 9));
                                if (index < 0 && useVectorArrow) {
                                        label.setBackground(new Color(238, 238, 238));
                                        label.setForeground(new Color(38, 50, 56));
                                } else if (isSelected) {
                                        label.setBackground(new Color(232, 240, 235));
                                        label.setForeground(new Color(24, 58, 55));
                                } else {
                                        label.setBackground(Color.WHITE);
                                        label.setForeground(new Color(38, 50, 56));
                                }
                                return label;
                        }
                });
        }

        private static Icon createDropdownArrowIcon(
                        Color arrowColor,
                        JComboBox<?> comboBox) {
                return new Icon() {
                        @Override
                        public int getIconWidth() {
                                return comboBox.getWidth() > 0
                                                && comboBox.getWidth() < 160
                                                                ? 18
                                                                : 28;
                        }

                        @Override
                        public int getIconHeight() {
                                return comboBox.getWidth() > 0
                                                && comboBox.getWidth() < 160
                                                                ? 12
                                                                : 16;
                        }

                        @Override
                        public void paintIcon(
                                        Component component,
                                        Graphics graphics,
                                        int x,
                                        int y) {
                                Graphics2D graphics2D =
                                                (Graphics2D) graphics.create();
                                int width = getIconWidth();
                                int height = getIconHeight();
                                graphics2D.setRenderingHint(
                                                RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);
                                graphics2D.setColor(arrowColor);
                                graphics2D.fillPolygon(
                                                new int[] {
                                                                x,
                                                                x + width,
                                                                x + width / 2
                                                },
                                                new int[] {
                                                                y,
                                                                y,
                                                                y + height - 1
                                                },
                                                3);
                                graphics2D.dispose();
                        }
                };
        }

        private static final class RoundedComboBoxBorder extends AbstractBorder {

                private static final long serialVersionUID = 1L;

                private final Color color;
                private final int cornerRadius;

                private RoundedComboBoxBorder(Color color, int cornerRadius) {
                        this.color = color;
                        this.cornerRadius = cornerRadius;
                }

                @Override
                public void paintBorder(
                                Component component,
                                Graphics graphics,
                                int x,
                                int y,
                                int width,
                                int height) {
                        Graphics2D graphics2D =
                                        (Graphics2D) graphics.create();
                        graphics2D.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                        graphics2D.setColor(color);
                        graphics2D.drawRoundRect(
                                        x,
                                        y,
                                        width - 1,
                                        height - 1,
                                        cornerRadius,
                                        cornerRadius);
                        graphics2D.dispose();
                }
        }

        private static void styleScrollBar(JScrollBar scrollBar) {
                scrollBar.setUI(new BasicScrollBarUI() {

                        private final Color trackColor = new Color(240, 241, 237);
                        private final Color thumbColor = new Color(159, 174, 163);
                        private final Color thumbHoverColor = new Color(119, 142, 126);
                        private final Color thumbPressedColor = new Color(78, 105, 88);

                        @Override
                        protected void paintTrack(
                                        Graphics graphics,
                                        JComponent component,
                                        Rectangle bounds) {

                                Graphics2D graphics2D = (Graphics2D) graphics.create();
                                graphics2D.setRenderingHint(
                                                RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);
                                graphics2D.setColor(trackColor);

                                if (scrollBar.getOrientation() == JScrollBar.VERTICAL) {
                                        graphics2D.fillRoundRect(
                                                        bounds.x + bounds.width / 2 - 2,
                                                        bounds.y + 3,
                                                        4,
                                                        Math.max(0, bounds.height - 6),
                                                        4,
                                                        4);
                                } else {
                                        graphics2D.fillRoundRect(
                                                        bounds.x + 3,
                                                        bounds.y + bounds.height / 2 - 2,
                                                        Math.max(0, bounds.width - 6),
                                                        4,
                                                        4,
                                                        4);
                                }

                                graphics2D.dispose();
                        }

                        @Override
                        protected void paintThumb(
                                        Graphics graphics,
                                        JComponent component,
                                        Rectangle bounds) {

                                if (bounds.isEmpty() || !scrollBar.isEnabled()) {
                                        return;
                                }

                                Graphics2D graphics2D = (Graphics2D) graphics.create();
                                graphics2D.setRenderingHint(
                                                RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);

                                Color color = isDragging
                                                ? thumbPressedColor
                                                : isThumbRollover()
                                                                ? thumbHoverColor
                                                                : thumbColor;
                                graphics2D.setColor(color);

                                int inset = 3;
                                int thumbWidth = scrollBar.getOrientation() == JScrollBar.VERTICAL
                                                ? Math.min(10, Math.max(0, bounds.width - inset * 2))
                                                : Math.max(0, bounds.width - inset * 2);
                                int thumbHeight = scrollBar.getOrientation() == JScrollBar.HORIZONTAL
                                                ? Math.min(10, Math.max(0, bounds.height - inset * 2))
                                                : Math.max(0, bounds.height - inset * 2);
                                graphics2D.fillRoundRect(
                                                bounds.x + (bounds.width - thumbWidth) / 2,
                                                bounds.y + (bounds.height - thumbHeight) / 2,
                                                thumbWidth,
                                                thumbHeight,
                                                10,
                                                10);
                                graphics2D.dispose();
                        }

                        @Override
                        protected Dimension getMinimumThumbSize() {
                                return scrollBar.getOrientation() == JScrollBar.VERTICAL
                                                ? new Dimension(12, 36)
                                                : new Dimension(36, 12);
                        }
                });

                scrollBar.setOpaque(false);
                scrollBar.setBorder(BorderFactory.createEmptyBorder());
                scrollBar.setPreferredSize(
                                scrollBar.getOrientation() == JScrollBar.VERTICAL
                                                ? new Dimension(16, 0)
                                                : new Dimension(0, 16));
                scrollBar.setUnitIncrement(16);
        }

        private static boolean showDialog(
                        Component parent,
                        String title,
                        String message,
                        DialogType type,
                        String actionText,
                        boolean confirmation) {

                boolean[] confirmed = { false };
                Window owner = SwingUtilities.getWindowAncestor(parent);
                JDialog dialog = new JDialog(
                                owner, title, Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setResizable(false);

                Color accent;
                Color iconBackground;
                String iconText;
                switch (type) {
                        case SUCCESS:
                                accent = new Color(57, 134, 106);
                                iconBackground = new Color(231, 244, 237);
                                iconText = "\u2713";
                                break;
                        case INFO:
                                accent = new Color(76, 116, 173);
                                iconBackground = new Color(232, 239, 249);
                                iconText = "i";
                                break;
                        case ERROR:
                                accent = DIALOG_DANGER;
                                iconBackground = new Color(252, 235, 233);
                                iconText = "!";
                                break;
                        case WARNING:
                        default:
                                accent = new Color(154, 107, 35);
                                iconBackground = new Color(252, 239, 214);
                                iconText = "!";
                                break;
                }

                JPanel content = new JPanel(new BorderLayout(0, 14));
                content.setBackground(DIALOG_BACKGROUND);
                content.setBorder(
                                BorderFactory.createEmptyBorder(20, 22, 18, 22));

                RoundedPanel messageCard = new RoundedPanel(18, Color.WHITE);
                messageCard.setLayout(new BorderLayout(12, 0));
                messageCard.setBorder(
                                BorderFactory.createEmptyBorder(16, 18, 16, 18));

                RoundedPanel dialogIcon = new RoundedPanel(24, iconBackground);
                dialogIcon.setLayout(new BorderLayout());
                dialogIcon.setPreferredSize(new Dimension(38, 38));

                JLabel iconLabel = new JLabel(iconText, SwingConstants.CENTER);
                iconLabel.setFont(new Font("Arial", Font.BOLD, 18));
                iconLabel.setForeground(accent);
                dialogIcon.add(iconLabel, BorderLayout.CENTER);

                JPanel textPanel = new JPanel(new BorderLayout(0, 5));
                textPanel.setOpaque(false);

                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
                titleLabel.setForeground(DIALOG_TEXT);

                String messageText = message == null || message.trim().isEmpty()
                                ? "An unexpected error occurred."
                                : message;
                JTextArea messageLabel = new JTextArea(messageText);
                messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                messageLabel.setForeground(DIALOG_MUTED);
                messageLabel.setOpaque(false);
                messageLabel.setEditable(false);
                messageLabel.setFocusable(false);
                messageLabel.setLineWrap(true);
                messageLabel.setWrapStyleWord(true);
                messageLabel.setColumns(38);
                messageLabel.setRows(estimateMessageRows(messageText));
                messageLabel.setBorder(BorderFactory.createEmptyBorder());

                JScrollPane messageScroll = new JScrollPane(
                                messageLabel,
                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                styleScrollPane(messageScroll);
                messageScroll.setBorder(BorderFactory.createEmptyBorder());
                messageScroll.setOpaque(false);
                messageScroll.getViewport().setOpaque(false);
                messageScroll.setPreferredSize(
                                new Dimension(340, Math.min(
                                                220, Math.max(
                                                                42, messageLabel.getPreferredSize().height))));
                messageLabel.setCaretPosition(0);

                textPanel.add(titleLabel, BorderLayout.NORTH);
                textPanel.add(messageScroll, BorderLayout.CENTER);

                messageCard.add(dialogIcon, BorderLayout.WEST);
                messageCard.add(textPanel, BorderLayout.CENTER);

                JPanel actions = new JPanel(
                                new FlowLayout(FlowLayout.RIGHT, 8, 0));
                actions.setOpaque(false);

                JButton defaultButton;
                if (confirmation) {
                        JButton cancelButton = createDialogButton(
                                        "Cancel", new Color(232, 233, 226), DIALOG_TEXT);
                        cancelButton.addActionListener(event -> dialog.dispose());

                        JButton confirmButton = createDialogButton(
                                        actionText, DIALOG_DANGER, Color.WHITE);
                        confirmButton.addActionListener(event -> {
                                confirmed[0] = true;
                                dialog.dispose();
                        });

                        actions.add(cancelButton);
                        actions.add(confirmButton);
                        defaultButton = cancelButton;
                } else {
                        JButton closeButton = createDialogButton(
                                        actionText, accent, Color.WHITE);
                        closeButton.addActionListener(event -> dialog.dispose());
                        actions.add(closeButton);
                        defaultButton = closeButton;
                }

                content.add(messageCard, BorderLayout.CENTER);
                content.add(actions, BorderLayout.SOUTH);

                dialog.setContentPane(content);
                dialog.getRootPane().setDefaultButton(defaultButton);
                dialog.pack();
                dialog.setLocationRelativeTo(parent);
                dialog.setVisible(true);

                return confirmed[0];
        }

        private static int estimateMessageRows(String message) {

                int rows = 0;
                for (String line : message.split("\\R", -1)) {
                        rows += Math.max(1, (line.length() + 37) / 38);
                }

                return Math.min(12, Math.max(2, rows));
        }

        private static JButton createDialogButton(
                        String text,
                        Color background,
                        Color foreground) {

                JButton button = new RoundedButton(text);
                button.setFont(new Font("Arial", Font.BOLD, 12));
                button.setBackground(background);
                button.setForeground(foreground);
                button.setPreferredSize(new Dimension(120, 38));
                button.setCursor(
                                new java.awt.Cursor(
                                                java.awt.Cursor.HAND_CURSOR));
                return button;
        }

        public static final class RoundedButton extends JButton {

                private static final long serialVersionUID = 1L;
                private final int cornerRadius;

                public RoundedButton(String text) {
                        this(text, 14);
                }

                public RoundedButton(String text, int cornerRadius) {
                        super(text);
                        this.cornerRadius = cornerRadius;
                        setContentAreaFilled(false);
                        setOpaque(false);
                        setBorderPainted(false);
                        setFocusPainted(false);
                }

                @Override
                protected void paintComponent(Graphics graphics) {
                        Graphics2D graphics2D = (Graphics2D) graphics.create();
                        graphics2D.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                        graphics2D.setColor(getBackground());
                        graphics2D.fillRoundRect(
                                        0, 0, getWidth(), getHeight(),
                                        cornerRadius, cornerRadius);
                        graphics2D.dispose();

                        super.paintComponent(graphics);
                }
        }

        private static final class StatCardSurface extends JPanel {

                private static final long serialVersionUID = 1L;
                private final int cornerRadius;

                private StatCardSurface(int cornerRadius, Color background) {
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
                                graphics2D.setColor(
                                                new Color(15, 23, 42, 4));
                                graphics2D.fillRoundRect(
                                                1, offset, getWidth() - 5,
                                                getHeight() - 5,
                                                cornerRadius, cornerRadius);
                        }
                        graphics2D.setColor(getBackground());
                        graphics2D.fillRoundRect(
                                        0, 0, getWidth() - 5,
                                        getHeight() - 5,
                                        cornerRadius, cornerRadius);
                        graphics2D.dispose();
                }
        }

        public static final class RoundedPanel extends JPanel {

                private static final long serialVersionUID = 1L;

                private final int cornerRadius;

                public RoundedPanel(int cornerRadius, Color background) {
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
                        graphics2D.setColor(getBackground());
                        graphics2D.fillRoundRect(
                                        0, 0, getWidth(), getHeight(),
                                        cornerRadius, cornerRadius);
                        graphics2D.dispose();
                }
        }
}
