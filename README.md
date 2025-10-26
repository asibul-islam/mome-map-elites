# Multi-Objective MAP-Elites (MOME)

This repository implements **MOME**, a Quality-Diversity algorithm that extends MAP-Elites to **multi-objective** optimization while preserving diversity across a **2D behavior grid**.  
In this implementation, the behavior descriptor is the **first two genome coordinates** (x₀, x₁).

> TL;DR: We keep *diverse, high-performing* solutions by maintaining a per-cell Pareto archive over a 2D descriptor space.

---

## 🔍 Key Features
- 2D behavior grid (bins along x₀ and x₁)
- Per-cell **Pareto archive** (cap configurable to bound memory)
- Continuous domains with Gaussian mutation
- Benchmarks included (bi-objective): **ZDT** family, Kursawe, Fonseca, etc.
- Batch plotting utilities for quick visual inspection (see `results/`)

---

## 🧠 Algorithm (high level)
1. **Seeding**: sample initial random solutions and place them into cells by descriptor (x₀,x₁).
2. **Variation loop**:
   - Select parents (from filled cells), mutate (Gaussian with `mutationSigma`), clip to bounds.
   - Evaluate **M** objectives; compute descriptor; route to a cell.
   - **Update cell**: keep a small Pareto set (non-dominated) up to `maxPerCell`; discard dominated or over-capacity using a simple crowding/tiebreak rule.
3. **Repeat** for `generations` with `evaluationsPerGeneration` offspring each.
4. **Report** coverage, fronts, and archive plots.

---

## ⚙️ Default Configuration (from `Main.java`)
```java
int dims = 10;                 // genome length
int numObjectives = 2;         // bi-objective benchmarks
int binsPerDim = 20;           // 20x20 grid on (x0,x1)
int evaluationsPerGeneration = 15000;
int generations = 50000;
int initialRandom = 200;       // seeds to populate cells
double mutationSigma = 0.05;   // Gaussian std dev
int maxPerCell = 8;            // Pareto cap per cell

double lower = 0.0, upper = 1.0;  // typical ZDT bounds
// Benchmark: e.g., BenchmarkFunctionsMO.zdt6(dims)
