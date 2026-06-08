package io.squid.cypgl.models;

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

    public AbstractCell(int x, int y, double initialPollution) {
        this.x = x;
        this.y = y;
        this.pollutionLevel = Math.clamp(initialPollution, 0.0, 3.0);
        this.nextPollutionLevel = this.pollutionLevel;
        // Diverse individual rate multiplier by default (0.5 to 2.0)
        this.customRate = 0.5 + Math.random() * 1.5;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public abstract String getName();
    
    public abstract char getConsoleChar();

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(double pollutionLevel) {
        double clamped = Math.clamp(pollutionLevel, 0.0, 3.0);
        this.pollutionLevel = clamped < 0.01 ? 0.0 : clamped;
    }

    public double getNextPollutionLevel() {
        return nextPollutionLevel;
    }

    public void setNextPollutionLevel(double nextPollutionLevel) {
        double clamped = Math.clamp(nextPollutionLevel, 0.0, 3.0);
        this.nextPollutionLevel = clamped < 0.01 ? 0.0 : clamped;
    }

    public double getCustomRate() {
        return customRate;
    }

    public void setCustomRate(double customRate) {
        this.customRate = Math.max(0.0, customRate);
    }

    /**
     * Resets next state values to match the current state.
     */
    public void resetNextBuffer() {
        this.nextPollutionLevel = this.pollutionLevel;
    }

    /**
     * Calculates the next state of the cell and stores it in double-buffered fields.
     */
    public abstract void computeNextState(Grid grid, SimulationParameters params);

    /**
     * Commits the computed double-buffered state to the active state.
     */
    public void commitState() {
        this.pollutionLevel = this.nextPollutionLevel;
    }
}
