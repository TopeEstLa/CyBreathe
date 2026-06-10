package io.squid.cybreathe.models;

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
    private List<Integer> pollutedAirHistory;

    private Grid grid;
    private SimulationParameters parameters;
    private int tickCount;
    private int speedDelayMs = 200;

    /**
     * Constructs a Simulation with the specified grid width and height.
     *
     * @param gridWidth  the width of the simulation grid
     * @param gridHeight the height of the simulation grid
     */
    public Simulation(int gridWidth, int gridHeight) {
        this.grid = new Grid(gridWidth, gridHeight);
        this.parameters = new SimulationParameters();
        this.tickCount = 0;
        this.avgPollutionHistory = new ArrayList<>();
        this.pollutedAirHistory = new ArrayList<>();
    }

    /**
     * Imports a saved simulation state from a binary file.
     *
     * @param file the source file to load the simulation from
     * @return the deserialized Simulation instance
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    public static Simulation loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return (Simulation) ois.readObject();
        }
    }

    /**
     * Gets the grid model.
     *
     * @return the grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Sets the grid model.
     *
     * @param grid the new grid model
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    /**
     * Gets the simulation parameters.
     *
     * @return the simulation parameters
     */
    public SimulationParameters getParameters() {
        return parameters;
    }

    /**
     * Sets the simulation parameters.
     *
     * @param parameters the new simulation parameters
     */
    public void setParameters(SimulationParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the current tick count of the simulation.
     *
     * @return the tick count
     */
    public int getTickCount() {
        return tickCount;
    }

    /**
     * Increments the simulation tick count by one.
     */
    public void incrementTickCount() {
        this.tickCount++;
    }

    /**
     * Resets the simulation tick count to zero.
     */
    public void resetTickCount() {
        this.tickCount = 0;
    }

    /**
     * Gets the delay between simulation steps in milliseconds for GUI execution.
     *
     * @return the speed delay in milliseconds
     */
    public int getSpeedDelayMs() {
        return speedDelayMs;
    }

    /**
     * Sets the delay between simulation steps in milliseconds for GUI execution, clamping it between 10 and 2000 ms.
     *
     * @param speedDelayMs the speed delay in milliseconds
     */
    public void setSpeedDelayMs(int speedDelayMs) {
        this.speedDelayMs = Math.clamp(speedDelayMs, 10, 2000);
    }

    /**
     * Gets the historical list of average pollution levels.
     *
     * @return a list of average pollution levels per tick
     */
    public List<Double> getAvgPollutionHistory() {
        return avgPollutionHistory;
    }

    /**
     * Gets the historical list of polluted air cell counts.
     *
     * @return a list of polluted air cell counts per tick
     */
    public List<Integer> getPollutedAirHistory() {
        if (pollutedAirHistory == null) {
            pollutedAirHistory = new ArrayList<>();
        }
        return pollutedAirHistory;
    }

    /**
     * Appends the current average pollution level to the historical list, using a default polluted air count of 0.
     *
     * @param avgPollution the average pollution level
     */
    public void recordStats(double avgPollution) {
        recordStats(avgPollution, 0);
    }

    /**
     * Appends the current average pollution level and polluted air cell count to the historical lists.
     * Limits history size to the last 200 ticks.
     *
     * @param avgPollution     the average pollution level
     * @param pollutedAirCount the count of polluted air cells
     */
    public void recordStats(double avgPollution, int pollutedAirCount) {
        if (avgPollutionHistory != null) {
            avgPollutionHistory.add(avgPollution);
            if (avgPollutionHistory.size() > 200) {
                avgPollutionHistory.removeFirst();
            }
        }
        List<Integer> pah = getPollutedAirHistory();
        pah.add(pollutedAirCount);
        if (pah.size() > 200) {
            pah.removeFirst();
        }
    }

    /**
     * Clears all recorded statistics history.
     */
    public void clearHistory() {
        if (avgPollutionHistory != null) {
            avgPollutionHistory.clear();
        }
        getPollutedAirHistory().clear();
    }

    /**
     * Exports the active simulation state (abstraction and children data) to a binary file.
     *
     * @param file the target file to save the simulation
     * @throws IOException if an I/O error occurs during saving
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(this);
        }
    }
}
