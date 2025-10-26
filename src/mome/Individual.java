package mome;

import java.util.Arrays;
import java.util.Random;

public class Individual {
    public double[] genome;        // decision variables
    public double[] objectives;    // multi-objective scores

    private static final Random rng = new Random();

    /** Construct empty individual of given dimension and objective count. */
    public Individual(int dimensions, int numObjectives) {
        this.genome = new double[dimensions];
        this.objectives = new double[numObjectives];
        Arrays.fill(this.objectives, Double.POSITIVE_INFINITY);
    }

    /** Construct from genome (objectives to be set later). */
    public Individual(double[] genome, int numObjectives) {
        this.genome = genome.clone();
        this.objectives = new double[numObjectives];
        Arrays.fill(this.objectives, Double.POSITIVE_INFINITY);
    }

    /** Generate a random individual within bounds. */
    public static Individual random(int dimensions, double lower, double upper, int numObjectives) {
        double[] g = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            g[i] = Utils.randUniform(rng, lower, upper);
        }
        return new Individual(g, numObjectives);
    }

    @Override
    public String toString() {
        return "Genome=" + Arrays.toString(genome) +
                ", Objectives=" + Arrays.toString(objectives);
    }
}
