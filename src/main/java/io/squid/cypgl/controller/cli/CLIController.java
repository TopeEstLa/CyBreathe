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

    public CLIController(Simulation abstraction) {
        this.abstraction = abstraction;
    }

    public Simulation getAbstraction() {
        return abstraction;
    }

    /**
     * Initializes the simulation model grid with clean AIR cells.
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

    public void saveSimulation(File file) throws IOException {
        abstraction.saveToFile(file);
    }

    public void loadSimulation(File file) throws IOException, ClassNotFoundException {
        this.abstraction = Simulation.loadFromFile(file);
    }

    public int getTickCount() {
        return abstraction.getTickCount();
    }

    public int getGridWidth() {
        return abstraction.getGrid().getWidth();
    }

    public int getGridHeight() {
        return abstraction.getGrid().getHeight();
    }

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

    public double getCellPollutionLevel(int x, int y) {
        AbstractCell cell = abstraction.getGrid().getCell(x, y);
        return cell != null ? cell.getPollutionLevel() : 0.0;
    }

    public String getCellName(int x, int y) {
        AbstractCell cell = abstraction.getGrid().getCell(x, y);
        return cell != null ? cell.getName() : "";
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

    public WindDirection getWindDirection() {
        return abstraction.getParameters().getWindDirection();
    }

    public void setWindDirection(WindDirection direction) {
        abstraction.getParameters().setWindDirection(direction);
    }

    public double getWindStrength() {
        return abstraction.getParameters().getWindStrength();
    }

    public void setWindStrength(double strength) {
        abstraction.getParameters().setWindStrength(strength);
    }

    public Map<String, Integer> getCellTypeCounts() {
        return abstraction.getGrid().getCellTypeCounts();
    }

    public Map<String, Double> getCellTypePercentages() {
        return abstraction.getGrid().getCellTypePercentages();
    }

}

