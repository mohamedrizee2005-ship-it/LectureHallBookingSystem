package hallsync;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public final class UIUtils {

    // Palette Definition (includes backwards compatibility for all panels)
    public static final Color NAVY        = new Color(15, 23, 42);    // #0F172A Primary Dark Navy
    public static final Color NAVY_DARK   = NAVY;
    public static final Color NAVY2       = new Color(30, 41, 59);    // #1E293B Secondary Navy
    public static final Color NAVY_HOVER  = NAVY2;
    public static final Color NAVY_ACTIVE = new Color(51, 65, 85);    // #334155 Active Nav Pill
    public static final Color BG          = new Color(248, 250, 252); // #F8FAFC Clean App BG
    public static final Color CARD_BG     = Color.WHITE;
    public static final Color BORDER      = new Color(226, 232, 240); // #E2E8F0 Subtle Border
    public static final Color TEXT        = new Color(15, 23, 42);    // #0F172A Primary Text
    public static final Color TEXT_MAIN   = TEXT;
    public static final Color TEXT_MUTED  = new Color(100, 116, 139); // #64748B Secondary Text

    // Status & Action Accents
    public static final Color EMERALD     = new Color(16, 185, 129);  // #10B981 Success
    public static final Color ACCENT      = new Color(13, 148, 136);  // #0D9488 Teal
    public static final Color BLUE        = new Color(37, 99, 235);   // #2563EB Primary Action
    public static final Color AMBER       = new Color(245, 158, 11);  // #F59E0B Warning
    public static final Color RED         = new Color(239, 68, 68);   // #EF4444 Danger

    private UIUtils() {}

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT);
        return l;
    }

    public static JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        styleInputComponent(tf);
        return tf;
    }

    public static void styleInputComponent(JComponent comp) {
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comp.setBackground(Color.WHITE);
        comp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }

    // Modern Button with Rounded Corners & Hover Glow
    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 18, 9, 18));

        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(adjustBrightness(bg, 0.9f));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(bg);
            }
        });
        return b;
    }

    // Modern Rounded Card Panel
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        return p;
    }

    // Modern Table Styling with Status Pill Badges
    public static void styleTable(JTable t) {
        t.setRowHeight(38);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(238, 242, 255));
        t.setSelectionForeground(NAVY);

        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setForeground(TEXT_MUTED);
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < t.getColumnCount(); i++) {
            String colName = t.getColumnName(i).toLowerCase();
            if (colName.contains("status")) {
                t.getColumnModel().getColumn(i).setCellRenderer(new StatusBadgeRenderer());
            } else {
                t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    // Pill Badge Renderer (Approved/Available = Green, Pending = Amber, Cancelled/Maintenance = Red)
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String status = value == null ? "" : value.toString();

            Color badgeBg;
            Color textCol;
            String text = status;

            switch (status.trim().toLowerCase()) {
                case "approved":
                case "available":
                    badgeBg = new Color(220, 252, 231);
                    textCol = new Color(21, 128, 61);
                    text = "● " + status;
                    break;
                case "pending":
                    badgeBg = new Color(254, 243, 199);
                    textCol = new Color(180, 83, 9);
                    text = "● " + status;
                    break;
                case "cancelled":
                case "maintenance":
                    badgeBg = new Color(254, 226, 226);
                    textCol = new Color(185, 28, 28);
                    text = "● " + status;
                    break;
                default:
                    badgeBg = new Color(241, 245, 249);
                    textCol = TEXT_MUTED;
                    break;
            }

            final Color finalBg = badgeBg;
            final Color finalTxt = textCol;
            final String finalText = text;

            JPanel panel = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(finalBg);
                    int h = 24;
                    int w = getFontMetrics(getFont()).stringWidth(finalText) + 20;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;
                    g2.fill(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
                    g2.dispose();
                }
            };
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel lbl = new JLabel(finalText);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(finalTxt);
            panel.add(lbl);

            return panel;
        }
    }

    private static Color adjustBrightness(Color c, float factor) {
        return new Color(
            Math.max((int)(c.getRed() * factor), 0),
            Math.max((int)(c.getGreen() * factor), 0),
            Math.max((int)(c.getBlue() * factor), 0)
        );
    }

    public static void error(Component c, String s) {
        JOptionPane.showMessageDialog(c, s, "HallSync - Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component c, String s) {
        JOptionPane.showMessageDialog(c, s, "HallSync - Notification", JOptionPane.INFORMATION_MESSAGE);
    }
}