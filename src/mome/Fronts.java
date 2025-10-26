package mome;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/** Helpers for true (or approximated) Pareto fronts and metrics (2 objectives). */
public class Fronts {

    // ---------------------------------------------------------------------
    // ========== ANALYTIC FRONTS (closed form) ============================
    // ---------------------------------------------------------------------

    /** ZDT1 (and ZDT4) true front: f2 = 1 - sqrt(f1),  f1 in [0,1]. */
    public static double[][] zdt1TrueFront(int nPoints) {
        List<double[]> pts = new ArrayList<>(nPoints);
        for (int i = 0; i < nPoints; i++) {
            double f1 = (double) i / (nPoints - 1);
            double f2 = 1.0 - Math.sqrt(f1);
            pts.add(new double[]{f1, f2});
        }
        return pts.toArray(new double[0][0]);
    }

    /** ZDT6 true front: f2 = 1 - f1^2,  f1 in [0,1]. */
    public static double[][] zdt6TrueFront(int nPoints) {
        List<double[]> pts = new ArrayList<>(nPoints);
        for (int i = 0; i < nPoints; i++) {
            double f1 = (double) i / (nPoints - 1);
            double f2 = 1.0 - f1 * f1;
            pts.add(new double[]{f1, f2});
        }
        return pts.toArray(new double[0][0]);
    }

    /**
     * ZDT3 true front (piecewise curve). We sample f1 on each allowed segment.
     * Segments from the original definition:
     * [0.0, 0.0830015349], [0.1822287280, 0.2577623634],
     * [0.4093136748, 0.4538821041], [0.6183967944, 0.6525117038],
     * [0.8233317983, 0.8518328654]
     */
    public static double[][] zdt3TrueFront(int pointsPerSegment) {
        double[][] segs = {
                {0.0, 0.0830015349},
                {0.1822287280, 0.2577623634},
                {0.4093136748, 0.4538821041},
                {0.6183967944, 0.6525117038},
                {0.8233317983, 0.8518328654}
        };
        List<double[]> pts = new ArrayList<>();
        for (double[] seg : segs) {
            for (int i = 0; i < pointsPerSegment; i++) {
                double f1 = seg[0] + (seg[1] - seg[0]) * i / (pointsPerSegment - 1);
                double f2 = 1 - Math.sqrt(f1) - f1 * Math.sin(10 * Math.PI * f1);
                pts.add(new double[]{f1, f2});
            }
        }
        return pts.toArray(new double[0][0]);
    }

    /**
     * Schaffer N.1 true front (bi-objective, 1 variable).
     * f1 = x^2, f2 = (x-2)^2. The Pareto set is x in [0,2].
     * We parametrize by x ∈ [0,2] and map to (f1,f2).
     */
    public static double[][] schafferN1TrueFront(int nPoints) {
        List<double[]> pts = new ArrayList<>(nPoints);
        for (int i = 0; i < nPoints; i++) {
            double x = 2.0 * i / (nPoints - 1);   // x in [0,2]
            double f1 = x * x;
            double f2 = (x - 2.0) * (x - 2.0);
            pts.add(new double[]{f1, f2});
        }
        return pts.toArray(new double[0][0]);
    }

    /**
     * Fonseca–Fleming true front (2 objectives, typically n=3 variables).
     * For PF, set all xi = t, with t ∈ [-1/√n, 1/√n].
     * Then:
     *   f1 = 1 - exp(-n (t - 1/√n)^2)
     *   f2 = 1 - exp(-n (t + 1/√n)^2)
     */
    public static double[][] fonsecaFlemingTrueFront(int nPoints, int n) {
        double invSqrtN = 1.0 / Math.sqrt(n);
        List<double[]> pts = new ArrayList<>(nPoints);
        for (int i = 0; i < nPoints; i++) {
            double t = -invSqrtN + (2.0 * invSqrtN) * i / (nPoints - 1);
            double s1 = n * (t - invSqrtN) * (t - invSqrtN);
            double s2 = n * (t + invSqrtN) * (t + invSqrtN);
            double f1 = 1.0 - Math.exp(-s1);
            double f2 = 1.0 - Math.exp(-s2);
            pts.add(new double[]{f1, f2});
        }
        return pts.toArray(new double[0][0]);
    }

