package io.squid.cypgl.view.cli;

import io.squid.cypgl.models.*;
import io.squid.cypgl.controller.cli.CLIController;

import java.io.File;
import java.util.Scanner;

/**
 * Text-based interactive command-line interface view.
 * Handles user input/output and forwards commands to the dedicated CLIController.
 *
 * @author TopeEstLa
 */
public class CommandLineInterface {

    private CLIController cliController;
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
        this.cliController = new CLIController(simAbs);
        this.cliController.initGrid(w, h);
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
                cliController.tick(count);
                System.out.printf("Advanced simulation by %d tick(s). Current tick: %d%n",
                        count, cliController.getAbstraction().getTickCount());
            }
            case "set" -> {
                if (tokens.length < 4) {
                    System.out.println("Usage: set <x> <y> <type> [pollution] [customRate]");
                    return;
                }
                int x = Integer.parseInt(tokens[1]);
                int y = Integer.parseInt(tokens[2]);
                String typeStr = tokens[3].toUpperCase();
                double pollution = 0.0;
                if (tokens.length >= 5) {
                    pollution = Double.parseDouble(tokens[4]);
                }
                double customRate = 1.0;
                if (tokens.length >= 6) {
                    customRate = Double.parseDouble(tokens[5]);
                }

                if (!typeStr.equals("AIR") && !typeStr.equals("TREE") && !typeStr.equals("FACTORY") && !typeStr.equals("BUILDING")) {
                    System.out.println("Unknown type: " + typeStr + ". Choose from: AIR, TREE, FACTORY, BUILDING");
                    return;
                }

                if (x < 0 || x >= cliController.getAbstraction().getGrid().getWidth() ||
                        y < 0 || y >= cliController.getAbstraction().getGrid().getHeight()) {
                    System.out.println("Coordinates out of grid boundaries.");
                    return;
                }

                cliController.setCell(x, y, typeStr, pollution, customRate);
                System.out.printf("Set cell (%d, %d) to %s (pollution: %.2f, customRate: %.2f)%n", x, y, typeStr, pollution, customRate);
            }
            case "random" -> {
                if (tokens.length < 3) {
                    System.out.println("Usage: random <type> <percentage>");
                    return;
                }
                String typeStr = tokens[1].toUpperCase();
                double pct = Double.parseDouble(tokens[2]) / 100.0;
                if (!typeStr.equals("AIR") && !typeStr.equals("TREE") && !typeStr.equals("FACTORY") && !typeStr.equals("BUILDING")) {
                    System.out.println("Unknown type: " + typeStr);
                    return;
                }
                cliController.massSpawn(typeStr, pct);
                System.out.printf("Randomly seeded %.0f%% of empty cells with %s.%n", pct * 100, typeStr);
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
                    cliController.saveSimulation(file);
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
                    cliController.loadSimulation(file);
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

    private void applyConfig(String param, String val) {
        SimulationParameters p = cliController.getAbstraction().getParameters();

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
            case "wind_direction" -> {
                try {
                    WindDirection dir = WindDirection.valueOf(val.toUpperCase().replace("-", "_"));
                    p.setWindDirection(dir);
                    System.out.printf("Wind direction set to %s%n", p.getWindDirection());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid direction. Use one of: NONE, NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST");
                }
            }
            case "wind_strength" -> {
                try {
                    double str = Double.parseDouble(val);
                    p.setWindStrength(str);
                    System.out.printf("Wind strength set to %.2f%n", p.getWindStrength());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid strength value. Must be a double between 0.0 and 1.0.");
                }
            }
            default -> System.out.println("Unknown config parameter. Use: diffusion, absorption, generation, wind_direction, wind_strength");
        }
    }

    private void showGrid() {
        GridAbstraction grid = cliController.getAbstraction().getGrid();
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
                AbstractCell cell = grid.getCell(x, y);
                char c = cell.getConsoleChar();

                // For AIR cells, show shaded intensity depending on pollution
                if (cell instanceof AirCell) {
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
        System.out.println("Legend: . (Clean Air), ░/▒/▓/█ (Polluted Air levels), T (Tree), # (Factory), B (Building)");
    }

    private void showStats() {
        SimulationAbstraction abs = cliController.getAbstraction();
        GridAbstraction grid = abs.getGrid();
        int totalCells = grid.getWidth() * grid.getHeight();

        int trees = 0, factories = 0, air = 0, buildings = 0;
        double totalPollution = 0.0;

        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                AbstractCell cell = grid.getCell(x, y);
                if (cell != null) {
                    totalPollution += cell.getPollutionLevel();
                    String name = cell.getName();
                    switch (name) {
                        case "TREE" -> trees++;
                        case "FACTORY" -> factories++;
                        case "AIR" -> air++;
                        case "BUILDING" -> buildings++;
                    }
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
        System.out.printf("  - BUILDING  : %d (%.1f%%)%n", buildings, (double) buildings / totalCells * 100);
        System.out.println("Parameters:");
        System.out.printf("  - Diffusion Rate  : %.2f%n", abs.getParameters().getDiffusionRate());
        System.out.printf("  - Absorption Rate : %.2f%n", abs.getParameters().getAbsorptionRate());
        System.out.printf("  - Generation Rate : %.2f%n", abs.getParameters().getGenerationRate());
        System.out.printf("  - Wind Direction  : %s%n", abs.getParameters().getWindDirection());
        System.out.printf("  - Wind Strength   : %.2f%n", abs.getParameters().getWindStrength());
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  help                               - Show this guide.");
        System.out.println("  init <width> <height>              - Create a clean grid of specified dimensions.");
        System.out.println("  show                               - Render the grid visually in ASCII format.");
        System.out.println("  tick [count]                       - Run simulation for count ticks (default 1).");
        System.out.println("  set <x> <y> <type> [pollution] [rate] - Place a cell (AIR, TREE, FACTORY, BUILDING) at (x, y) with optional pollution and custom rate multiplier.");
        System.out.println("  random <type> <percentage>         - Randomly seed a % of clean cells with specified type.");
        System.out.println("  stats                              - View grid configurations and cell statistics.");
        System.out.println("  config <param> <value>             - Set parameters:");
        System.out.println("                                         - diffusion <0.0 - 1.0>");
        System.out.println("                                         - absorption <0.0 - 1.0>");
        System.out.println("                                         - generation <0.0 - 1.0>");
        System.out.println("                                         - wind_direction <NONE/NORTH/EAST/SOUTH/WEST/...>");
        System.out.println("                                         - wind_strength <0.0 - 1.0>");
        System.out.println("  save <filename>                    - Save current simulation state to a binary file.");
        System.out.println("  load <filename>                    - Load a simulation state from a binary file.");
        System.out.println("  exit / quit                        - Terminate the application.");
    }
}
