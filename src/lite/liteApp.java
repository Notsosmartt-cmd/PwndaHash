// Import necessary packages
package lite;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;
import main.Launcher;
import processors.Utf8Utils;

public class liteApp {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Gaussian Random Number Generator");
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

        JButton selectDirectoryButton = new JButton("Select Input Directory");
        selectDirectoryButton.setBounds(210, 100, 200, 30);
        frame.add(selectDirectoryButton);

        JLabel selectedFileOrDirectoryLabel = new JLabel("No file or directory selected.");
        selectedFileOrDirectoryLabel.setBounds(50, 140, 500, 30);
        frame.add(selectedFileOrDirectoryLabel);

        final String[] inputPath = {null};
        final boolean[] isDirectory = {false};
        final String[] passwordFilePath = {null};

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
        passwordFileButton.addActionListener(e -> {
            FileDialog fileDialog = new FileDialog(frame, "Select Password File", FileDialog.LOAD);
            fileDialog.setVisible(true);
            String fileName = fileDialog.getFile();
            if (fileName != null) {
                File selectedFile = new File(fileDialog.getDirectory(), fileName);
                passwordFilePath[0] = selectedFile.getAbsolutePath();
                
                // Determine if zip file
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
                } else {
                    // Regular file selected
                    passwordFileLabel.setText("File: " + selectedFile.getName());
                }
            } });

        // Input file selection
        selectFileButton.addActionListener(e -> {
            FileDialog fileDialog = new FileDialog(frame, "Select Input File", FileDialog.LOAD);
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName != null) {
            File selectedFile = new File(fileDialog.getDirectory(), fileName);
            inputPath[0] = selectedFile.getAbsolutePath();
            isDirectory[0] = false;
            selectedFileOrDirectoryLabel.setText("File: " + selectedFile.getName());
        }
        });

        // Directory selection
        selectDirectoryButton.addActionListener(e -> {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = directoryChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = directoryChooser.getSelectedFile();
                inputPath[0] = selectedDirectory.getAbsolutePath();
                isDirectory[0] = true;
                selectedFileOrDirectoryLabel.setText("Directory: " + selectedDirectory.getName());
            }
        });

        // Generate button
        JButton generateButton = new JButton("Process");
        generateButton.setBounds(100, 180, 200, 30);
        frame.add(generateButton);

        generateButton.addActionListener(e -> {
            try {
                // Validate input path
                if (inputPath[0] == null) {
                    JOptionPane.showMessageDialog(frame, "Please select an input file or directory.", "Error", JOptionPane.ERROR_MESSAGE);
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

                //  Turns password into numbers
                List<Integer> utf8Values = Utf8Utils.convertToCodePointArrayList(password);
                
                //Encrypts files 
                FileProcessor fileProcessor = new FileProcessor(utf8Values); //feeds file processor class utf8 password values

                // Process input
                if (isDirectory[0]) {
                	//Feeds and Processes each file in a directory if directory is selected
                    File dir = new File(inputPath[0]);
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".txt")); // Adjust filter as needed
                    if (files != null) {
                        for (File file : files) {
                            File outputFile = new File(dir, "(Pwnda)" + file.getName());
                            fileProcessor.processFile(file.getAbsolutePath(), outputFile.getAbsolutePath()); //gives each file over to file processor
                        }
                        JOptionPane.showMessageDialog(frame, "Processing complete for all files in directory!");
                    }
                } else {
                	//Processes a singular file if selected
                    File inputFile = new File(inputPath[0]);
                    File outputFile = new File(inputFile.getParent(), "(Pwnda)" + inputFile.getName());
                    fileProcessor.processFile(inputFile.getAbsolutePath(), outputFile.getAbsolutePath()); //gives file over to file processor
                    JOptionPane.showMessageDialog(frame, "Processing complete for file!");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
