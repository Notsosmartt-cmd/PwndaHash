package processors;

import java.util.ArrayList;
import java.util.List;

public class Utf8Utils {
	public static List<Integer> convertToCodePointArrayList(String input) {
	    List<Integer> utf8Values = new ArrayList<>();
	    
	    // Directly collect code points into the ArrayList
        input.codePoints().forEach(utf8Values::add);  //turns characters like 'A' into raw integer for their code point '65'
        
        return utf8Values;
	}
}