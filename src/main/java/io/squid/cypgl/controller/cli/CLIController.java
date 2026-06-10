package io.squid.cypgl.controller.cli;

import io.squid.cypgl.models.*;
import io.squid.cypgl.models.cells.AirCell;
import io.squid.cypgl.models.cells.BuildingCell;
import io.squid.cypgl.models.cells.FactoryCell;
import io.squid.cypgl.models.cells.VegetationCell;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Controller mediating simulation data model updates specifically for the CommandLineInterface view.
 * Isolates CLI tick operations and cell management, bypassing JavaFX dependencies.
 *
 * @author TopeEstLa
 */
public class CLIController {

    private Simulation abstraction;

    /**
     * Constructs a CLIController with the given simulation abstraction.
     *
     * @param abstraction the simulation abstraction to be controlled
     */
    public CLIController(Simulation abstraction) {
        this.abstraction = abstraction;
    }

    /**
     * Gets the simulation abstraction model.
     *
     * @return the current simulation model
     */
    public Simulation getAbstraction() {
        return abstraction;
    }

    /**
     * Initializes the simulation model grid with clean AIR cells.
     *
     * @param w the width of the grid
     * @param h the height of the grid
     */
    public void initGrid(int w, int h) {
        this.abstraction = new Simulation(w, h);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.abstraction.getGrid().setCell(x, y, new AirCell(x, y, 0.0));
            }
        }
    }

    /**
     * Runs simulation execution loop for a specific tick count.
     *
     * @param count the number of ticks to run the simulation
     */
    public void tick(int count) {
        Grid grid = abstraction.getGrid();
        SimulationParameters params = abstraction.getParameters();
        int w = grid.getWidth();
        int h = grid.getHeight();

        for (int i = 0; i < count; i++) {
            // Phase 1: Compute next state based on neighbors' current state
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    AbstractCell cell = grid.getCell(x, y);
                    if (cell != null) {
                        cell.computeNextState(grid, params);
                    }
                }
            }

            // Phase 2: Commit computed states to active states
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    AbstractCell cell = grid.getCell(x, y);
                    if (cell != null) {
                        cell.commitState();
                    }
                }
            }

            abstraction.incrementTickCount();
        }
    }

    /**
     * Replaces the cell at (x, y) with a new subclass representing the target type.
     *
     * @param x          the x-coordinate of the cell
     * @param y          the y-coordinate of the cell
     * @param typeName   the type of the cell (e.g., "AIR", "VEGETATION", "FACTORY", "BUILDING")
     * @param pollution  the initial pollution level of the cell
     * @param customRate the custom rate for absorption or production for the cell
     */
    public void setCell(int x, int y, String typeName, double pollution, double customRate) {
        Grid grid = abstraction.getGrid();
        AbstractCell cell = switch (typeName.toUpperCase()) {
            case "AIR" -> new AirCell(x, y, pollution);
            case "VEGETATION" -> new VegetationCell(x, y, pollution);
            case "FACTORY" -> new FactoryCell(x, y, pollution);
            case "BUILDING" -> new BuildingCell(x, y, pollution);
            default -> new AirCell(x, y, pollution);
        };
        cell.setCustomRate(customRate);
        grid.setCell(x, y, cell);
    }

    /**
     * Randomly spawns a percentage of cells of a target type across active clean air tiles.
     *
     * @param typeName   the type of cells to spawn (e.g., "VEGETATION", "FACTORY", "BUILDING")
     * @param percentage the percentage of clean air cells to replace (between 0.0 and 1.0)
     */
    public void massSpawn(String typeName, double percentage) {
        Random rand = new Random();
        Grid grid = abstraction.getGrid();
        int w = grid.getWidth();
        int h = grid.getHeight();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                AbstractCell cell = grid.getCell(x, y);
                if (cell != null && cell.getName().equals("AIR")) {
                    if (rand.nextDouble() < percentage) {
                        AbstractCell newCell = switch (typeName.toUpperCase()) {
                            case "AIR" -> new AirCell(x, y, 0.0);
                            case "VEGETATION" -> new VegetationCell(x, y, 0.0);
                            case "FACTORY" -> new FactoryCell(x, y, 0.0);
                            case "BUILDING" -> new BuildingCell(x, y, 0.0);
                            default -> new AirCell(x, y, 0.0);
                        };
                        grid.setCell(x, y, newCell);
                    }
                }
            }
        }
    }

    /**
     * Saves the current simulation state to a file.
     *
     * @param file the target file where the simulation will be saved
     * @throws IOException if an I/O error occurs during saving
     */
    public void saveSimulation(File file) throws IOException {
        abstraction.saveToFile(file);
    }

    /**
     * Loads a simulation state from a file.
     *
     * @param file the file from which the simulation will be loaded
     * @throws IOException            if an I/O error occurs during loading
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    public void loadSimulation(File file) throws IOException, ClassNotFoundException {
        this.abstraction = Simulation.loadFromFile(file);
    }

    /**
     * Gets the current tick count of the simulation.
     *
     * @return the number of elapsed simulation ticks
     */
    public int getTickCount() {
        return abstraction.getTickCount();
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
     * Gets a character representation of the cell type at (x, y) for console display.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return a char representation: 'A' for Air, 'V' for Vegetation, 'F' for Factory, 'B' for Building, or ' ' if null/unknown
     */
    public char getCellConsoleChar(int x, int y) {
        AbstractCell cell = abstraction.getGrid().getCell(x, y);
        if (cell == null) return ' ';
        return switch (cell.getName()) {
            case "AIR" -> 'A';
            case "VEGETATION" -> 'V';
            case "FACTORY" -> 'F';
            case "BUILDING" -> 'B';
            default -> ' ';
        };
    }

    /**
     * Gets the pollution level of the cell at (x, y).
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return the pollution level of the cell, or 0.0 if the cell does not exist
     */
    public double getCellPollutionLevel(int x, int y) {
        AbstractCell cell = abstraction.getGrid().getCell(x, y);
        return cell != null ? cell.getPollutionLevel() : 0.0;
    }

    /**
     * Gets the type name of the cell at (x, y).
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return the name of the cell type, or an empty string if the cell does not exist
     */
    public String getCellName(int x, int y) {
        AbstractCell cell = abstraction.getGrid().getCell(x, y);
        return cell != null ? cell.getName() : "";
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
     * @param rate the new diffusion rate to set
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
     * @param rate the new absorption rate to set
     */
    public void setAbsorptionRate(double rate) {
        abstraction.getParameters().setAbsorptionRate(rate);
    }

    /**
     * Gets the current wind direction of the simulation.
     *
     * @return the wind direction
     */
    public WindDirection getWindDirection() {
        return abstraction.getParameters().getWindDirection();
    }

    /**
     * Sets the wind direction of the simulation.
     *
     * @param direction the new wind direction
     */
    public void setWindDirection(WindDirection direction) {
        abstraction.getParameters().setWindDirection(direction);
    }

    /**
     * Gets the wind strength of the simulation.
     *
     * @return the wind strength
     */
    public double getWindStrength() {
        return abstraction.getParameters().getWindStrength();
    }

    /**
     * Sets the wind strength of the simulation.
     *
     * @param strength the new wind strength
     */
    public void setWindStrength(double strength) {
        abstraction.getParameters().setWindStrength(strength);
    }

    /**
     * Gets the counts of each cell type present in the grid.
     *
     * @return a map mapping cell type names to their counts
     */
    public Map<String, Integer> getCellTypeCounts() {
        return abstraction.getGrid().getCellTypeCounts();
    }

    /**
     * Gets the percentage distributions of each cell type present in the grid.
     *
     * @return a map mapping cell type names to their percentages
     */
    public Map<String, Double> getCellTypePercentages() {
        return abstraction.getGrid().getCellTypePercentages();
    }

}

