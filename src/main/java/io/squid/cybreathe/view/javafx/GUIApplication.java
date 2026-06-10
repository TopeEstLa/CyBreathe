package io.squid.cybreathe.view.javafx;

import io.squid.cybreathe.controller.javafx.SimulationController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main graphical user interface application wrapper for JavaFX presentation window.
 *
 * @author TopeEstLa
 */
public class GUIApplication extends Application {

    private SimulationView presentation;

    /**
     * Entry point to launch the graphical user interface.
     * Initializes the controller, the presentation view, and setup the primary stage.
     *
     * @param primaryStage the stage for this application, on which the scene will be set
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            int defaultSize = 30;
            SimulationController control = new SimulationController(defaultSize, defaultSize);

            presentation = new SimulationView(control);
            Scene scene = new Scene(presentation, 1280, 800);

            primaryStage.setTitle("CyBreathe - SquidDevelopment");
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/squidicon.png")));
            } catch (Exception ex) {
                System.err.println("Failed to load application icon: " + ex.getMessage());
            }

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();
        } catch (Throwable t) {
            System.err.println("Error initializing GUI: " + t.getMessage());
            t.printStackTrace();
            throw new RuntimeException("Failed to initialize GUI", t);
        }
    }

    /**
     * Performs cleanup tasks when the application is stopped, such as shutting down background threads.
     *
     * @throws Exception if an error occurs during stop
     */
    @Override
    public void stop() throws Exception {
        if (presentation != null) {
            presentation.cleanup();
        }
        super.stop();
    }
}
