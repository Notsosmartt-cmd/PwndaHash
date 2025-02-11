// FileProcessor.java
package lite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor {
    private final List<Character> utf8Values;

    public FileProcessor(List<Character> utf8Values) {
        this.utf8Values = utf8Values;
    }

    public void processFile(String inputFile, String outputFile) {
    	    try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile));
    	         BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

    	        int charValue;
    	        while ((charValue = reader.read()) != -1) {
    	            // Process the character
    	            int fileCharValue = charValue;

    	            // Use the Gaussian-based random selection method
    	            int passwordIndex = RandomSelector.selectGaussianIndex(utf8Values);
    	            int passwordCharValue = utf8Values.get(passwordIndex);

    	            // Get the last value from the utf8Values ArrayList
    	            int salt = utf8Values.get(utf8Values.size() - 1);

    	            int encryptedValue = fileCharValue + passwordCharValue + salt;
    	            char encryptedChar = (char) encryptedValue;

    	            // Write the encrypted character to the output file
    	            writer.write(passwordIndex + "/" + encryptedChar);
    	            writer.newLine();
    	        }

    	        System.out.println("Encrypted values written to file: " + outputFile);

    	    } catch (IOException e) {
    	        System.err.println("Error: " + e.getMessage());
    	    }
    }
}