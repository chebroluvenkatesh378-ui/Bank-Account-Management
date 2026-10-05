package com.bank;

import com.bank.ui.BankFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class BankApp {
    private BankApp() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // The application can use the platform's default Swing theme.
            }
            new BankFrame().setVisible(true);
        });
    }
}