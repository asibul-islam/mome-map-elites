package mome;

import java.awt.Point;
import java.util.*;

/**
 * Multi-Objective MAP-Elites (MO-ME).
 * - Behavior space: first two genome values (x0, x1) → 2D grid of bins.
 * - Each bin stores a small PARETO SET (non-dominated individuals).
 * - Objectives are MINIMIZED.
 */
public class MOME {

    // ==== configuration ====
    private final int dimensions;           // number of decision variables
    private final int numObjectives;        // e.g., 2
    private final int binsPerDim;           // grid resolution per axis (e.g., 20)
    private final double lowerBound;        // same bound for all variables (simple version)
    private final double upperBound;
    private final int evaluationsPerGeneration;
    private final int generations;
    private final int initialRandom;        // how many random seeds at start
    private final double mutationSigma;     // std dev for Gaussian mutation
    private final int maxPerCell;           // limit Pareto set size per cell (keeps memory bounded)

    private final BenchmarkFunctionMO problem;   // multi-objective function (minimize)

    // ==== state ====
    private final Random rng = new Random();

    // Archive: cell → list of non-dominated individuals
    private final Map<Point, List<Individual>> archive = new HashMap<>();

    public MOME(int dimensions,
                int numObjectives,
                int binsPerDim,
                double lowerBound,
                double upperBound,
                int evaluationsPerGeneration,
                int generations,
                int initialRandom,
                double mutationSigma,
                int maxPerCell,
                BenchmarkFunctionMO problem) {

        this.dimensions = dimensions;
        this.numObjectives = numObjectives;
        this.binsPerDim = binsPerDim;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.evaluationsPerGeneration = evaluationsPerGeneration;
        this.generations = generations;
        this.initialRandom = initialRandom;
        this.mutationSigma = mutationSigma;
        this.maxPerCell = Math.max(1, maxPerCell);
        this.problem = problem;
    }

    /** Run the MO-ME process: seed → evolve for (generations × evalsPerGen). */
    public void run() {
        // 1) Seed with random individuals to populate some cells
        int seeds = Math.max(1, initialRandom);

        for (int i = 0; i < initialRandom; i++) {
            Individual ind = Individual.random(dimensions, lowerBound, upperBound, numObjectives);
            ind.objectives = problem.evaluate(ind.genome);
            insertIntoArchive(ind);
        }

        // 2) Evolution loop
        for (int gen = 0; gen < generations; gen++) {
            for (int ev = 0; ev < evaluationsPerGeneration; ev++) {
                Individual parent = selectParent();
                Individual child = mutate(parent);
                child.objectives = problem.evaluate(child.genome);
                insertIntoArchive(child);
            }
        }
    }

    // =========================
    // Selection / Variation
    // =========================

    /** Pick a parent from archive; if empty, make a random one. Uses best-of-k tournament for mild pressure. */
    private Individual selectParent()   {
        // If archive empty → random
        if (archive.isEmpty()) {
            return Individual.random(dimensions, lowerBound, upperBound, numObjectives);
        }

        // Build pool only from non-empty cell lists
        List<Individual> pool = new ArrayList<>();
        for (List<Individual> list : archive.values()) {
            if (list != null && !list.isEmpty()) {
                pool.addAll(list);
            }
        }

        // If pool empty → random (safety)
        if (pool.isEmpty()) {
            return Individual.random(dimensions, lowerBound, upperBound, numObjectives);
        }

        // Best-of-k tournament on sum of objectives (minimization)
        int k = Math.min(5, pool.size());
        Individual best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i < k; i++) {
            Individual cand = pool.get(rng.nextInt(pool.size()));
            double s = 0.0;
            for (double v : cand.objectives) s += v;
            if (s < bestScore) {
                bestScore = s;
                best = cand;
            }
        }

