package generators;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PasswordGeneratorUTF8 {
    private static final int MAX_CODE_POINT = 0x10FFFF; // Maximum valid Unicode code point
    private static List<Integer> validCodePoints;

    static {
        // Precompute all valid UTF-8 code points
        validCodePoints = new ArrayList<>();
        for (int codePoint = 0; codePoint <= MAX_CODE_POINT; codePoint++) {
            if (isValidCodePoint(codePoint)) {
                validCodePoints.add(codePoint);
            }
        }
    }

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        int passwordLength = 512; // Fixed length (or randomize)
        String password = generatePassword(passwordLength);
        System.out.println("Generated Password:\n" + password);

        String zipFilename = "GeneratedPasswordUTF8.zip";
        String textFilename = "GeneratedPasswordUTF8.txt";
        try {
            zipPasswordToFile(zipFilename, textFilename, password);
            System.out.println("Password written to zip: " + zipFilename);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        List<Integer> selectedCodePoints = new ArrayList<>();

        // Select random code points
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(validCodePoints.size());
            selectedCodePoints.add(validCodePoints.get(randomIndex));
        }

        // Shuffle code points for additional randomness
        Collections.shuffle(selectedCodePoints, random);

        // Build the password string
        StringBuilder password = new StringBuilder();
        for (int codePoint : selectedCodePoints) {
            password.appendCodePoint(codePoint);
        }

        return password.toString();
    }

    private static boolean isValidCodePoint(int codePoint) {
        return Character.isDefined(codePoint) && // Only defined characters
               !isSurrogate(codePoint) &&       // Exclude surrogate pairs
               !isNonCharacter(codePoint);       // Exclude non-characters
    }

    private static boolean isSurrogate(int codePoint) {
        return codePoint >= Character.MIN_SURROGATE && 
               codePoint <= Character.MAX_SURROGATE;
    }

    private static boolean isNonCharacter(int codePoint) {
        return (codePoint >= 0xFDD0 && codePoint <= 0xFDEF) || 
               (codePoint & 0xFFFE) == 0xFFFE;
    }

    private static void zipPasswordToFile(String zipFilename, String textFilename, String password) 
            throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilename))) {
            zos.putNextEntry(new ZipEntry(textFilename));
            zos.write(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }
}