package io.squid.cybreathe;

import io.squid.cybreathe.view.cli.CommandLineInterface;
import io.squid.cybreathe.view.javafx.GUIApplication;
import javafx.application.Application;

/**
 * CyBreathe launcher.
 * Supports launching in interactive GUI mode (default) or CLI mode (via '--cli' argument).
 *
 * @author TopeEstLa
 */
public class CyBreathe {

    /**
     * Main entry point of the application.
     * Decides whether to launch in CLI or GUI mode based on the command-line arguments.
     *
     * @param args the command-line arguments; passing "--cli" runs the CLI mode, otherwise GUI mode is attempted
     */
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