    // ---------------------------------------------------------------------
    // ========== APPROXIMATED FRONT (no simple closed form) ===============
    // ---------------------------------------------------------------------

    /**
     * Kursawe approximate front via random sampling + global ND filter.
     * Bounds: [-5,5]^n (n=3 typical). Increase samples for a tighter curve.
     */
    public static double[][] kursaweApproxFront(int n, int samples, long seed) {
        Random rng = new Random(seed);
        List<double[]> pts = new ArrayList<>(samples);

        // Random sampling in decision space
        for (int s = 0; s < samples; s++) {
            double[] x = new double[n];
            for (int i = 0; i < n; i++) {
                x[i] = -5.0 + 10.0 * rng.nextDouble();
            }
            double[] f = kursaweEval(x);
            pts.add(new double[]{f[0], f[1]});
        }

        // Global ND filter in objective space (minimization)
        List<double[]> nd = new ArrayList<>();
        for (int i = 0; i < pts.size(); i++) {
            double[] a = pts.get(i);
            boolean dominated = false;
            for (int j = 0; j < pts.size(); j++) {
                if (i == j) continue;
                double[] b = pts.get(j);
                if (dominates(b, a)) { dominated = true; break; }
            }
            if (!dominated) nd.add(a);
        }

        // Sort by f1 for plotting consistency
        nd.sort(Comparator.comparingDouble(p -> p[0]));
        return nd.toArray(new double[0][0]);
    }

    private static double[] kursaweEval(double[] x) {
        int n = x.length;
        double f1 = 0.0, f2 = 0.0;
        for (int i = 0; i < n - 1; i++) {
            f1 += -10.0 * Math.exp(-0.2 * Math.sqrt(x[i] * x[i] + x[i + 1] * x[i + 1]));
        }
        for (int i = 0; i < n; i++) {
            f2 += Math.pow(Math.abs(x[i]), 0.8) + 5.0 * Math.sin(Math.pow(x[i], 3));
        }
        return new double[]{f1, f2};
    }

    private static boolean dominates(double[] a, double[] b) {
        boolean betterInOne = false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i]) return false;       // minimization: worse in obj i
            if (a[i] < b[i]) betterInOne = true;
        }
        return betterInOne;
    }

    // ---------------------------------------------------------------------
    // ========== METRICS & IO ============================================
    // ---------------------------------------------------------------------

    /** Write 2D points (f1,f2) to CSV: header then rows. */
    public static void writeCSV(String path, double[][] points) {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("f1,f2\n");
            for (double[] p : points) fw.write(p[0] + "," + p[1] + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV: " + path, e);
        }
    }

    /** Euclidean distance in objective space. */
    public static double dist2(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** IGD: avg distance from each true PF point to nearest approx point. Lower is better. */
    public static double igd(double[][] truePF, double[][] approx) {
        if (truePF.length == 0 || approx.length == 0) return Double.POSITIVE_INFINITY;
        double sum = 0.0;
        for (double[] t : truePF) {
            double best = Double.POSITIVE_INFINITY;
            for (double[] a : approx) best = Math.min(best, dist2(t, a));
            sum += best;
        }
        return sum / truePF.length;
    }

    /** GD: avg distance from each approx point to nearest true PF point. Lower is better. */
    public static double gd(double[][] approx, double[][] truePF) {
        if (truePF.length == 0 || approx.length == 0) return Double.POSITIVE_INFINITY;
        double sum = 0.0;
        for (double[] a : approx) {
            double best = Double.POSITIVE_INFINITY;
            for (double[] t : truePF) best = Math.min(best, dist2(a, t));
            sum += best;
        }
        return sum / approx.length;
    }
}
