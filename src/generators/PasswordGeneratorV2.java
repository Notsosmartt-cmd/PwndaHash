package generators;
//add gui
import java.io.*;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PasswordGeneratorV2 {
    public static void main(String[] args) {
        // Generate a random password length between 128 and 512
        SecureRandom random = new SecureRandom();
        int passwordLength = 128 + random.nextInt(512 - 128 + 1); // Random length in range [128, 512]

        // Generate the password
        String password = generatePassword(passwordLength);
        System.out.println("Generated Password:");
        System.out.println(password);

        // Write the password directly into a .txt file inside a .zip file
        String zipFilename = "GeneratedPassword.zip";
        String textFilename = "GeneratedPassword.txt";
        try {
            zipPasswordToFile(zipFilename, textFilename, password);
            System.out.println("Password written directly into zip file: " + zipFilename);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static String generatePassword(int length) {
        // Define the full range of printable ASCII characters
        StringBuilder allCharsBuilder = new StringBuilder();
        for (int i = 32; i <= 126; i++) {
            allCharsBuilder.append((char) i);
        }
        String allChars = allCharsBuilder.toString();

        // SecureRandom for better randomness
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Fill the password with random characters from the allowed set
        for (int i = 0; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password for additional randomness
        return shuffleString(password.toString(), random);
    }

    private static String shuffleString(String input, SecureRandom random) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Swap characters
            char temp = characters[i];
            characters[i] = characters[index];
            characters[index] = temp;
        }
        return new String(characters);
    }

    private static void zipPasswordToFile(String zipFilename, String textFilename, String password) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilename);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Create a new zip entry for the text file
            ZipEntry zipEntry = new ZipEntry(textFilename);
            zos.putNextEntry(zipEntry);

            // Write the password into the zip entry
            zos.write(password.getBytes());
            zos.closeEntry();
        }
    }
}
