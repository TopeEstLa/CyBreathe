package io.squid.cypgl.agent.simulation;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;
import io.squid.cypgl.agent.grid.GridControl;
import io.squid.cypgl.model.SimulationParameters;
import java.io.File;
import java.io.IOException;

/**
 * Control layer in the PAC architecture for the root Simulation agent.
 * Manages the background simulation execution loop, gathers grid statistics,
 * and handles persistent save/load operations.
 * 
 * @author TopeEstLa
 */
public class SimulationControl {

    private SimulationAbstraction abstraction;
    private SimulationPresentation presentation; // Optional, null in CLI mode
    private final GridControl gridControl;

    public SimulationControl(SimulationAbstraction abstraction) {
        this.abstraction = abstraction;
        this.gridControl = new GridControl(abstraction.getGrid());
        recordCurrentStats(); // Seed initial statistics
    }

    public SimulationAbstraction getAbstraction() {
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
        // Phase 1: Compute next state based on neighbors' current state
        gridControl.computeNextStates(abstraction.getParameters());
        
        // Phase 2: Commit computed states to active states
        gridControl.commitStates();
        
        // Update statistics
        abstraction.incrementTickCount();
        recordCurrentStats();

        // Propagate UI update
        updatePresentation();
    }

    /**
     * Loops a specified number of ticks (convenient for CLI commands).
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
        GridAbstraction grid = abstraction.getGrid();
        int w = grid.getWidth();
        int h = grid.getHeight();
        int totalCells = w * h;

        double sumPollution = 0.0;
        int treeCount = 0;
        int factoryCount = 0;
        int airCount = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                CellAbstraction cell = grid.getCell(x, y);
                if (cell != null) {
                    sumPollution += cell.getPollutionLevel();
                    String typeName = cell.getType().getName();
                    switch (typeName) {
                        case "TREE" -> treeCount++;
                        case "FACTORY" -> factoryCount++;
                        case "AIR", "DEAD_TREE" -> airCount++; // Count dead trees as air equivalent for base stats
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
        // Load the new abstraction
        SimulationAbstraction loadedAbs = SimulationAbstraction.loadFromFile(file);
        
        // Swap active abstraction reference
        this.abstraction = loadedAbs;
        
        // Relink the GridControl and rebuild cell controls
        
        // Copy the grid cells over to the existing grid control structure
        int w = loadedAbs.getGrid().getWidth();
        int h = loadedAbs.getGrid().getHeight();
        
        // Rebuild and copy references
        gridControl.rebuildCellControls();
        
        // Update stats
        recordCurrentStats();

        // Redraw GUI if active
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
}
