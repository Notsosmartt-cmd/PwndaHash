// FileProcessor.java
package decrypt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import processors.Utf8Utils;


public class Decrypter {
    private final List<Integer> utf8Values;

    public Decrypter(List<Integer> utf8Values) {
        this.utf8Values = utf8Values;
    }

    public void processFile(String inputFile, String outputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

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

                        // Validate index
                        if (index < 0 || index >= utf8Values.size()) {
                            writer.write("Wrong Password.\n");
                            continue;
                        }

                        // Get the last value from the utf8Values ArrayList
                        int salt = utf8Values.get(utf8Values.size() - 1);

                        int value = utf8Values.get(index);
                        int passwordUTF = value + salt;

                        int secondUtf8Value = (int) secondChar;
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

        System.out.println("Output file has been written: " + outputFile);
    }
}
