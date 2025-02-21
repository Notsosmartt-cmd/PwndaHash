// MainApp.java
package main;

import decrypt.Decrypter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;
import processors.Utf8Utils;


public class decryptApp {
   
    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Decrypt");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(null);

        // Password source selection
        JRadioButton manualPasswordRadio = new JRadioButton("Enter Password");
        JRadioButton filePasswordRadio = new JRadioButton("Load Password from File");
        ButtonGroup passwordGroup = new ButtonGroup();
        passwordGroup.add(manualPasswordRadio);
        passwordGroup.add(filePasswordRadio);
        manualPasswordRadio.setSelected(true);

        manualPasswordRadio.setBounds(50, 20, 150, 30);
        filePasswordRadio.setBounds(200, 20, 200, 30);
        frame.add(manualPasswordRadio);
        frame.add(filePasswordRadio);

        // Password input components
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 60, 150, 30);
        frame.add(passwordLabel);

        JTextField passwordField = new JTextField();
        passwordField.setBounds(150, 60, 250, 30);
        frame.add(passwordField);
       

        // Password file selection components
        JButton passwordFileButton = new JButton("Select Password File");
        passwordFileButton.setBounds(150, 60, 250, 30);
        passwordFileButton.setVisible(false);
        frame.add(passwordFileButton);

        JLabel passwordFileLabel = new JLabel("No password file selected");
        passwordFileLabel.setBounds(410, 60, 200, 30);
        frame.add(passwordFileLabel);

        // Input file selection components
        JButton selectFileButton = new JButton("Select Input File");
        selectFileButton.setBounds(50, 100, 150, 30);
        frame.add(selectFileButton);

        JLabel selectedFileLabel = new JLabel("No input file selected.");
        selectedFileLabel.setBounds(210, 100, 350, 30);
        frame.add(selectedFileLabel);

         String[] inputFilePath = {null};
         String[] passwordFilePath = {null};

        // Radio button listeners
        manualPasswordRadio.addActionListener(e -> {
            passwordField.setVisible(true);
            passwordFileButton.setVisible(false);
            passwordFileLabel.setText("No password file selected");
        });

        filePasswordRadio.addActionListener(e -> {
            passwordField.setVisible(false);
            passwordFileButton.setVisible(true);
        });

        // Password file selection ***
        passwordFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    passwordFilePath[0] = selectedFile.getAbsolutePath();
                    
                    //determine if zip file
                    if (passwordFilePath[0].toLowerCase().endsWith(".zip")) {
                        try {
                            // Handle zip file
                            File tempDir = Files.createTempDirectory("passwordZip").toFile();
                            java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(selectedFile);
                            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
                            boolean passwordFileFound = false;
                            while (entries.hasMoreElements()) {
                                java.util.zip.ZipEntry entry = entries.nextElement();
                                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".txt")) {
                                    File extractedFile = new File(tempDir, entry.getName());
                                    Files.copy(zipFile.getInputStream(entry), extractedFile.toPath());
                                    passwordFilePath[0] = extractedFile.getAbsolutePath();
                                    passwordFileFound = true;
                                    break;
                                }
                            }
                            zipFile.close();
                            if (passwordFileFound) {
                                passwordFileLabel.setText("File (from zip): " + new File(passwordFilePath[0]).getName());
                            } else {
                                JOptionPane.showMessageDialog(frame, "No password file found in the zip.", "Error", JOptionPane.ERROR_MESSAGE);
                                passwordFilePath[0] = null; // Reset if no valid file found
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, "Error reading zip file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            passwordFilePath[0] = null; // Reset in case of error
                        }
                        // Regular file selected
                    } else {
                        passwordFileLabel.setText("File: " + selectedFile.getName());
                    }
                }
            }
        });
        

        // Input file selection
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    inputFilePath[0] = selectedFile.getAbsolutePath();
                    selectedFileLabel.setText("File: " + selectedFile.getName());
                }
            }
        });

        // Generate button
        JButton generateButton = new JButton("Process File");
        generateButton.setBounds(100, 140, 200, 30);
        frame.add(generateButton);
        
        

        // Result label
        JLabel resultLabel = new JLabel("Click the button after selecting files/password");
        resultLabel.setBounds(50, 180, 500, 30);
        frame.add(resultLabel);

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Validate input file
                    if (inputFilePath[0] == null) {
                        JOptionPane.showMessageDialog(frame, "Please select an input file.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Get password
                    String password = "";
                    if (manualPasswordRadio.isSelected()) {
                        password = passwordField.getText();
                        if (password.isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "Please enter a password.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        if (passwordFilePath[0] == null) {
                            JOptionPane.showMessageDialog(frame, "Please select a password file.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        password = new String(Files.readAllBytes(new File(passwordFilePath[0]).toPath()));
                        if (password.isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "Password file is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                 // Process files
                    File inputFile = new File(inputFilePath[0]);
                    String inputFileName = inputFile.getName();
                    String outputFileName = "(Decrypted)" + inputFileName;
                    File outputFile = new File(inputFile.getParent(), outputFileName);
                    
                    //password is being directly imported to integer and doesn't consider surrogate. Import characters, then use UTF-8 utils to change them to int
                    
                 //   List<Character> utfChar = Utf8Utils.convertToUtf8ArrayList(password);
                    
                    List<Character> utf8Values = Utf8Utils.convertToCharacterArrayList(password);
                    System.out.println(utf8Values);
                    
                    Decrypter fileProcessor = new Decrypter(utf8Values);

                 
                    
                    fileProcessor.processFile(inputFilePath[0], outputFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(frame, 
                        "Processing complete!\nOutput saved to:\n" + outputFile.getAbsolutePath());

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        
        // Home Button
        JButton homeButton = new JButton("Home");
        homeButton.setBounds(450, 400, 100, 30);
        frame.add(homeButton);

        homeButton.addActionListener(e -> {
            frame.dispose(); // Close the current window
            Launcher.main(null); // Go back to the main launcher
        });


        frame.setVisible(true);
    }
}