        // Safety fallback
        if (best == null) {
            return Individual.random(dimensions, lowerBound, upperBound, numObjectives);
        }
        return best;
    }


    /** Gaussian mutation with per-gene noise and clamping to bounds. */
    private Individual mutate(Individual parent) {
        double[] g = parent.genome.clone();
        for (int i = 0; i < g.length; i++) {
            g[i] += rng.nextGaussian() * mutationSigma;
            if (g[i] < lowerBound) g[i] = lowerBound;
            if (g[i] > upperBound) g[i] = upperBound;
        }
        return new Individual(g, numObjectives);
    }

    // =========================
    // Archive management
    // =========================

    /** Insert individual into its 2D bin, keep only non-dominated set, and cap size with crowding-like pruning. */
    private void insertIntoArchive(Individual ind) {
        Point cell = computeCell(ind);
        List<Individual> cellSet = archive.computeIfAbsent(cell, c -> new ArrayList<>());

        // 1) If any existing member dominates this candidate → discard
        for (Individual e : cellSet) {
            if (Utils.dominates(e.objectives, ind.objectives)) {
                return; // dominated → not inserted
            }
        }

        // 2) Remove any members dominated by the candidate
        cellSet.removeIf(e -> Utils.dominates(ind.objectives, e.objectives));

        // 3) Add candidate
        cellSet.add(ind);

        // 4) If cell over capacity, prune using a simple crowding-like spread in objective space
        if (cellSet.size() > maxPerCell) {
            pruneByCrowding(cellSet);
        }
    }

    /** Map first two genes (x0,x1) to a 2D grid index. */
    private Point computeCell(Individual ind) {
        double gx = ind.genome[0];
        // If the genome has only one variable, synthesize a second descriptor (here, 0.0)
        double gy = (ind.genome.length > 1) ? ind.genome[1] : 0.0;

        int bx = Utils.toBinIndex(gx, lowerBound, upperBound, binsPerDim);
        int by = Utils.toBinIndex(gy, lowerBound, upperBound, binsPerDim);
        return new Point(bx, by);
    }


    /** Keep at most maxPerCell by removing the most crowded point(s) in objective space. */
    private void pruneByCrowding(List<Individual> set) {
        // Compute per-objective min/max
        double[] min = new double[numObjectives];
        double[] max = new double[numObjectives];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (Individual ind : set) {
            for (int j = 0; j < numObjectives; j++) {
                min[j] = Math.min(min[j], ind.objectives[j]);
                max[j] = Math.max(max[j], ind.objectives[j]);
            }
        }

        // Compute simple crowding distance
        Map<Individual, Double> crowd = new HashMap<>();
        for (Individual ind : set) crowd.put(ind, 0.0);

        for (int j = 0; j < numObjectives; j++) {
            final int obj = j;
            set.sort(Comparator.comparingDouble(a -> a.objectives[obj]));
            // Boundary points get large distance
            crowd.put(set.get(0), Double.POSITIVE_INFINITY);
            crowd.put(set.get(set.size() - 1), Double.POSITIVE_INFINITY);
            // Internal points: normalized distance between neighbors
            for (int i = 1; i < set.size() - 1; i++) {
                double prev = set.get(i - 1).objectives[obj];
                double next = set.get(i + 1).objectives[obj];
                double denom = (max[obj] - min[obj]);
                double incr = (denom == 0) ? 0.0 : (next - prev) / denom;
                crowd.put(set.get(i), crowd.get(set.get(i)) + incr);
            }
        }

        // Remove the MOST crowded (smallest crowding distance) until within capacity
        while (set.size() > maxPerCell) {
            Individual worst = null;
            double worstScore = Double.POSITIVE_INFINITY;
            for (Individual ind : set) {
                double d = crowd.getOrDefault(ind, 0.0);
                if (d < worstScore) {
                    worstScore = d;
                    worst = ind;
                }
            }
            set.remove(worst);
            crowd.remove(worst);
        }
    }

    // =========================
    // Output helpers
    // =========================

    /** Print a brief summary of filled cells and per-cell Pareto set sizes. */
    public void printArchiveSummary() {
        System.out.println("=== MO-ME Archive Summary ===");
        System.out.println("Filled cells: " + archive.size());
        int total = 0, maxCell = 0;
        for (Map.Entry<Point, List<Individual>> e : archive.entrySet()) {
            int k = e.getValue().size();
            total += k;
            maxCell = Math.max(maxCell, k);
        }
        double avg = archive.isEmpty() ? 0.0 : (double) total / archive.size();
        System.out.printf("Avg per-cell Pareto set size: %.2f (max: %d)\n", avg, maxCell);
    }

    /** Print all cells and their members (can be long for big grids). */
    public void printArchiveDetailed() {
        System.out.println("=== MO-ME Archive (Detailed) ===");
        for (Map.Entry<Point, List<Individual>> e : archive.entrySet()) {
            Point p = e.getKey();
            System.out.printf("Cell (%d,%d), count=%d\n", p.x, p.y, e.getValue().size());
            for (Individual ind : e.getValue()) {
                System.out.println("  " + ind);
            }
        }
    }

    /** Return the global non-dominated set (ND) across all archive cells. */
    public List<Individual> getGlobalParetoSet() {
        // collect all individuals from all bins
        List<Individual> all = new ArrayList<>();
        for (List<Individual> cell : archive.values()) {
            all.addAll(cell);
        }

        // filter out dominated ones
        List<Individual> nd = new ArrayList<>();
        for (Individual a : all) {
            boolean dominated = false;
            for (Individual b : all) {
                if (a == b) continue;
                if (Utils.dominates(b.objectives, a.objectives)) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) nd.add(a);
        }
        return nd;
    }


    public double[][] getGlobalParetoAsArray() {
        List<Individual> nd = getGlobalParetoSet();
        double[][] arr = new double[nd.size()][2]; // assumes bi-objective
        for (int i = 0; i < nd.size(); i++) {
            arr[i][0] = nd.get(i).objectives[0];   // MUST be objectives
            arr[i][1] = nd.get(i).objectives[1];
        }
        return arr;
    }

}
