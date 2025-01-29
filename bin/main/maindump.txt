package gui;

import javax.swing.JFrame;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int x = 10;
        int y = 10;
        int width = 700;
        int height = 600;
       
        // Create a JFrame object to act as the main window for the game
        JFrame obj = new JFrame();
        
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose an option to start the game:");
        System.out.println("1. Use a manual password.");
        System.out.println("2. Use a password from a ZIP file.");
        System.out.print("Enter your choice (1 or 2): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        String password = null;
        String zipFilePath = null;
        Gameplay gamePlay;

        if (choice == 1) {
            System.out.println("Manual Password Used");
            password = "123"; // Static manual password for simplicity
            gamePlay = new Gameplay(password, null);
        } else if (choice == 2) {
            System.out.println("ZIP File Path Used");
            zipFilePath = "C:\\Users\\user\\eclipse-workspace\\ZPwndaHashComplete\\GeneratedPassword.zip"; // Path to the ZIP file
            gamePlay = new Gameplay(null, zipFilePath); // Password will be loaded dynamically
        } else {
            System.out.println("Invalid choice. Exiting...");
            scanner.close();
            return;
        }

        scanner.close();

       

        // Set up the game window
        obj.setBounds(x, y, width, height); // Position and size of the window
        obj.setTitle("Breakout Ball"); // Set the title of the window
        obj.setResizable(true); // Allow the window to be resized
        obj.setVisible(true); // Make the window visible
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close application on window close

        // Add the Gameplay class to the JFrame as the main content
        obj.add(gamePlay);
    }
}
