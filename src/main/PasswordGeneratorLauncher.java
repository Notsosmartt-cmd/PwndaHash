package main;

import generators.PasswordGeneratorASCII;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public class PasswordGeneratorLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Password Generator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel lengthLabel = new JLabel("Enter password length (1-512):");
            JTextField lengthField = new JTextField();
            lengthField.setPreferredSize(new Dimension(100, 25));
            lengthField.setMaximumSize(new Dimension(100, 25));

            JLabel directoryLabel = new JLabel("Select output directory:");
            JTextField directoryField = new JTextField();
            directoryField.setPreferredSize(new Dimension(300, 25));
            directoryField.setMaximumSize(new Dimension(300, 25));
            JButton browseButton = new JButton("Browse");
            browseButton.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton generateButton = new JButton("Generate Password");
            JTextArea outputArea = new JTextArea(4, 30);
            outputArea.setEditable(false);
            outputArea.setLineWrap(true);
            outputArea.setWrapStyleWord(true);

            // Home Button
            JButton homeButton = new JButton("Home");
            homeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Action listeners
            browseButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = fileChooser.getSelectedFile();
                    directoryField.setText(selectedDir.getAbsolutePath());
                }
            });

            generateButton.addActionListener(e -> {
                try {
                    int passwordLength = Integer.parseInt(lengthField.getText().trim());
                    if (passwordLength < 1 || passwordLength > 512) {
                        JOptionPane.showMessageDialog(frame, "Password length must be between 1 and 512.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String outputDirPath = directoryField.getText().trim();
                    File outputDir = new File(outputDirPath);
                    if (!outputDir.exists() || !outputDir.isDirectory()) {
                        JOptionPane.showMessageDialog(frame, "Invalid output directory.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String password = PasswordGeneratorASCII.generatePassword(passwordLength);
                    String zipFilename = outputDir.getAbsolutePath() + File.separator + "GeneratedPasswordAscii.zip";
                    String textFilename = "GeneratedPasswordAscii.txt";

                    PasswordGeneratorASCII.zipPasswordToFile(zipFilename, textFilename, password);

                    outputArea.setText("Generated Password:\n" + password + "\n\nPassword saved to: " + zipFilename);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid password length. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            homeButton.addActionListener(e -> {
                frame.dispose(); // Close the current window
                Launcher.main(null); // Go back to the main launcher
            });

            // Add components to the panel
            panel.add(lengthLabel);
            panel.add(lengthField);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(directoryLabel);

            JPanel directoryPanel = new JPanel();
            directoryPanel.setLayout(new BoxLayout(directoryPanel, BoxLayout.X_AXIS));
            directoryPanel.add(directoryField);
            directoryPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            directoryPanel.add(browseButton);
            panel.add(directoryPanel);

            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(generateButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(new JScrollPane(outputArea));
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(homeButton);

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
