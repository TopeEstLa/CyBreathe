package io.squid.cypgl.agent.simulation;

import io.squid.cypgl.agent.grid.GridAbstraction;
import io.squid.cypgl.model.SimulationParameters;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction layer in the PAC architecture for the root Simulation agent.
 * Represents the global state of the simulation, including parameters,
 * the active grid, run parameters, and historical statistics.
 * Supports binary serialization to import/export simulation states.
 * 
 * @author TopeEstLa
 */
public class SimulationAbstraction implements Serializable {
    private static final long serialVersionUID = 1L;

    private GridAbstraction grid;
    private SimulationParameters parameters;
    
    private int tickCount;
    private int speedDelayMs = 200; // Delay between steps in milliseconds

    // Historical statistics for charting/trend purposes
    private final List<Double> avgPollutionHistory;
    private final List<Integer> treeCountHistory;
    private final List<Integer> factoryCountHistory;
    private final List<Integer> airCountHistory;

    public SimulationAbstraction(int gridWidth, int gridHeight) {
        this.grid = new GridAbstraction(gridWidth, gridHeight);
        this.parameters = new SimulationParameters();
        this.tickCount = 0;
        this.avgPollutionHistory = new ArrayList<>();
        this.treeCountHistory = new ArrayList<>();
        this.factoryCountHistory = new ArrayList<>();
        this.airCountHistory = new ArrayList<>();
    }

    public GridAbstraction getGrid() {
        return grid;
    }

    public void setGrid(GridAbstraction grid) {
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

    public List<Integer> getTreeCountHistory() {
        return treeCountHistory;
    }

    public List<Integer> getFactoryCountHistory() {
        return factoryCountHistory;
    }

    public List<Integer> getAirCountHistory() {
        return airCountHistory;
    }

    /**
     * Appends current tick stats to the historical lists.
     */
    public void recordStats(double avgPollution, int trees, int factories, int air) {
        avgPollutionHistory.add(avgPollution);
        treeCountHistory.add(trees);
        factoryCountHistory.add(factories);
        airCountHistory.add(air);

        // Keep history size reasonable (e.g. 200 ticks)
        if (avgPollutionHistory.size() > 200) {
            avgPollutionHistory.removeFirst();
            treeCountHistory.removeFirst();
            factoryCountHistory.removeFirst();
            airCountHistory.removeFirst();
        }
    }

    public void clearHistory() {
        avgPollutionHistory.clear();
        treeCountHistory.clear();
        factoryCountHistory.clear();
        airCountHistory.clear();
    }

    /**
     * Exports the active simulation state (abstraction and children data) to a binary file.
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(this);
        }
    }

    /**
     * Imports a saved simulation state from a binary file.
     */
    public static SimulationAbstraction loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return (SimulationAbstraction) ois.readObject();
        }
    }
}
