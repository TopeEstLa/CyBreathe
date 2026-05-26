package io.squid.cypgl.gui;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.simulation.SimulationAbstraction;
import io.squid.cypgl.agent.simulation.SimulationControl;
import io.squid.cypgl.agent.simulation.SimulationPresentation;
import io.squid.cypgl.entities.AirCellType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main graphical user interface application wrapper.
 * Instantiates the Simulation agent PAC layers, starts the window stage,
 * and sets up a default 30x30 cellular pollution board.
 * 
 * @author TopeEstLa
 */
public class GUIApplication extends Application {

    private SimulationPresentation presentation;

    @Override
    public void start(Stage primaryStage) {
        // Create a default 30x30 simulation grid
        int defaultSize = 30;
        SimulationAbstraction abstraction = new SimulationAbstraction(defaultSize, defaultSize);

        // Pre-fill with standard clean AIR cells
        for (int x = 0; x < defaultSize; x++) {
            for (int y = 0; y < defaultSize; y++) {
                abstraction.getGrid().setCell(x, y, new CellAbstraction(x, y, new AirCellType(), 0.0));
            }
        }

        // Initialize root agent Controller
        SimulationControl control = new SimulationControl(abstraction);

        // Initialize root agent View
        presentation = new SimulationPresentation(control);

        // Create main application Scene
        Scene scene = new Scene(presentation, 1280, 800);
        
        primaryStage.setTitle("2D Cellular Pollution Simulation - CyTech PGL v1.0");
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
