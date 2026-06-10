package io.squid.cybreathe.models;

import java.io.Serializable;

/**
 * Abstract base class representing a Cell on the 2D grid.
 * Combines basic properties with polymorphic simulation execution methods.
 *
 * @author TopeEstLa
 */
public abstract class AbstractCell implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final int x;
    protected final int y;

    protected double pollutionLevel;
    protected double nextPollutionLevel;

    protected double customRate;

    /**
     * Constructs an AbstractCell at the specified coordinates with an initial pollution level.
     *
     * @param x                the x-coordinate of the cell
     * @param y                the y-coordinate of the cell
     * @param initialPollution the initial pollution level, clamped between 0.0 and 3.0
     */
    public AbstractCell(int x, int y, double initialPollution) {
        this.x = x;
        this.y = y;
        this.pollutionLevel = Math.clamp(initialPollution, 0.0, 3.0);
        this.nextPollutionLevel = this.pollutionLevel;
        // Diverse individual rate multiplier by default (0.5 to 2.0)
        this.customRate = 0.5 + Math.random() * 1.5;
    }

    /**
     * Gets the x-coordinate of this cell.
     *
     * @return the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this cell.
     *
     * @return the y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the name of the cell type.
     *
     * @return the cell type name (e.g. "AIR", "VEGETATION", "FACTORY", "BUILDING")
     */
    public abstract String getName();

    /**
     * Gets the current pollution level of this cell.
     *
     * @return the current pollution level
     */
    public double getPollutionLevel() {
        return pollutionLevel;
    }

    /**
     * Sets the current pollution level of this cell, clamping it between 0.0 and 3.0.
     * Levels below 0.01 are set to 0.0.
     *
     * @param pollutionLevel the new pollution level to set
     */
    public void setPollutionLevel(double pollutionLevel) {
        double clamped = Math.clamp(pollutionLevel, 0.0, 3.0);
        this.pollutionLevel = clamped < 0.01 ? 0.0 : clamped;
    }

    /**
     * Gets the computed next pollution level (double-buffered state).
     *
     * @return the next pollution level
     */
    public double getNextPollutionLevel() {
        return nextPollutionLevel;
    }

    /**
     * Sets the computed next pollution level, clamping it between 0.0 and 3.0.
     * Levels below 0.01 are set to 0.0.
     *
     * @param nextPollutionLevel the next pollution level to set
     */
    public void setNextPollutionLevel(double nextPollutionLevel) {
        double clamped = Math.clamp(nextPollutionLevel, 0.0, 3.0);
        this.nextPollutionLevel = clamped < 0.01 ? 0.0 : clamped;
    }

    /**
     * Gets the custom rate for this cell.
     *
     * @return the custom rate
     */
    public double getCustomRate() {
        return customRate;
    }

    /**
     * Sets the custom rate for this cell, ensuring it is non-negative.
     *
     * @param customRate the new custom rate
     */
    public void setCustomRate(double customRate) {
        this.customRate = Math.max(0.0, customRate);
    }

    /**
     * Resets the next state buffer to match the current pollution level.
     */
    public void resetNextBuffer() {
        this.nextPollutionLevel = this.pollutionLevel;
    }

    /**
     * Calculates the next state of the cell based on neighbors and parameters,
     * and stores it in the double-buffered next state field.
     *
     * @param grid   the grid containing this cell
     * @param params the simulation parameters
     */
    public abstract void computeNextState(Grid grid, SimulationParameters params);

    /**
     * Commits the computed double-buffered state to the active state.
     */
    public void commitState() {
        this.pollutionLevel = this.nextPollutionLevel;
    }
}
