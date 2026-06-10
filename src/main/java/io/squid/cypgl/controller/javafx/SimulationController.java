package io.squid.cypgl.controller.javafx;

import io.squid.cypgl.models.*;
import io.squid.cypgl.view.javafx.SimulationView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller mediating communication between SimulationAbstraction model and JavaFX SimulationPresentation view.
 *
 * @author TopeEstLa
 */
public class SimulationController {

    private GridController gridController;
    private Simulation abstraction;
    private SimulationView presentation;

    /**
     * Constructs a SimulationController with a new simulation of the specified grid dimensions.
     *
     * @param gridWidth  the width of the grid
     * @param gridHeight the height of the grid
     */
    public SimulationController(int gridWidth, int gridHeight) {
        this.abstraction = new Simulation(gridWidth, gridHeight);
        this.gridController = new GridController(abstraction.getGrid());
        recordCurrentStats();
    }

    /**
     * Gets the associated simulation view presentation.
     *
     * @return the simulation view presentation
     */
    public SimulationView getPresentation() {
        return presentation;
    }

    /**
     * Sets the simulation view presentation and updates it.
     *
     * @param presentation the simulation view presentation to associate
     */
    public void setPresentation(SimulationView presentation) {
        this.presentation = presentation;
        updatePresentation();
    }

    /**
     * Gets the grid controller managed by this simulation.
     *
     * @return the grid controller
     */
    public GridController getGridControl() {
        return gridController;
    }

    /**
     * Performs a single simulation step (double-buffered tick).
     */
    public synchronized void tick() {
        gridController.computeNextStates(abstraction.getParameters());

        gridController.commitStates();

        abstraction.incrementTickCount();
        recordCurrentStats();
        updatePresentation();
    }

    /**
     * Loops a specified number of ticks.
     *
     * @param count the number of ticks to run
     */
    public void tickMultiple(int count) {
        for (int i = 0; i < count; i++) {
            tick();
        }
    }

