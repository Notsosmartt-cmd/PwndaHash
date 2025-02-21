package generators;

import java.io.*;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PasswordGeneratorASCII {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        int passwordLength = 512; // Fixed length (or use random length)
        String password = generatePassword(passwordLength);
        System.out.println("Generated Password:\n" + password);

        String zipFilename = "GeneratedPasswordAscii.zip";
        String textFilename = "GeneratedPasswordAscii.txt";
        try {
            zipPasswordToFile(zipFilename, textFilename, password);
            System.out.println("Password written to zip: " + zipFilename);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static String generatePassword(int length) {
        StringBuilder allCharsBuilder = new StringBuilder();
        
        // Printable ASCII range (32-126)
        for (int i = 32; i <= 126; i++) {
            allCharsBuilder.append((char) i);
        }
        
        String allChars = allCharsBuilder.toString();
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        return shuffleString(password.toString(), random);
    }

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

    public static void zipPasswordToFile(String zipFilename, String textFilename, String password) 
            throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilename))) {
            zos.putNextEntry(new ZipEntry(textFilename));
            zos.write(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }
}