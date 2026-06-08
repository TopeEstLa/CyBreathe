package io.squid.cypgl.controller.javafx;

import io.squid.cypgl.models.*;
import io.squid.cypgl.view.javafx.SimulationPresentation;

import java.io.File;
import java.io.IOException;

/**
 * Controller mediating communication between SimulationAbstraction model and JavaFX SimulationPresentation view.
 *
 * @author TopeEstLa
 */
public class SimulationControl {

    private final GridControl gridControl;
    private Simulation abstraction;
    private SimulationPresentation presentation;

    public SimulationControl(Simulation abstraction) {
        this.abstraction = abstraction;
        this.gridControl = new GridControl(abstraction.getGrid());
        recordCurrentStats(); // Seed initial statistics
    }

    public Simulation getAbstraction() {
        return abstraction;
    }

    public SimulationPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(SimulationPresentation presentation) {
        this.presentation = presentation;
        updatePresentation();
    }

    public GridControl getGridControl() {
        return gridControl;
    }

    /**
     * Performs a single simulation step (double-buffered tick).
     */
    public synchronized void tick() {
        gridControl.computeNextStates(abstraction.getParameters());

        gridControl.commitStates();

        abstraction.incrementTickCount();
        recordCurrentStats();
        updatePresentation();
    }

    /**
     * Loops a specified number of ticks.
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
        int treeCount = 0;
        int factoryCount = 0;
        int airCount = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                AbstractCell cell = grid.getCell(x, y);
                if (cell != null) {
                    sumPollution += cell.getPollutionLevel();
                    String typeName = cell.getName();
                    switch (typeName) {
                        case "TREE" -> treeCount++;
                        case "FACTORY" -> factoryCount++;
                        case "AIR" -> airCount++;
                    }
                }
            }
        }

        double avgPollution = totalCells > 0 ? (sumPollution / totalCells) : 0.0;
        abstraction.recordStats(avgPollution, treeCount, factoryCount, airCount);
    }

    /**
     * Saves the current simulation abstraction to a binary file.
     */
    public void saveSimulation(File file) throws IOException {
        abstraction.saveToFile(file);
    }

    /**
     * Restores a simulation state from a binary file and rebuilds the controller links.
     */
    public void loadSimulation(File file) throws IOException, ClassNotFoundException {
        Simulation loadedAbs = Simulation.loadFromFile(file);

        this.abstraction = loadedAbs;

        gridControl.rebuildCellControls();

        recordCurrentStats();

        if (presentation != null) {
            presentation.rebuildGridDisplay(gridControl);
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
                    abstraction.getTreeCountHistory(),
                    abstraction.getFactoryCountHistory(),
                    abstraction.getAirCountHistory()
            );
        }
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
                gridControl.setCellType(x, y, "AIR");
                gridControl.getCellControl(x, y).setPollution(0.0);
            }
        }
        abstraction.resetTickCount();
        abstraction.clearHistory();
        recordCurrentStats();
        updatePresentation();
    }

    public double getDiffusionRate() {
        return abstraction.getParameters().getDiffusionRate();
    }

    public void setDiffusionRate(double rate) {
        abstraction.getParameters().setDiffusionRate(rate);
    }

    public double getAbsorptionRate() {
        return abstraction.getParameters().getAbsorptionRate();
    }

    public void setAbsorptionRate(double rate) {
        abstraction.getParameters().setAbsorptionRate(rate);
    }

    public double getGenerationRate() {
        return abstraction.getParameters().getGenerationRate();
    }

    public void setGenerationRate(double rate) {
        abstraction.getParameters().setGenerationRate(rate);
    }

    public int getSpeedDelayMs() {
        return abstraction.getSpeedDelayMs();
    }

    public void setSpeedDelayMs(int delayMs) {
        abstraction.setSpeedDelayMs(delayMs);
    }

    public double getWindStrength() {
        return abstraction.getParameters().getWindStrength();
    }

    public void setWindStrength(double strength) {
        abstraction.getParameters().setWindStrength(strength);
    }

    public WindDirection getWindDirection() {
        return abstraction.getParameters().getWindDirection();
    }

    public void setWindDirection(WindDirection direction) {
        abstraction.getParameters().setWindDirection(direction);
    }

    public int getGridWidth() {
        return abstraction.getGrid().getWidth();
    }

    public int getGridHeight() {
        return abstraction.getGrid().getHeight();
    }
}
