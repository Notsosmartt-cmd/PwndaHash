// FileProcessor.java
package lite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileProcessor {
    private final List<Integer> utf8Values;

    public FileProcessor(List<Integer> utf8Values) {
        this.utf8Values = utf8Values;
    }

    public void processFile(String inputFile, String outputFile) {
    	
    	//Sum of all password characters
    	final long bigSum = utf8Values.stream().mapToInt(Integer::intValue).sum(); //streams can be used in parrellelization
    	//consider wrapping because big sum
    	 System.out.println("bigSum: " + bigSum);
    	 
    	long adjustedSum = bigSum;

    	Set<Integer> usedValues = new HashSet<>();
    	
    	//Proccess each plain Text character in file
    	    try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile),StandardCharsets.UTF_8);
    	    	BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8))
    	    	{
    	    	
    	        int charValue;
    	        while ((charValue = reader.read()) != -1) { 
    	        	// Process the character
    	            int fileCharValue = charValue;
    	            //Check if adjustedSum is empty and reset hashset if so
    	            if (adjustedSum <= 0) {
    	            	adjustedSum = bigSum;
    	            	usedValues.clear();
    	            }
    	            
    	            // Use the Gaussian-based random selection method
    	            int passwordIndex = RandomSelector.selectGaussianIndex(utf8Values);
    	            int passwordCharValue = utf8Values.get(passwordIndex);
    	            
    	            //subtract random index from big sum if it hasn't been used to create an adjusted sum
    	            if(usedValues.add(passwordCharValue)) {
    	            	adjustedSum -= passwordCharValue;
    	            }
    	            
    	            long rawValue = (long) fileCharValue + (long) passwordCharValue + adjustedSum;
    	            
    	            // Wrap overflow back around
    	            int encryptedValue = (rawValue > 0x10FFFF) 
    	                    ? (int) (rawValue % 0x110000L)  
    	                    : (int) rawValue;
    	                

    	         // Validate and convert to proper UTF-8 character
                    if (Character.isValidCodePoint(encryptedValue)) {
                        // Convert code point to char array (handles surrogate pairs if needed)
                        char[] chars = Character.toChars(encryptedValue);
                     
                        // Write as UTF-8
                        writer.write(passwordIndex + "/" + new String(chars));
                        
                    } else {
                        // Fallback for invalid code points '\uFFFD' is a replacement character
                        writer.write(passwordIndex + "/" + '\uFFFD');    
                    }
    	            writer.newLine();
           
    	        }

    	        System.out.println("Encrypted values written to file: " + outputFile);
    	       


    	    } catch (IOException e) {
    	        System.err.println("Error: " + e.getMessage());
    	    }
    }
}