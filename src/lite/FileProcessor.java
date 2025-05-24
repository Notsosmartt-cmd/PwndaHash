// FileProcessor.java (Revised)
package lite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class FileProcessor {
    // Stores UTF-8 code points of password characters
    private final List<Integer> utf8Values;
    
    // Cryptographic parameters
    private static final int SALT_LENGTH = 16;  // 128-bit salt for PBKDF2
    private static final int ITERATIONS = 10000; // Iteration count for key stretching

    public FileProcessor(List<Integer> utf8Values) {
        this.utf8Values = utf8Values;
    }

    public void processFile(String inputFile, String outputFile) {
        long startTime = System.currentTimeMillis(); // Start timer
        
        try {
            // ========== SALT GENERATION ========== //
            // Generate cryptographically secure random salt
            byte[] salt = new byte[SALT_LENGTH];
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            secureRandom.nextBytes(salt);

            // ========== KEY DERIVATION ========== //
            // Convert password from UTF-8 code points to char array
            char[] passwordChars = utf8Values.stream()
                .map(c -> (char) c.intValue())
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString().toCharArray();

            // Derive cryptographic seed using PBKDF2
            PBEKeySpec spec = new PBEKeySpec(
                passwordChars, 
                salt, 
                ITERATIONS, 
                256 // Key length in bits
            );
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] derivedSeed = skf.generateSecret(spec).getEncoded();

            // ========== RANDOM NUMBER GENERATION ========== //
            // Initialize PRNG with derived seed
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            prng.setSeed(derivedSeed);

            // ========== Initialize encryption state ========== //
            long bigSum = utf8Values.stream().mapToInt(Integer::intValue).sum();
            long adjustedSum = bigSum;
            Set<Integer> usedValues = new HashSet<>();

            
            // ========== FILE PROCESSING ========== //
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile), StandardCharsets.UTF_8);
                 BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8)) {

                // Write salt as hex string at file start
                writer.write(HexFormat.of().formatHex(salt));

                int charValue;
                
                // Process each character in input file
                while ((charValue = reader.read()) != -1) {
                    // Reset state when adjustment sum is exhausted
                    if (adjustedSum <= 0) {
                        adjustedSum = bigSum;
                        usedValues.clear();
                    }

                    // ========== INDEX SELECTION ========== //
                    // Generate Gaussian-distributed index (biased toward center)
                    int passwordIndex = (int) Math.abs(
                        prng.nextGaussian() * (utf8Values.size() / 2.0)
                    );
                    passwordIndex = Math.min(passwordIndex, utf8Values.size() - 1);
                    
                    // Get password character value
                    int passwordCharValue = utf8Values.get(passwordIndex);

                    // Update adjustment sum tracking
                    if (usedValues.add(passwordCharValue)) {
                        adjustedSum -= passwordCharValue;
                    }

                    // ========== ENCRYPTION CALCULATION ========== //
                    long rawValue = (long) charValue + passwordCharValue + adjustedSum;
                    // Ensure valid Unicode code point (0x0000-0x10FFFF)
                    int encryptedValue = (rawValue > 0x10FFFF) 
                        ? (int) (rawValue % 0x110000L) 
                        : (int) rawValue;

                    // Write valid characters or replacement symbol
                    if (Character.isValidCodePoint(encryptedValue)) {
                        writer.write(Character.toChars(encryptedValue));
                    } else {
                        writer.write('\uFFFD'); // Unicode replacement character
                    }
                }
                
                // Calculate and display processing time
                long endTime = System.currentTimeMillis();
                double elapsedSeconds = (endTime - startTime) / 1000.0;
                
                System.out.println("Encrypted file generated: " + outputFile);
                System.out.printf("Processing time: %.2f seconds%n", elapsedSeconds);
                
            } // End of try-with-resources
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

