package mome;

public class BenchmarkFunctionsMO {

    // =====================
    // EASY BENCHMARKS
    // =====================

    /** Schaffer N.1 (2 objectives, 1 variable) */
    public static BenchmarkFunctionMO schafferN1() {
        return x -> {
            double f1 = x[0] * x[0];
            double f2 = (x[0] - 2) * (x[0] - 2);
            return new double[]{f1, f2};
        };
    }

    /** Fonseca–Fleming (2 objectives, typically 3 variables) */
    public static BenchmarkFunctionMO fonsecaFleming(int n) {
        return x -> {
            double sum1 = 0, sum2 = 0;
            for (int i = 0; i < n; i++) {
                sum1 += Math.pow(x[i] - 1.0 / Math.sqrt(n), 2);
                sum2 += Math.pow(x[i] + 1.0 / Math.sqrt(n), 2);
            }
            double f1 = 1 - Math.exp(-sum1);
            double f2 = 1 - Math.exp(-sum2);
            return new double[]{f1, f2};
        };
    }

    // =====================
    // MEDIUM BENCHMARKS
    // =====================

    /** ZDT1 (30 variables typical, convex front) */
    public static BenchmarkFunctionMO zdt1(int n) {
        return x -> {
            double f1 = x[0];
            double g = 1.0 + 9.0 / (n - 1) * sum(x, 1, n);
            double f2 = g * (1.0 - Math.sqrt(f1 / g));
            return new double[]{f1, f2};
        };
    }

    /** Kursawe (3 variables typical, non-convex) */
    public static BenchmarkFunctionMO kursawe(int n) {
        return x -> {
            double f1 = 0, f2 = 0;
            for (int i = 0; i < n - 1; i++) {
                f1 += -10 * Math.exp(-0.2 * Math.sqrt(x[i] * x[i] + x[i + 1] * x[i + 1]));
            }
            for (int i = 0; i < n; i++) {
                f2 += Math.pow(Math.abs(x[i]), 0.8) + 5 * Math.sin(Math.pow(x[i], 3));
            }
            return new double[]{f1, f2};
        };
    }

    // =====================
    // HARD BENCHMARKS
    // =====================

    /** ZDT3 (discontinuous front, 30 variables typical) */
    public static BenchmarkFunctionMO zdt3(int n) {
        return x -> {
            double f1 = x[0];
            double g = 1.0 + 9.0 / (n - 1) * sum(x, 1, n);
            double f2 = g * (1.0 - Math.sqrt(f1 / g) - (f1 / g) * Math.sin(10 * Math.PI * f1));
            return new double[]{f1, f2};
        };
    }

    // HARD: ZDT6 (30 variables typical, bounds [0,1])
    public static BenchmarkFunctionMO zdt6(int n) {
        return x -> {
            double x1 = x[0];
            double f1 = 1.0 - Math.exp(-4.0 * x1) * Math.pow(Math.sin(6.0 * Math.PI * x1), 6);
            double avg = 0.0;
            for (int i = 1; i < n; i++) avg += x[i];
            avg /= (n - 1);
            double g = 1.0 + 9.0 * Math.pow(avg, 0.25);
            double f2 = g * (1.0 - Math.pow(f1 / g, 2.0)); // h = 1 - (f1/g)^2
            return new double[]{f1, f2};
        };
    }

    // =====================
    // HELPER
    // =====================
    private static double sum(double[] arr, int start, int end) {
        double s = 0;
        for (int i = start; i < end; i++) s += arr[i];
        return s;
    }
}
