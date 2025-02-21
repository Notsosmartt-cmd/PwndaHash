// Main.java
package main;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import javax.swing.*;

public class PwndaHashMain extends JFrame {
    private final int WIDTH = 700;
    private final int HEIGHT = 600;
    private final String DEFAULT_INPUT_FILE = "No input file selected.";

    public PwndaHashMain() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("PwndaHash Game");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // Password source selection
        JRadioButton manualPasswordRadio = new JRadioButton("Manual Password");
        JRadioButton zipPasswordRadio = new JRadioButton("Password from ZIP");
        
        ButtonGroup passwordGroup = new ButtonGroup();
        passwordGroup.add(manualPasswordRadio);
        passwordGroup.add(zipPasswordRadio);
        manualPasswordRadio.setSelected(true);

        manualPasswordRadio.setBounds(20, 20, 150, 25);
        zipPasswordRadio.setBounds(180, 20, 150, 25);
        add(manualPasswordRadio);
        add(zipPasswordRadio);

        // Password input components
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 80, 25);
        add(passwordLabel);

        JTextField passwordField = new JTextField();
        passwordField.setBounds(100, 60, 200, 25);
        add(passwordField);

        // ZIP file components
        JButton zipFileButton = new JButton("Select ZIP File");
        zipFileButton.setBounds(100, 60, 200, 25);
        zipFileButton.setVisible(false);
        add(zipFileButton);

        JLabel zipFileLabel = new JLabel("No ZIP file selected");
        zipFileLabel.setBounds(310, 60, 250, 25);
        add(zipFileLabel);

        // Input file components
        JButton inputFileButton = new JButton("Select Input File");
        inputFileButton.setBounds(20, 100, 150, 25);
        add(inputFileButton);

        JLabel inputFileLabel = new JLabel(DEFAULT_INPUT_FILE);
        inputFileLabel.setBounds(180, 100, 300, 25);
        add(inputFileLabel);

        // Game controls
        JButton startButton = new JButton("Start Game");
        startButton.setBounds(200, 150, 150, 30);
        add(startButton);

        // Radio button listeners
        manualPasswordRadio.addActionListener(e -> {
            passwordField.setVisible(true);
            zipFileButton.setVisible(false);
            zipFileLabel.setText("No ZIP file selected");
        });

        zipPasswordRadio.addActionListener(e -> {
            passwordField.setVisible(false);
            zipFileButton.setVisible(true);
        });

        // ZIP file selection
        zipFileButton.addActionListener(e -> selectZipFile(zipFileLabel));

        // Input file selection
        inputFileButton.addActionListener(e -> selectInputFile(inputFileLabel));

        // Start game button
        startButton.addActionListener(e -> startGame(
            manualPasswordRadio.isSelected() ? passwordField.getText() : null,
            zipPasswordRadio.isSelected() ? zipFileLabel.getText() : null,
            inputFileLabel.getText()
        ));
        
     // Home Button
        JButton homeButton = new JButton("Home");
        homeButton.setBounds(450, 400, 100, 30);
        add(homeButton);

        // Home button functionality (for now, it just resets the screen)
        homeButton.addActionListener(e -> {
            dispose(); // Close the current window
            Launcher.main(null); // Go back to the main launcher
            //new Main(); // Reopen a fresh Main menu
        });
        

        setVisible(true);
    }

    private void selectZipFile(JLabel zipFileLabel) {
        FileDialog fileDialog = new FileDialog(this, "Select ZIP File", FileDialog.LOAD);
        fileDialog.setVisible(true);
    
        String directory = fileDialog.getDirectory();
        String file = fileDialog.getFile();
    
        if (directory != null && file != null) {
            zipFileLabel.setText(directory + file);
        }
    }
    
    private void selectInputFile(JLabel inputFileLabel) {
        FileDialog fileDialog = new FileDialog(this, "Select Input File", FileDialog.LOAD);
        fileDialog.setVisible(true);
    
        String directory = fileDialog.getDirectory();
        String file = fileDialog.getFile();
    
        if (directory != null && file != null) {
            inputFileLabel.setText(directory + file);
        }
    }
    

    public void startGame(String manualPassword, String zipFilePath, String inputFilePath) {
       
    	try {
            String password = validateAndGetPassword(manualPassword, zipFilePath);
            startGameWindow(password, inputFilePath);
        } catch (IllegalArgumentException | IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private String validateAndGetPassword(String manualPassword, String zipFilePath) throws IOException {
        if (manualPassword != null && !manualPassword.isEmpty()) {
            return manualPassword;
        }
        
        if (zipFilePath == null || zipFilePath.equals("No ZIP file selected")) {
            throw new IllegalArgumentException("Please select a valid file for the password!");
        }
    
        File file = new File(zipFilePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + zipFilePath);
        }
    
        if (isZipFile(file)) {
            return loadPasswordFromZip(zipFilePath);
        } else {
            return loadPasswordFromFile(file);
        }
    }
    
    private boolean isZipFile(File file) {
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file)) {
            return true; // If no exception is thrown, it's a valid ZIP file
        } catch (IOException e) {
            return false; // Not a valid ZIP file
        }
    }
    
    private String loadPasswordFromFile(File file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            return reader.readLine(); // Return the first line as the password
        }
    }

    private String loadPasswordFromZip(String zipFilePath) throws IOException {
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            throw new IOException("ZIP file not found!");
        }
        
        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(zipFile)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zf.entries();
            if (!entries.hasMoreElements()) {
                throw new IOException("ZIP file is empty!");
            }
            
            java.util.zip.ZipEntry entry = entries.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(entry)))) {
                return reader.readLine();
            }
        }
    }

    private void startGameWindow(String password, String inputFilePath) {
        JFrame gameFrame = new JFrame("PwndaHash Game");
        gameFrame.setSize(WIDTH, HEIGHT);
        gameFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        Gameplay gameplay = new Gameplay(password, inputFilePath);
        gameFrame.add(gameplay);
        gameFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new PwndaHashMain();
    }
}