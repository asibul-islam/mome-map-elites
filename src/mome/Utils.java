package mome;

public class Utils {

    /** Clamp a value into [min, max]. */
    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Uniform random double in [min, max]. */
    public static double randUniform(java.util.Random rng, double min, double max) {
        return min + rng.nextDouble() * (max - min);
    }

    /** Map a descriptor value to a bin index given [lower, upper] and number of bins. */
    public static int toBinIndex(double value, double lower, double upper, int bins) {
        double width = (upper - lower) / bins;
        int idx = (int) ((value - lower) / width);
        if (idx < 0) idx = 0;
        if (idx >= bins) idx = bins - 1;
        return idx;
    }

    /** Returns true if a dominates b in minimization (strictly better in at least one, no worse in all). */
    public static boolean dominates(double[] a, double[] b) {
        boolean strictlyBetterInAtLeastOne = false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i]) return false;               // worse in objective i → not dominating
            if (a[i] < b[i]) strictlyBetterInAtLeastOne = true;
        }
        return strictlyBetterInAtLeastOne;
    }
}
