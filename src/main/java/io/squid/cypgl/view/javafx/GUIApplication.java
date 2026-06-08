package io.squid.cypgl.view.javafx;

import io.squid.cypgl.controller.javafx.SimulationController;
import io.squid.cypgl.models.AirCell;
import io.squid.cypgl.models.Simulation;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main graphical user interface application wrapper for JavaFX presentation window.
 *
 * @author TopeEstLa
 */
public class GUIApplication extends Application {

    private SimulationPresentation presentation;

    @Override
    public void start(Stage primaryStage) {
        int defaultSize = 30;
        Simulation abstraction = new Simulation(defaultSize, defaultSize);

        for (int x = 0; x < defaultSize; x++) {
            for (int y = 0; y < defaultSize; y++) {
                abstraction.getGrid().setCell(x, y, new AirCell(x, y, 0.0));
            }
        }

        SimulationController control = new SimulationController(abstraction);

        presentation = new SimulationPresentation(control);
        Scene scene = new Scene(presentation, 1280, 800);

        primaryStage.setTitle("CyBreathe v1.0");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (presentation != null) {
            presentation.cleanup();
        }
        super.stop();
    }
}
