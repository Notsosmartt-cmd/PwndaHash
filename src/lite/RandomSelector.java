// RandomSelector.java
package lite;

import java.util.List;
import java.util.Random;

public class RandomSelector {
    public static int selectGaussianIndex(List<Character> values) {
        Random random = new Random();
        double mean = (values.size() - 1) / 2.0;
        double stdDev = values.size() / 4.0;
        int selectedIndex;

        do {
            double gaussian = random.nextGaussian() * stdDev + mean;
            selectedIndex = (int) Math.round(gaussian);
        } while (selectedIndex < 0 || selectedIndex >= values.size());

        return selectedIndex;
    }
}