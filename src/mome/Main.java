package mome;

public class Main {

    public static void main(String[] args) {

        // ============================================
        // 1) PICK A BENCHMARK (EASY / MEDIUM / HARD)
        //    Also set DIMENSIONS and BOUNDS to match.
        //    All objectives are MINIMIZED.
        // ============================================

        // ---- EASY: Schaffer N.1 (1D), bounds ~ [-100,100] ----
        // int dims = 1;
        // double lower = -100, upper = 100;
        // BenchmarkFunctionMO problem = BenchmarkFunctionsMO.schafferN1();

        // ---- EASY: Fonseca–Fleming (3D), bounds [-4,4] ----
        // int dims = 3;
        // double lower = -4, upper = 4;
        // BenchmarkFunctionMO problem = BenchmarkFunctionsMO.fonsecaFleming(dims);

        // ---- MEDIUM: ZDT1 (30D), bounds [0,1] ----
        // int dims = 30;
        // double lower = 0.0, upper = 1.0;
        // BenchmarkFunctionMO problem = BenchmarkFunctionsMO.zdt1(dims);

        // ---- MEDIUM: Kursawe (3D), bounds [-5,5] ----
        // int dims = 3;
        // double lower = -5.0, upper = 5.0;
        // BenchmarkFunctionMO problem = BenchmarkFunctionsMO.kursawe(dims);

        // ---- HARD: ZDT3 (30D), bounds [0,1] ----
        // int dims = 30;
        // double lower = 0.0, upper = 1.0;
        // BenchmarkFunctionMO problem = BenchmarkFunctionsMO.zdt3(dims);

        // ---- HARD: ZDT6 (30D), bounds [0,1] ----
        int dims = 10;
        double lower = 0.0, upper = 1.0;
        BenchmarkFunctionMO problem = BenchmarkFunctionsMO.zdt6(dims);

        // ============================================
        // 2) MAP / SEARCH SETTINGS
        // ============================================
        int numObjectives = 2;              // all benchmarks defined above are bi-objective
        int binsPerDim = 20;                // 20x20 behavior grid using (x0,x1)
        int evaluationsPerGeneration = 15000; // offspring per generation
        int generations = 50000;              // number of generations
        int initialRandom = 200;            // random seeds to populate cells
        double mutationSigma = 0.05;        // Gaussian std dev (tune per problem)
        int maxPerCell = 8;                 // cap Pareto set size per cell (keeps memory bounded)

        // ============================================
        // 3) BUILD AND RUN MO-ME
        // ============================================
        MOME algo = new MOME(
                dims,
                numObjectives,
                binsPerDim,
                lower, upper,
                evaluationsPerGeneration,
                generations,
                initialRandom,
                mutationSigma,
                maxPerCell,
                problem
        );

        algo.run();

        // ============================================
        // 4) REPORT
        // ============================================
        algo.printArchiveSummary();

        // Optional: show a few cells to sanity-check contents
        // (Uncomment to see detailed output)
        // algo.printArchiveDetailed();

        // Schaffer N.1
        // double[][] truePF = Fronts.schafferN1TrueFront(400);

        // Fonseca–Fleming (n = dims)
        // double[][] truePF = Fronts.fonsecaFlemingTrueFront(400, dims);

        // ZDT1
        // double[][] truePF = Fronts.zdt1TrueFront(400);

        // Kursawe (approximate): n = dims, choose enough samples for a smooth ND curve
        // double[][] truePF = Fronts.kursaweApproxFront(dims, /*samples=*/2_000_000, /*seed=*/42L);

        // ZDT3
        // double[][] truePF = Fronts.zdt3TrueFront(200);

        // ZDT6
        double[][] truePF = Fronts.zdt6TrueFront(400);



        double[][] approx = algo.getGlobalParetoAsArray();

        System.out.println("IGD = " + Fronts.igd(truePF, approx));
        System.out.println("GD  = " + Fronts.gd(approx, truePF));


        System.out.println("Global ND size = " + approx.length);
        if (approx.length > 0) {
            System.out.printf("First ND point: (%.4f, %.4f)%n", approx[0][0], approx[0][1]);
        }

        Plotter.showZDTOverlay("ZDT6: True Front vs Global Pareto Found", truePF, approx);

        // Optional: also save to PNG (e.g., project root)
        Plotter.saveZDTOverlayPNG("ZDT6_run_long.png", "ZDT6: True Front vs Global Pareto Found", truePF, approx, 900, 600);

        // Fronts.writeCSV("true_front.csv", truePF);
        // Fronts.writeCSV("approx_front.csv", approx);
    }
}
