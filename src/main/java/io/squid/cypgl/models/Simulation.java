package io.squid.cypgl.models;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction layer in the PAC architecture representing the global state of the simulation.
 * Represents global stats, active grid, loaded parameters, and historical data.
 * Supports binary serialization to import/export simulation states.
 *
 * @author TopeEstLa
 */
public class Simulation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Double> avgPollutionHistory;

    private Grid grid;
    private SimulationParameters parameters;
    private int tickCount;
    private int speedDelayMs = 200;

    public Simulation(int gridWidth, int gridHeight) {
        this.grid = new Grid(gridWidth, gridHeight);
        this.parameters = new SimulationParameters();
        this.tickCount = 0;
        this.avgPollutionHistory = new ArrayList<>();
    }

    /**
     * Imports a saved simulation state from a binary file.
     */
    public static Simulation loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return (Simulation) ois.readObject();
        }
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public SimulationParameters getParameters() {
        return parameters;
    }

    public void setParameters(SimulationParameters parameters) {
        this.parameters = parameters;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void incrementTickCount() {
        this.tickCount++;
    }

    public void resetTickCount() {
        this.tickCount = 0;
    }

    public int getSpeedDelayMs() {
        return speedDelayMs;
    }

    public void setSpeedDelayMs(int speedDelayMs) {
        this.speedDelayMs = Math.clamp(speedDelayMs, 10, 2000);
    }

    public List<Double> getAvgPollutionHistory() {
        return avgPollutionHistory;
    }

    /**
     * Appends current tick stats to the historical lists.
     */
    public void recordStats(double avgPollution) {
        avgPollutionHistory.add(avgPollution);

        if (avgPollutionHistory.size() > 200) {
            avgPollutionHistory.removeFirst();
        }
    }

    public void clearHistory() {
        avgPollutionHistory.clear();
    }

    /**
     * Exports the active simulation state (abstraction and children data) to a binary file.
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(this);
        }
    }
}
