// FileProcessor.java
package decrypt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import processors.Utf8Utils;


public class Decrypter {
    private final List<Integer> utf8Values;

    public Decrypter(List<Integer> utf8Values) {
        this.utf8Values = utf8Values;
    }

    public void processFile(String inputFile, String outputFile) {
    	
    	//Sum of all password characters
    	final long bigSum = utf8Values.stream().mapToInt(Integer::intValue).sum(); //streams can be used in parrellelization
    	long adjustedSum = bigSum; // Mirror encryption's adjustedSum
    	
    	Set<Integer> usedValues = new HashSet<>(); // Track used indexes like encryption
    	
        try (BufferedReader reader =Files.newBufferedReader(Paths.get(inputFile), StandardCharsets.UTF_8);
        	BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8)) {

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
                        //part 1
                    	int index = Integer.parseInt(parts[0]);
                        
                        // Get full code point (should work for surrogate pairs) part 2
                        int secondCharValue = parts[1].codePointAt(0);

                        // Reset logic (MUST mirror encryption)
                        if (adjustedSum <= 0) {
                            adjustedSum = bigSum;
                            usedValues.clear();
                        }
                        
                        // Validate index
                        if (index < 0 || index >= utf8Values.size()) {
                            writer.write("Wrong Password.\n");
                            continue;
                        }
                
                        int passwordUTF = utf8Values.get(index);
                
                     // Update adjustedSum during decryption (MIRROR encryption)
                        if (usedValues.add(passwordUTF)) { // Track index, not value
                            adjustedSum -= passwordUTF;
                        }
                        
                        long rawDecrypted = (long)secondCharValue - (long)passwordUTF - adjustedSum;
                        
                     // Apply reverse wrapping using the same modulus
                        long decryptedValue = (rawDecrypted % 0x110000L + 0x110000L) % 0x110000L; 
                        int plainCharValue = (int) decryptedValue;
                        
                
                   
                        
                        // Validate and convert to proper UTF-8 character 
                        if (Character.isValidCodePoint(plainCharValue)) {
                            // Convert code point to char array (handles surrogate pairs if needed)
                            char[] chars = Character.toChars(plainCharValue);
                         // Write as UTF-8
                            writer.write(new String(chars));
                        } else {
                            // Fallback for invalid code points '\uFFFD' is a replacement character
                            writer.write('\uFFFD');
                        }
                        
                        
                        
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
        System.out.println("bigSum: " + bigSum);
    }
}
