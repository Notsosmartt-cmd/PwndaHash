// Skewed RandomSelector.java
package lite;

import java.util.List;
import java.util.Random;

public class SkewedRandomSelector {
    private static final Random random = new Random();
    
    public static int selectGaussianIndex(List<Integer> values) {
        final int size = values.size();
        final double baseMean = (size - 1) / 2.0;
        final double baseStdDev = size / 4.0;
        
        // Secondary randomization: Skew direction and strength
        final double skewDirection = random.nextBoolean() ? 1 : -1; // Left or right skew
        final double skewStrength = random.nextDouble() * 0.5 + 0.3; // 30-80% strength
        
        // Create skewed distribution parameters
        final double skewedMean = baseMean + (skewDirection * baseStdDev * skewStrength);
        final double skewedStdDev = baseStdDev * (1 - (skewStrength * 0.5)); // Reduce spread for stronger skew

        int selectedIndex;
        do {
            // Primary Gaussian randomization with skewed parameters
            double gaussianValue = random.nextGaussian() * skewedStdDev + skewedMean;
            selectedIndex = (int) Math.round(gaussianValue);
        } while (selectedIndex < 0 || selectedIndex >= size);

        return selectedIndex;
    }
}