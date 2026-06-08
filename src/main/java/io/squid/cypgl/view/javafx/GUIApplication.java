package io.squid.cypgl.view.javafx;

import io.squid.cypgl.controller.javafx.SimulationController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main graphical user interface application wrapper for JavaFX presentation window.
 *
 * @author TopeEstLa
 */
public class GUIApplication extends Application {

    private SimulationView presentation;

    @Override
    public void start(Stage primaryStage) {
        try {
            int defaultSize = 30;
            SimulationController control = new SimulationController(defaultSize, defaultSize);

            presentation = new SimulationView(control);
            Scene scene = new Scene(presentation, 1280, 800);

            primaryStage.setTitle("CyBreathe v1.0");
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

    @Override
    public void stop() throws Exception {
        if (presentation != null) {
            presentation.cleanup();
        }
        super.stop();
    }
}
