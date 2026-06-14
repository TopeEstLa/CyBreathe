package io.squid.cybreathe.models;

import java.io.Serializable;

/**
 * Global configuration parameters for the 2D Cellular Pollution Simulation.
 * These settings control the speed, rates, and thresholds of pollution dynamics.
 *
 * @author TopeEstLa
 */
public class SimulationParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Speed at which pollution spreads from Air cells (0.0 to 1.0).
     */
    private double diffusionRate = 0.3;

    /**
     * Rate at which Vegetation absorbs pollution (0.0 to 1.0).
     */
    private double absorptionRate = 0.15;

    /**
     * Direction of atmospheric wind.
     */
    private WindDirection windDirection = WindDirection.NONE;

    /**
     * Strength of the wind advection effect (0.0 to 1.0).
     */
    private double windStrength = 0.5;

    /**
     * Constructs SimulationParameters with default settings.
     */
    public SimulationParameters() {
        // Default constructor
    }

    /**
     * Gets the global diffusion rate of pollution.
     *
     * @return the diffusion rate (value between 0.0 and 1.0)
     */
    public double getDiffusionRate() {
        return diffusionRate;
    }

    /**
     * Sets the global diffusion rate of pollution, clamped between 0.0 and 1.0.
     *
     * @param diffusionRate the new diffusion rate
     */
    public void setDiffusionRate(double diffusionRate) {
        this.diffusionRate = Math.clamp(diffusionRate, 0.0, 1.0);
    }

    /**
     * Gets the global absorption rate of vegetation cells.
     *
     * @return the absorption rate (value between 0.0 and 1.0)
     */
    public double getAbsorptionRate() {
        return absorptionRate;
    }

    /**
     * Sets the global absorption rate of vegetation cells, clamped between 0.0 and 1.0.
     *
     * @param absorptionRate the new absorption rate
     */
    public void setAbsorptionRate(double absorptionRate) {
        this.absorptionRate = Math.clamp(absorptionRate, 0.0, 1.0);
    }

    /**
     * Gets the current wind direction.
     *
     * @return the wind direction
     */
    public WindDirection getWindDirection() {
        return windDirection;
    }

    /**
     * Sets the wind direction. If null, sets it to WindDirection.NONE.
     *
     * @param windDirection the new wind direction
     */
    public void setWindDirection(WindDirection windDirection) {
        this.windDirection = windDirection != null ? windDirection : WindDirection.NONE;
    }

    /**
     * Gets the wind strength.
     *
     * @return the wind strength (value between 0.0 and 1.0)
     */
    public double getWindStrength() {
        return windStrength;
    }

    /**
     * Sets the wind strength, clamped between 0.0 and 1.0.
     *
     * @param windStrength the new wind strength
     */
    public void setWindStrength(double windStrength) {
        this.windStrength = Math.clamp(windStrength, 0.0, 1.0);
    }
}
