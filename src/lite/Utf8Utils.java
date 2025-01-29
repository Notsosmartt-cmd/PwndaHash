// Utf8Utils.java
package lite;

import java.util.ArrayList;
import java.util.List;

public class Utf8Utils {
    public static List<Integer> convertToUtf8ArrayList(String password) {
        List<Integer> utf8Values = new ArrayList<>();

        for (int i = 0; i < password.length(); i++) {
            int value = password.codePointAt(i); // Get code point
            utf8Values.add(value); // Add to the list
            if (Character.isSupplementaryCodePoint(value)) {
                i++; // Skip the second char in the surrogate pair
            }
        }

        return utf8Values;
    }
}