    /**
     * Scans the grid, calculates aggregate statistics, and records them in history.
     */
    public void recordCurrentStats() {
        Grid grid = abstraction.getGrid();
        int w = grid.getWidth();
        int h = grid.getHeight();
        int totalCells = w * h;

        double sumPollution = 0.0;
        int pollutedAirCount = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                AbstractCell cell = grid.getCell(x, y);
                if (cell != null) {
                    sumPollution += cell.getPollutionLevel();
                    if ("AIR".equals(cell.getName()) && cell.getPollutionLevel() > 0.0) {
                        pollutedAirCount++;
                    }
                }
            }
        }

        double avgPollution = totalCells > 0 ? (sumPollution / totalCells) : 0.0;
        abstraction.recordStats(avgPollution, pollutedAirCount);
    }

    /**
     * Saves the current simulation abstraction to a binary file.
     *
     * @param file the target file to save the simulation
     * @throws IOException if an I/O error occurs during saving
     */
    public void saveSimulation(File file) throws IOException {
        abstraction.saveToFile(file);
    }

    /**
     * Restores a simulation state from a binary file and rebuilds the controller links.
     *
     * @param file the source file to load the simulation from
     * @throws IOException            if an I/O error occurs during loading
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    public void loadSimulation(File file) throws IOException, ClassNotFoundException {
        Simulation loadedAbs = Simulation.loadFromFile(file);

        this.abstraction = loadedAbs;
        this.gridController = new GridController(abstraction.getGrid());

        recordCurrentStats();

        if (presentation != null) {
            presentation.rebuildGridDisplay();
            presentation.syncUIWithModel();
            updatePresentation();
        }
    }

    /**
     * Triggers the Presentation dashboard to update charts and text panels.
     */
    public void updatePresentation() {
        if (presentation != null) {
            presentation.updateDashboard(
                    abstraction.getTickCount(),
                    abstraction.getAvgPollutionHistory(),
                    abstraction.getPollutedAirHistory()
            );
        }
    }

    /**
     * Gets the history of the number of polluted air cells.
     *
     * @return a list of polluted air counts per tick
     */
    public List<Integer> getPollutedAirHistory() {
        return abstraction.getPollutedAirHistory();
    }

    /**
     * Resets the entire simulation to a clean state.
     */
    public synchronized void clearSimulation() {
        Grid grid = abstraction.getGrid();
        int w = grid.getWidth();
        int h = grid.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                gridController.setCellType(x, y, "AIR");
                gridController.getCellControl(x, y).setPollution(0.0);
            }
        }
        abstraction.resetTickCount();
        abstraction.clearHistory();
        recordCurrentStats();
        updatePresentation();
    }

    /**
     * Gets the global diffusion rate for pollution.
     *
     * @return the diffusion rate
     */
    public double getDiffusionRate() {
        return abstraction.getParameters().getDiffusionRate();
    }

    /**
     * Sets the global diffusion rate for pollution.
     *
     * @param rate the new diffusion rate
     */
    public void setDiffusionRate(double rate) {
        abstraction.getParameters().setDiffusionRate(rate);
    }

    /**
     * Gets the global absorption rate for pollution.
     *
     * @return the absorption rate
     */
    public double getAbsorptionRate() {
        return abstraction.getParameters().getAbsorptionRate();
    }

    /**
     * Sets the global absorption rate for pollution.
     *
     * @param rate the new absorption rate
     */
    public void setAbsorptionRate(double rate) {
        abstraction.getParameters().setAbsorptionRate(rate);
    }

    /**
     * Gets the delay between simulation steps in milliseconds for GUI execution.
     *
     * @return the speed delay in milliseconds
     */
    public int getSpeedDelayMs() {
        return abstraction.getSpeedDelayMs();
    }

    /**
     * Sets the delay between simulation steps in milliseconds for GUI execution.
     *
     * @param delayMs the new delay in milliseconds
     */
    public void setSpeedDelayMs(int delayMs) {
        abstraction.setSpeedDelayMs(delayMs);
    }

    /**
     * Gets the wind strength.
     *
     * @return the wind strength
     */
    public double getWindStrength() {
        return abstraction.getParameters().getWindStrength();
    }

    /**
     * Sets the wind strength.
     *
     * @param strength the new wind strength
     */
    public void setWindStrength(double strength) {
        abstraction.getParameters().setWindStrength(strength);
    }

    /**
     * Gets the wind direction.
     *
     * @return the wind direction
     */
    public WindDirection getWindDirection() {
        return abstraction.getParameters().getWindDirection();
    }

    /**
     * Sets the wind direction.
     *
     * @param direction the new wind direction
     */
    public void setWindDirection(WindDirection direction) {
        abstraction.getParameters().setWindDirection(direction);
    }

    /**
     * Gets the width of the simulation grid.
     *
     * @return the grid width
     */
    public int getGridWidth() {
        return abstraction.getGrid().getWidth();
    }

    /**
     * Gets the height of the simulation grid.
     *
     * @return the grid height
     */
    public int getGridHeight() {
        return abstraction.getGrid().getHeight();
    }

    /**
     * Gets the count of each cell type in the grid.
     *
     * @return a map mapping cell type names to their counts
     */
    public Map<String, Integer> getCellTypeCounts() {
        return abstraction.getGrid().getCellTypeCounts();
    }

    /**
     * Gets the percentage of each cell type in the grid.
     *
     * @return a map mapping cell type names to their percentage share
     */
    public Map<String, Double> getCellTypePercentages() {
        return abstraction.getGrid().getCellTypePercentages();
    }

    /**
     * Resets the simulation with a new grid size, preserving the existing simulation parameters.
     *
     * @param width  the new width of the grid
     * @param height the new height of the grid
     */
    public synchronized void resizeAndReset(int width, int height) {
        SimulationParameters oldParams = this.abstraction.getParameters();
        int oldSpeed = this.abstraction.getSpeedDelayMs();

        this.abstraction = new Simulation(width, height);

        // Preserve parameters and speed delay
        this.abstraction.setParameters(oldParams);
        this.abstraction.setSpeedDelayMs(oldSpeed);

        this.gridController = new GridController(abstraction.getGrid());
        recordCurrentStats();
        if (presentation != null) {
            presentation.rebuildGridDisplay();
            presentation.syncUIWithModel();
            updatePresentation();
        }
    }
}

