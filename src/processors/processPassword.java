package processors;

import java.util.ArrayList;
import java.util.List;

public class processPassword {

    /**
     * Processes the password into a list of code point values represented as strings.
     * It ensures that surrogate pairs are handled properly.
     */
    public static List<String> processPassword(String password) {
        List<String> charData = new ArrayList<>();
        for (int i = 0; i < password.length(); i++) {
            int codePoint = password.codePointAt(i);
            // Convert the code point to a string (you could also store the int if you prefer)
            charData.add(Integer.toString(codePoint));
            // If this is a supplementary character, skip the next char as it is part of the pair
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++;
            }
        }
        return charData;
    }
}
