// Decrypter.java
package decrypt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DecrypterLite {
    private final List<Integer> utf8Values;
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;

    public DecrypterLite(List<Integer> utf8Values) {
        this.utf8Values = utf8Values;
    }

    public void processFile(String inputFile, String outputFile) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile), StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8)) {
            
            // Read salt from header
            char[] saltChars = new char[SALT_LENGTH * 2]; // Hex characters
            reader.read(saltChars, 0, saltChars.length);
            byte[] salt = HexFormat.of().parseHex(new String(saltChars));

            // Derive seed using password and salt
            char[] passwordChars = utf8Values.stream()
                .map(c -> (char) c.intValue())
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString().toCharArray();
            
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] derivedSeed = skf.generateSecret(spec).getEncoded();

            // Initialize PRNG with derived seed
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            prng.setSeed(derivedSeed);

            // Initialize decryption state
            long bigSum = utf8Values.stream().mapToInt(Integer::intValue).sum();
            long adjustedSum = bigSum;
            Set<Integer> usedValues = new HashSet<>();
            int codePoint;

            // Process continuous UTF-8 stream
            while ((codePoint = reader.read()) != -1) {
                if (adjustedSum <= 0) {
                    adjustedSum = bigSum;
                    usedValues.clear();
                }

                // Generate same Gaussian index as encryption
                int passwordIndex = (int) Math.abs(prng.nextGaussian() * (utf8Values.size() / 2.0));
                passwordIndex = Math.min(passwordIndex, utf8Values.size() - 1);
                
                if (passwordIndex < 0 || passwordIndex >= utf8Values.size()) {
                    throw new IOException("Invalid password index - likely wrong password");
                }

                int passwordCharValue = utf8Values.get(passwordIndex);
                
                // Mirror encryption's sum adjustment
                if (usedValues.add(passwordCharValue)) {
                    adjustedSum -= passwordCharValue;
                }

                // Reverse encryption math
                long rawDecrypted = (long) codePoint - passwordCharValue - adjustedSum;
                int decryptedValue = (int) ((rawDecrypted % 0x110000L + 0x110000L) % 0x110000L);

                // Validate and write character
                if (Character.isValidCodePoint(decryptedValue)) {
                    writer.write(Character.toChars(decryptedValue));
                } else {
                    writer.write('\uFFFD');
                }
            }

            System.out.println("Successfully decrypted file: " + outputFile);

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Decryption error: " + e.getMessage());
            // Consider deleting partially decrypted file here for security
        }
    }
}

