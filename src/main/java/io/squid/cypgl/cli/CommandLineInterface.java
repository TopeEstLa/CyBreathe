package io.squid.cypgl.cli;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;
import io.squid.cypgl.agent.simulation.SimulationAbstraction;
import io.squid.cypgl.agent.simulation.SimulationControl;
import io.squid.cypgl.model.*;
import java.io.File;
import java.util.Scanner;

/**
 * Text-based interactive command-line interface for the 2D Cellular Pollution Simulation.
 * Allows independent validation of the simulation model, configurations, ticks, and binary serialization.
 * 
 * @author TopeEstLa
 */
public class CommandLineInterface {

    private SimulationControl simulationControl;
    private boolean running = true;

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================");
        System.out.println("  2D Cellular Pollution Simulation - CLI Mode  ");
        System.out.println("=================================================");
        System.out.println("Type 'help' to list available commands.");
        
        // Initialize a default 15x15 grid on start
        initGrid(15, 15);
        System.out.println("Initialized default 15x15 grid. Ready.");

        while (running) {
            System.out.print("\nCyPGL> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                processCommand(line);
            } catch (Exception e) {
                System.out.println("Error processing command: " + e.getMessage());
            }
        }
        System.out.println("Exiting CyPGL CLI. Goodbye!");
    }

    private void initGrid(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive.");
        }
        SimulationAbstraction simAbs = new SimulationAbstraction(w, h);
        
        // Populate grid with default clean AIR cells
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                simAbs.getGrid().setCell(x, y, new CellAbstraction(x, y, new AirCellType(), 0.0));
            }
        }
        
        this.simulationControl = new SimulationControl(simAbs);
    }

    private void processCommand(String inputLine) {
        String[] tokens = inputLine.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help" -> printHelp();
            case "init" -> {
                if (tokens.length < 3) {
                    System.out.println("Usage: init <width> <height>");
                    return;
                }
                int w = Integer.parseInt(tokens[1]);
                int h = Integer.parseInt(tokens[2]);
                initGrid(w, h);
                System.out.printf("Created a clean %dx%d grid.%n", w, h);
            }
            case "show" -> showGrid();
            case "tick" -> {
                int count = 1;
                if (tokens.length >= 2) {
                    count = Integer.parseInt(tokens[1]);
                }
                simulationControl.tickMultiple(count);
                System.out.printf("Advanced simulation by %d tick(s). Current tick: %d%n", 
                                  count, simulationControl.getAbstraction().getTickCount());
            }
            case "set" -> {
                if (tokens.length < 4) {
                    System.out.println("Usage: set <x> <y> <type> [pollution]");
                    return;
                }
                int x = Integer.parseInt(tokens[1]);
                int y = Integer.parseInt(tokens[2]);
                String typeStr = tokens[3].toUpperCase();
                double pollution = 0.0;
                if (tokens.length >= 5) {
                    pollution = Double.parseDouble(tokens[4]);
                }

                CellType type = parseCellType(typeStr);
                if (type == null) {
                    System.out.println("Unknown type: " + typeStr + ". Choose from: AIR, TREE, FACTORY, DEAD_TREE");
                    return;
                }

                if (x < 0 || x >= simulationControl.getAbstraction().getGrid().getWidth() ||
                    y < 0 || y >= simulationControl.getAbstraction().getGrid().getHeight()) {
                    System.out.println("Coordinates out of grid boundaries.");
                    return;
                }

                simulationControl.getGridControl().setCellType(x, y, type);
                simulationControl.getGridControl().getCellControl(x, y).setPollution(pollution);
                
                // Seed tree health to full on manual placement
                if (type instanceof TreeCellType) {
                    simulationControl.getGridControl().getCellControl(x, y).getAbstraction().setHealth(1.0);
                }
                
                simulationControl.recordCurrentStats();
                System.out.printf("Set cell (%d, %d) to %s (pollution: %.2f)%n", x, y, type.getName(), pollution);
            }
            case "random" -> {
                if (tokens.length < 3) {
                    System.out.println("Usage: random <type> <percentage>");
                    return;
                }
                String typeStr = tokens[1].toUpperCase();
                double pct = Double.parseDouble(tokens[2]) / 100.0;
                CellType type = parseCellType(typeStr);
                if (type == null) {
                    System.out.println("Unknown type: " + typeStr);
                    return;
                }
                simulationControl.getGridControl().massSpawn(type, pct);
                simulationControl.recordCurrentStats();
                System.out.printf("Randomly seeded %.0f%% of empty cells with %s.%n", pct * 100, type.getName());
            }
            case "stats", "status" -> showStats();
            case "config" -> {
                if (tokens.length < 3) {
                    System.out.println("Usage: config <param> <value>");
                    return;
                }
                String param = tokens[1].toLowerCase();
                String val = tokens[2].toLowerCase();
                applyConfig(param, val);
            }
            case "save" -> {
                if (tokens.length < 2) {
                    System.out.println("Usage: save <filename>");
                    return;
                }
                File file = new File(tokens[1]);
                try {
                    simulationControl.saveSimulation(file);
                    System.out.println("Simulation successfully exported to " + file.getAbsolutePath());
                } catch (Exception e) {
                    System.out.println("Failed to export state: " + e.getMessage());
                }
            }
            case "load" -> {
                if (tokens.length < 2) {
                    System.out.println("Usage: load <filename>");
                    return;
                }
                File file = new File(tokens[1]);
                try {
                    simulationControl.loadSimulation(file);
                    System.out.println("Simulation successfully loaded from " + file.getAbsolutePath());
                    showStats();
                } catch (Exception e) {
                    System.out.println("Failed to import state: " + e.getMessage());
                }
            }
            case "exit", "quit" -> running = false;
            default -> System.out.println("Unknown command. Type 'help' for commands.");
        }
    }

    private CellType parseCellType(String str) {
        return switch (str) {
            case "AIR" -> new AirCellType();
            case "TREE" -> new TreeCellType();
            case "FACTORY" -> new FactoryCellType();
            case "DEAD_TREE" -> new DeadTreeCellType();
            default -> null;
        };
    }

    private void applyConfig(String param, String val) {
        GridAbstraction grid = simulationControl.getAbstraction().getGrid();
        SimulationParameters p = simulationControl.getAbstraction().getParameters();

        switch (param) {
            case "diffusion" -> {
                p.setDiffusionRate(Double.parseDouble(val));
                System.out.printf("Diffusion rate set to %.2f%n", p.getDiffusionRate());
            }
            case "absorption" -> {
                p.setAbsorptionRate(Double.parseDouble(val));
                System.out.printf("Absorption rate set to %.2f%n", p.getAbsorptionRate());
            }
            case "generation" -> {
                p.setGenerationRate(Double.parseDouble(val));
                System.out.printf("Generation rate set to %.2f%n", p.getGenerationRate());
            }
            default -> System.out.println("Unknown config parameter. Use: diffusion, absorption, generation");
        }
    }

    private void showGrid() {
        GridAbstraction grid = simulationControl.getAbstraction().getGrid();
        int w = grid.getWidth();
        int h = grid.getHeight();

        // Print header coordinate index
        System.out.print("   ");
        for (int x = 0; x < w; x++) {
            System.out.print(x % 10 + " ");
        }
        System.out.println();

        System.out.print("  +");
        System.out.print("--".repeat(w));
        System.out.println("+");

        for (int y = 0; y < h; y++) {
            System.out.printf("%2d| ", y);
            for (int x = 0; x < w; x++) {
                CellAbstraction cell = grid.getCell(x, y);
                char c = cell.getType().getConsoleChar();
                
                // For AIR cells, show shaded intensity depending on pollution
                if (cell.getType() instanceof AirCellType) {
                    double poll = cell.getPollutionLevel();
                    if (poll == 0.0) {
                        c = '.';
                    } else if (poll <= 0.25) {
                        c = '░';
                    } else if (poll <= 0.50) {
                        c = '▒';
                    } else if (poll <= 0.75) {
                        c = '▓';
                    } else {
                        c = '█';
                    }
                }
                System.out.print(c + " ");
            }
            System.out.println("|");
        }

        System.out.print("  +");
        System.out.print("--".repeat(w));
        System.out.println("+");
        System.out.println("Legend: . (Clean Air), ░/▒/▓/█ (Polluted Air levels), T (Tree), # (Factory), x (Dead Tree)");
    }

    private void showStats() {
        SimulationAbstraction abs = simulationControl.getAbstraction();
        GridAbstraction grid = abs.getGrid();
        int totalCells = grid.getWidth() * grid.getHeight();

        // Get count
        int trees = 0, factories = 0, air = 0, dead = 0;
        double totalPollution = 0.0;

        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                CellAbstraction cell = grid.getCell(x, y);
                totalPollution += cell.getPollutionLevel();
                String name = cell.getType().getName();
                switch (name) {
                    case "TREE" -> trees++;
                    case "FACTORY" -> factories++;
                    case "AIR" -> air++;
                    case "DEAD_TREE" -> dead++;
                }
            }
        }

        double avgPollution = totalCells > 0 ? totalPollution / totalCells : 0.0;

        System.out.println("--- Grid Status & Stats ---");
        System.out.printf("Grid Size   : %d x %d (%d cells)%n", grid.getWidth(), grid.getHeight(), totalCells);
        System.out.printf("Current Tick: %d%n", abs.getTickCount());
        System.out.printf("Avg Pollution: %.4f%n", avgPollution);
        System.out.println("Cell Populations:");
        System.out.printf("  - AIR       : %d (%.1f%%)%n", air, (double) air / totalCells * 100);
        System.out.printf("  - TREE      : %d (%.1f%%)%n", trees, (double) trees / totalCells * 100);
        System.out.printf("  - FACTORY   : %d (%.1f%%)%n", factories, (double) factories / totalCells * 100);
        System.out.printf("  - DEAD_TREE : %d (%.1f%%)%n", dead, (double) dead / totalCells * 100);
        System.out.println("Parameters:");
        System.out.printf("  - Diffusion Rate  : %.2f%n", abs.getParameters().getDiffusionRate());
        System.out.printf("  - Absorption Rate : %.2f%n", abs.getParameters().getAbsorptionRate());
        System.out.printf("  - Generation Rate : %.2f%n", abs.getParameters().getGenerationRate());
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  help                               - Show this guide.");
        System.out.println("  init <width> <height>              - Create a clean grid of specified dimensions.");
        System.out.println("  show                               - Render the grid visually in ASCII format.");
        System.out.println("  tick [count]                       - Run simulation for count ticks (default 1).");
        System.out.println("  set <x> <y> <type> [pollution]     - Place a cell (AIR, TREE, FACTORY, DEAD_TREE) at (x, y) and set optional pollution.");
        System.out.println("  random <type> <percentage>         - Randomly seed a % of clean cells with specified type.");
        System.out.println("  stats                              - View grid configurations and cell statistics.");
        System.out.println("  config <param> <value>             - Set parameters:");
        System.out.println("                                         - diffusion <0.0 - 1.0>");
        System.out.println("                                         - absorption <0.0 - 1.0>");
        System.out.println("                                         - generation <0.0 - 1.0>");
        System.out.println("  save <filename>                    - Save current simulation state to a binary file.");
        System.out.println("  load <filename>                    - Load a simulation state from a binary file.");
        System.out.println("  exit / quit                        - Terminate the application.");
    }
}
