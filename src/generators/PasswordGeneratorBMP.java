package generators;

import java.io.*;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PasswordGeneratorBMP {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        int passwordLength = 512; // Fixed length (or use random length)
        String password = generatePassword(passwordLength);
        System.out.println("Generated Password:\n" + password);

        String zipFilename = "GeneratedPassword.zip";
        String textFilename = "GeneratedPassword.txt";
        try {
            zipPasswordToFile(zipFilename, textFilename, password);
            System.out.println("Password written to zip: " + zipFilename);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static String generatePassword(int length) {
        StringBuilder allCharsBuilder = new StringBuilder();
        
        // Include valid BMP characters (U+0020 to U+D7FF and U+E000 to U+FFFD)
        for (int i = 32; i <= 0xD7FF; i++) {
            if (isValidBMPChar(i)) {
                allCharsBuilder.append((char) i);
            }
        }
        for (int i = 0xE000; i <= 0xFFFD; i++) { // Stop at U+FFFD to exclude non-characters
            if (isValidBMPChar(i)) {
                allCharsBuilder.append((char) i);
            }
        }
        
        String allChars = allCharsBuilder.toString();
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        return shuffleString(password.toString(), random);
    }

    // Check if a code point is valid for inclusion
    private static boolean isValidBMPChar(int codePoint) {
        return Character.isDefined(codePoint) &&
               !Character.isSurrogate((char) codePoint) &&
               !isNonCharacter(codePoint);
    }

    // Check for Unicode non-characters (e.g., U+FFFE, U+FFFF)
    private static boolean isNonCharacter(int codePoint) {
        return (codePoint >= 0xFDD0 && codePoint <= 0xFDEF) || 
               (codePoint & 0xFFFE) == 0xFFFE;
    }

    // Shuffle the password for randomness
    private static String shuffleString(String input, SecureRandom random) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[index];
            characters[index] = temp;
        }
        return new String(characters);
    }

    // Write password to a ZIP file
    private static void zipPasswordToFile(String zipFilename, String textFilename, String password) 
            throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilename))) {
            zos.putNextEntry(new ZipEntry(textFilename));
            zos.write(password.getBytes());
            zos.closeEntry();
        }
    }
}