package decrypt;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DeEncrypt {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose an option to proceed:");
        System.out.println("1. Use a manual password.");
        System.out.println("2. Use a password from a ZIP file.");
        System.out.print("Enter your choice (1 or 2): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        String password = null;
        String zipFilePath = null;

        if (choice == 1) {
            System.out.println("Manual Password Used");
            password = "1234"; // Static manual password for simplicity
        } else if (choice == 2) {
            System.out.println("ZIP File Path Used");
            zipFilePath = "C:\\Users\\user\\eclipse-workspace\\ZPwndaHashComplete\\GeneratedPassword.zip"; // Path to the ZIP file
            password = loadPasswordFromZip(zipFilePath);
            if (password == null) {
                System.out.println("Failed to load password from ZIP file. Exiting...");
                scanner.close();
                return;
            }
        } else {
            System.out.println("Invalid choice. Exiting...");
            scanner.close();
            return;
        }

        scanner.close();

        // File paths
        String inputFilePath = "output.txt"; // Replace with your input file
        String outputFilePath = "result.txt"; // Output file path

        // Password to UTF-8 values
        List<Integer> passwordValue = new ArrayList<>();
        for (int i = 0; i < password.length(); i++) {
            int value = password.codePointAt(i);
            passwordValue.add(value);

            if (Character.isSupplementaryCodePoint(value)) {
                i++; // Skip the second char in the surrogate pair
            }
        }

        // Convert the List<Integer> to an int array
        int[] primitivePasswordArray = passwordValue.stream().mapToInt(Integer::intValue).toArray();
        int salt = primitivePasswordArray[primitivePasswordArray.length - 1];
        System.out.println("Last element of the array (salt): " + salt);

        // Process the input file
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove extra spaces

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // Split the line into two parts
                String[] parts = line.split("/");
                if (parts.length == 2) {
                    try {
                        int index = Integer.parseInt(parts[0]);
                        char secondChar = parts[1].charAt(0);

                        if (index < 0 || index >= password.length()) {
                            writer.write("Wrong Password.\n");
                            continue;
                        }

                        char refChar = password.charAt(index);
                        int passwordUTF = refChar + (char) salt;

                        int secondUtf8Value = secondChar;
                        int result = Math.abs(passwordUTF - secondUtf8Value);

                        writer.write((char) result);
                    } catch (NumberFormatException e) {
                        writer.write("Invalid index format in line: " + line + "\n");
                    }
                } else {
                    writer.write("Invalid line format: " + line + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        System.out.println("result.txt has been written");
    }

    private static String loadPasswordFromZip(String zipFilePath) {
        if (zipFilePath == null || zipFilePath.isEmpty()) return null;

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            ZipEntry entry = zipFile.entries().nextElement(); // Assume one file
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)))) {
                StringBuilder passwordBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    passwordBuilder.append(line);
                }
                return passwordBuilder.toString();
            }
        } catch (IOException e) {
            System.out.println("Failed to extract password from ZIP file: " + e.getMessage());
            return null;
        }
    }
}
