package io.squid.cypgl.entities;

import java.io.Serializable;

/**
 * Global configuration parameters for the 2D Cellular Pollution Simulation.
 * These settings control the speed, rates, and thresholds of pollution dynamics.
 *
 * @author TopeEstLa
 */
public class SimulationParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    // Rates of change
    private double diffusionRate = 0.3;     // Speed at which pollution spreads from Air cells (0.0 to 1.0)
    private double absorptionRate = 0.15;   // Rate at which Trees absorb pollution (0.0 to 1.0)
    private double generationRate = 0.5;   // Rate at which Factories emit pollution to neighbors (0.0 to 1.0)

    // Tree Health parameters
    private double treePollutionThreshold = 0.5; // Pollution level above which tree health decays
    private double treeDecayRate = 0.05;         // Health lost per tick when polluted
    private double treeRecoveryRate = 0.02;      // Health gained per tick when clean

    public double getDiffusionRate() {
        return diffusionRate;
    }

    public void setDiffusionRate(double diffusionRate) {
        this.diffusionRate = Math.clamp(diffusionRate, 0.0, 1.0);
    }

    public double getAbsorptionRate() {
        return absorptionRate;
    }

    public void setAbsorptionRate(double absorptionRate) {
        this.absorptionRate = Math.clamp(absorptionRate, 0.0, 1.0);
    }

    public double getGenerationRate() {
        return generationRate;
    }

    public void setGenerationRate(double generationRate) {
        this.generationRate = Math.clamp(generationRate, 0.0, 1.0);
    }

    public double getTreePollutionThreshold() {
        return treePollutionThreshold;
    }

    public void setTreePollutionThreshold(double treePollutionThreshold) {
        this.treePollutionThreshold = Math.clamp(treePollutionThreshold, 0.0, 1.0);
    }

    public double getTreeDecayRate() {
        return treeDecayRate;
    }

    public void setTreeDecayRate(double treeDecayRate) {
        this.treeDecayRate = Math.clamp(treeDecayRate, 0.0, 1.0);
    }

    public double getTreeRecoveryRate() {
        return treeRecoveryRate;
    }

    public void setTreeRecoveryRate(double treeRecoveryRate) {
        this.treeRecoveryRate = Math.clamp(treeRecoveryRate, 0.0, 1.0);
    }
}
