// Launcher.java
package main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import decrypt.PwndaHashDecryptApp;
import decrypt.DecryptAppLite;
import lite.liteApp;

public class Launcher extends JFrame {
    public Launcher() {
        setTitle("PwndaHash Launcher");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Choose an Application to Run", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton pwndaHashButton = new JButton("PwndaHash");
        JButton pwndaHashLiteButton = new JButton("PwndaHashLite");
        
        JButton pwndaHashDecrypt = new JButton("PwndaHash Decrypt");
        JButton LiteDecrypt = new JButton("Lite Decrypt");
        
        JButton pwndaPwsswordGeneratorASCII = new JButton("pwndaPwsswordGeneratorASCII");

        pwndaHashButton.addActionListener(e -> {
            // Launch PwndaHash
            SwingUtilities.invokeLater(PwndaHashMain::new);
            dispose(); // Close the launcher window
        });

        pwndaHashLiteButton.addActionListener(e -> {
            // Launch PwndaHashLite
            SwingUtilities.invokeLater(() -> {
                liteApp.main(null); // Call the static main method of MainApp
            });
            dispose(); // Close the launcher window
        });
        
        pwndaHashDecrypt.addActionListener(e -> {
            // Launch PwndaHashLite
            SwingUtilities.invokeLater(() -> {
            	PwndaHashDecryptApp.main(null); // Call the static main method of MainApp
            });
            dispose(); // Close the launcher window
        });
        LiteDecrypt.addActionListener(e -> {
            // Launch PwndaHashLite
            SwingUtilities.invokeLater(() -> {
            	DecryptAppLite.main(null); // Call the static main method of MainApp
            });
            dispose(); // Close the launcher window
        });

        pwndaPwsswordGeneratorASCII.addActionListener(e -> {
            // Launch PwndaHashLite
            SwingUtilities.invokeLater(() -> {
            	PasswordGeneratorLauncher.main(null); // Call the static main method of MainApp
            });
            dispose(); // Close the launcher window
        });

        buttonPanel.add(pwndaHashButton);
        buttonPanel.add(pwndaHashLiteButton);
        buttonPanel.add(pwndaHashDecrypt);
        buttonPanel.add(LiteDecrypt);
        buttonPanel.add(pwndaPwsswordGeneratorASCII);
        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Launcher::new);
    }
}
