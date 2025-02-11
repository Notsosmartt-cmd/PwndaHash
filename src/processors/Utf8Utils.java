package processors;

import java.util.ArrayList;
import java.util.List;

public class Utf8Utils {
    public static List<Character> convertToCharacterArrayList(String password) {
        List<Character> charList = new ArrayList<>();
        
        for (char ch : password.toCharArray()) {
            charList.add(ch);
        }

        return charList;
    }
}
