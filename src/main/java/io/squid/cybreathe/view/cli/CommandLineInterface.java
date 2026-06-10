package io.squid.cybreathe.view.cli;

import io.squid.cybreathe.controller.cli.CLIController;
import io.squid.cybreathe.models.Simulation;
import io.squid.cybreathe.models.WindDirection;

import java.io.File;
import java.util.List;
import java.util.Map;
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

    /**
     * Starts the interactive command-line interface loop.
     * Continuously reads user input and executes commands until the user exits.
     */
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

    /**
     * Initializes the simulation grid with the specified width and height.
     *
     * @param w the width of the grid
     * @param h the height of the grid
     * @throws IllegalArgumentException if dimensions are not positive
     */
    private void initGrid(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive.");
        }
        Simulation simAbs = new Simulation(w, h);
        this.cliController = new CLIController(simAbs);
        this.cliController.initGrid(w, h);
    }

    /**
     * Processes a single command line input.
     *
     * @param inputLine the raw input string from the console
     */
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
                        count, cliController.getTickCount());
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

                if (!typeStr.equals("AIR") && !typeStr.equals("VEGETATION") && !typeStr.equals("FACTORY") && !typeStr.equals("BUILDING")) {
                    System.out.println("Unknown type: " + typeStr + ". Choose from: AIR, VEGETATION, FACTORY, BUILDING");
                    return;
                }

                if (x < 0 || x >= cliController.getGridWidth() ||
                        y < 0 || y >= cliController.getGridHeight()) {
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
                if (!typeStr.equals("AIR") && !typeStr.equals("VEGETATION") && !typeStr.equals("FACTORY") && !typeStr.equals("BUILDING")) {
                    System.out.println("Unknown type: " + typeStr);
                    return;
                }
                cliController.massSpawn(typeStr, pct);
                System.out.printf("Randomly seeded %.0f%% of empty cells with %s.%n", pct * 100, typeStr);
            }
            case "config" -> {
                if (tokens.length < 3) {
                    System.out.println("Usage: config <param> <value>");
                    return;
                }
                String param = tokens[1].toLowerCase();
                String val = tokens[2].toLowerCase();
                applyConfig(param, val);
            }
            case "stats" -> printStats();
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
                } catch (Exception e) {
                    System.out.println("Failed to import state: " + e.getMessage());
                }
            }
            case "exit", "quit" -> running = false;
            default -> System.out.println("Unknown command. Type 'help' for commands.");
        }
    }

    /**
     * Applies a configuration parameter change to the simulation model.
     *
     * @param param the parameter name to change
     * @param val   the new value to apply
     */
    private void applyConfig(String param, String val) {
        switch (param) {
            case "diffusion" -> {
                cliController.setDiffusionRate(Double.parseDouble(val));
                System.out.printf("Diffusion rate set to %.2f%n", cliController.getDiffusionRate());
            }
            case "absorption" -> {
                cliController.setAbsorptionRate(Double.parseDouble(val));
                System.out.printf("Absorption rate set to %.2f%n", cliController.getAbsorptionRate());
            }
            case "wind_direction" -> {
                try {
                    WindDirection dir = WindDirection.valueOf(val.toUpperCase().replace("-", "_"));
                    cliController.setWindDirection(dir);
                    System.out.printf("Wind direction set to %s%n", cliController.getWindDirection());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid direction. Use one of: NONE, NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST");
                }
            }
            case "wind_strength" -> {
                try {
                    double str = Double.parseDouble(val);
                    cliController.setWindStrength(str);
                    System.out.printf("Wind strength set to %.2f%n", cliController.getWindStrength());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid strength value. Must be a double between 0.0 and 1.0.");
                }
            }
            default ->
                    System.out.println("Unknown config parameter. Use: diffusion, absorption, generation, wind_direction, wind_strength");
        }
    }

    /**
     * Renders the grid in an ASCII-based visual format on the standard output.
     */
    private void showGrid() {
        int w = cliController.getGridWidth();
        int h = cliController.getGridHeight();

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
                char c = cliController.getCellConsoleChar(x, y);
                String typeName = cliController.getCellName(x, y);

                if ("AIR".equals(typeName)) {
                    double poll = cliController.getCellPollutionLevel(x, y);
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
        System.out.println("Legend: . (Clean Air), ░/▒/▓/█ (Polluted Air levels), V (Vegetation), # (Factory), B (Building)");
    }

    /**
     * Prints the help instructions and lists all available commands in the CLI.
     */
    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  help                               - Show this guide.");
        System.out.println("  init <width> <height>              - Create a clean grid of specified dimensions.");
        System.out.println("  show                               - Render the grid visually in ASCII format.");
        System.out.println("  tick [count]                       - Run simulation for count ticks (default 1).");
        System.out.println("  set <x> <y> <type> [pollution] [rate] - Place a cell (AIR, VEGETATION, FACTORY, BUILDING) at (x, y) with optional pollution and custom rate multiplier.");
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

    /**
     * Prints the current simulation parameters, configurations, and grid cell statistics to standard output.
     */
    private void printStats() {
        System.out.println("================================================");
        System.out.println("  Simulation Statistics & Configurations  ");
        System.out.println("================================================");
        System.out.printf("Grid Size          : %d x %d%n", cliController.getGridWidth(), cliController.getGridHeight());
        System.out.printf("Total Ticks        : %d%n", cliController.getTickCount());

        double avgPoll = 0.0;
        List<Double> history = cliController.getAbstraction().getAvgPollutionHistory();
        if (!history.isEmpty()) {
            avgPoll = history.get(history.size() - 1);
        }
        System.out.printf("Average Pollution  : %.4f%n", avgPoll);
        System.out.println();
        System.out.println("Cell Types Distribution:");
        Map<String, Integer> counts = cliController.getCellTypeCounts();
        Map<String, Double> percentages = cliController.getCellTypePercentages();
        for (String type : counts.keySet()) {
            int count = counts.get(type);
            double pct = percentages.get(type) * 100.0;
            System.out.printf("  %-10s: %d cells (%.1f%%)%n", type, count, pct);
        }
        System.out.println();
        System.out.println("Configurations:");
        System.out.printf("  Diffusion Rate   : %.2f%n", cliController.getDiffusionRate());
        System.out.printf("  Absorption Rate  : %.2f%n", cliController.getAbsorptionRate());
        System.out.printf("  Wind Direction   : %s%n", cliController.getWindDirection());
        System.out.printf("  Wind Strength    : %.2f%n", cliController.getWindStrength());
        System.out.println("================================================");
    }
}
