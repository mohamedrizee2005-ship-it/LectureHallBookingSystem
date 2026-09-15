package hallsync;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Enable subpixel anti-aliasing for smooth modern fonts
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            FlatIntelliJLaf.setup();
        } catch (Exception ex) {
            System.err.println("Could not initialize FlatLaf theme: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}