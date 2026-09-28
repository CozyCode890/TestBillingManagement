package billing.ui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {
    public LoginDialog(Frame parent) {
        super(parent, "Login to the System -- Retail Invoicing", true);
        setLayout(new BorderLayout(10, 10));

        // Tung: a place for "nhập liệu"
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 24, 12, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tung -> Huy: your task
        // add Ussername and Password

        pack();
        setLocationRelativeTo(parent);

        // Tung: I think that it's really hard to understand. So u don't need to try to
        // understand it.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}

// Tung: it's just for test. Now, u can execute directly this class.
class TestLoginDialog {
    public static void main(String[] args) {
        LoginDialog login = new LoginDialog(null);
        login.setVisible(true);
    }
}
