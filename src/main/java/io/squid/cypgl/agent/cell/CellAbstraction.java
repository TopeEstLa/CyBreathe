package io.squid.cypgl.agent.cell;

import io.squid.cypgl.entities.CellType;
import java.io.Serializable;

/**
 * Abstraction layer in the PAC architecture for a Cell agent.
 * Represents the data state of a single cell on the 2D grid.
 * 
 * @author TopeEstLa
 */
public class CellAbstraction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int x;
    private final int y;

    // Active state variables
    private CellType type;
    private double pollutionLevel;

    // Double-buffered variables for order-independent grid updates
    private CellType nextType;
    private double nextPollutionLevel;

    public CellAbstraction(int x, int y, CellType type, double initialPollution) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.nextType = type;
        this.pollutionLevel = Math.clamp(initialPollution, 0.0, 1.0);
        this.nextPollutionLevel = this.pollutionLevel;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
        this.nextType = type;
    }

    public CellType getNextType() {
        return nextType;
    }

    public void setNextType(CellType nextType) {
        this.nextType = nextType;
    }

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(double pollutionLevel) {
        this.pollutionLevel = Math.clamp(pollutionLevel, 0.0, 1.0);
    }

    public double getNextPollutionLevel() {
        return nextPollutionLevel;
    }

    public void setNextPollutionLevel(double nextPollutionLevel) {
        this.nextPollutionLevel = Math.clamp(nextPollutionLevel, 0.0, 1.0);
    }

    /**
     * Resets next state values to match the current state.
     */
    public void resetNextBuffer() {
        this.nextType = this.type;
        this.nextPollutionLevel = this.pollutionLevel;
    }
}
