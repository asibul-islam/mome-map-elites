package mome;

/**
 * Multi-objective benchmark function interface.
 * Implementations should return an array of objective values (to MINIMIZE).
 * Example: for 2 objectives, return new double[]{f1, f2}.
 */
@FunctionalInterface
public interface BenchmarkFunctionMO {

    /**
     * Evaluate the decision vector x and return objective values.
     * Convention: all objectives are MINIMIZED.
     *
     * @param x decision vector (genome)
     * @return objective values, e.g., [f1, f2, ...]
     */
    double[] evaluate(double[] x);
}
