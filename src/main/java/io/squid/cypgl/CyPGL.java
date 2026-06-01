package io.squid.cypgl;

import io.squid.cypgl.view.cli.CommandLineInterface;
import io.squid.cypgl.view.javafx.GUIApplication;
import javafx.application.Application;

/**
 * Main application launcher for the 2D Cellular Pollution Simulation.
 * Supports launching in interactive GUI mode (default) or CLI mode (via '--cli' argument).
 *
 * @author TopeEstLa
 */
public class CyPGL {

    public static void main(String[] args) {
        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        if (cliMode) {
            CommandLineInterface cli = new CommandLineInterface();
            cli.start();
        } else {
            System.out.println("Launching GUI Mode...");
            try {
                Application.launch(GUIApplication.class, args);
            } catch (Throwable t) {
                System.err.println("Failed to launch JavaFX GUI: " + t.getMessage());
                System.err.println("Falling back to CLI mode...");
                CommandLineInterface cli = new CommandLineInterface();
                cli.start();
            }
        }
    }
}
