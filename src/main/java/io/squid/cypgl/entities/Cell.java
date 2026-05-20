package io.squid.cypgl.entities;

/**
 * @author TopeEstLa
 */
public class Cell {

    private double pollutionLevel;

    public Cell(double pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(double pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }
}
