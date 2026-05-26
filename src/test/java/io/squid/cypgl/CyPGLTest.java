package io.squid.cypgl;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;
import io.squid.cypgl.agent.simulation.SimulationAbstraction;
import io.squid.cypgl.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Unit Tests verifying the 2D Cellular Pollution Simulation behaviors.
 * 
 * @author TopeEstLa
 */
public class CyPGLTest {

    private SimulationParameters params;

    @BeforeEach
    public void setUp() {
        params = new SimulationParameters();
        params.setDiffusionRate(0.5);
        params.setAbsorptionRate(0.2);
        params.setGenerationRate(0.4);
    }

    @Test
    public void testBoundedMooreNeighbors() {
        GridAbstraction grid = new GridAbstraction(3, 3);

        // Populate grid
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                grid.setCell(x, y, new CellAbstraction(x, y, new AirCellType(), 0.0));
            }
        }

        // Cell (0, 0) should have exactly 3 neighbors in a Bounded grid with Moore neighborhood
        List<CellAbstraction> neighbors00 = grid.getNeighbors(0, 0);
        assertEquals(3, neighbors00.size());

        // Cell (1, 1) should have exactly 8 neighbors
        List<CellAbstraction> neighbors11 = grid.getNeighbors(1, 1);
        assertEquals(8, neighbors11.size());
    }

    @Test
    public void testAirDiffusionMath() {
        GridAbstraction grid = new GridAbstraction(3, 3);

        // Populate: (1, 1) is AIR with 0.0 pollution.
        // Neighbors: 4 of them are 1.0, 4 of them are 0.0
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                grid.setCell(x, y, new CellAbstraction(x, y, new AirCellType(), 0.0));
            }
        }

        grid.getCell(0, 0).setPollutionLevel(1.0);
        grid.getCell(0, 2).setPollutionLevel(1.0);
        grid.getCell(2, 0).setPollutionLevel(1.0);
        grid.getCell(2, 2).setPollutionLevel(1.0);

        CellAbstraction center = grid.getCell(1, 1);
        assertEquals(0.0, center.getPollutionLevel());

        // Run compute next state on the center cell
        center.getType().computeNextState(center, grid, params);
        
        // Expected average of neighbors: (1.0 * 4 + 0.0 * 4) / 8 = 0.5
        // Expected next state: 0.0 + 0.5 * (0.5 - 0.0) = 0.25
        assertEquals(0.25, center.getNextPollutionLevel(), 0.0001);

        // Commit state
        center.getType().commitState(center);
        assertEquals(0.25, center.getPollutionLevel(), 0.0001);
    }

    @Test
    public void testFactoryCreation() {
        GridAbstraction grid = new GridAbstraction(1, 1);
        CellAbstraction cell = new CellAbstraction(0, 0, new FactoryCellType(), 0.0);
        grid.setCell(0, 0, cell);

        // Factory computeNextState should always set pollution to 1.0
        cell.getType().computeNextState(cell, grid, params);
        assertEquals(1.0, cell.getNextPollutionLevel());

        cell.getType().commitState(cell);
        assertEquals(1.0, cell.getPollutionLevel());
    }

    @Test
    public void testTreeAbsorption() {
        GridAbstraction grid = new GridAbstraction(1, 1);
        CellAbstraction cell = new CellAbstraction(0, 0, new TreeCellType(), 0.8);
        grid.setCell(0, 0, cell);


        // Absorption: 0.8 - absorptionRate (0.2) = 0.6
        // Health should remain locked at 1.0 (permanently alive)
        cell.getType().computeNextState(cell, grid, params);
        
        assertEquals(0.6, cell.getNextPollutionLevel(), 0.0001);
        assertEquals("TREE", cell.getNextType().getName());

        // Commit
        cell.getType().commitState(cell);
        assertEquals(0.6, cell.getPollutionLevel(), 0.0001);
    }

    @Test
    public void testBinarySerialization() throws IOException, ClassNotFoundException {
        SimulationAbstraction originalAbs = new SimulationAbstraction(2, 2);
        originalAbs.getGrid().setCell(0, 0, new CellAbstraction(0, 0, new FactoryCellType(), 1.0));
        originalAbs.getGrid().setCell(0, 1, new CellAbstraction(0, 1, new TreeCellType(), 0.5));
        originalAbs.getGrid().setCell(1, 0, new CellAbstraction(1, 0, new AirCellType(), 0.1));
        originalAbs.getGrid().setCell(1, 1, new CellAbstraction(1, 1, new AirCellType(), 0.0));
        
        originalAbs.getParameters().setDiffusionRate(0.44);
        originalAbs.recordStats(0.4, 1, 1, 2);

        // Temp file inside the project workspace directory
        File tempFile = new File("run/temp_simulation_test.cyp");
        tempFile.getParentFile().mkdirs();

        try {
            // Save
            originalAbs.saveToFile(tempFile);
            assertTrue(tempFile.exists());

            // Load
            SimulationAbstraction restoredAbs = SimulationAbstraction.loadFromFile(tempFile);
            assertNotNull(restoredAbs);

            // Assertions
            assertEquals(0.44, restoredAbs.getParameters().getDiffusionRate(), 0.0001);
            assertEquals("FACTORY", restoredAbs.getGrid().getCell(0, 0).getType().getName());
            assertEquals("TREE", restoredAbs.getGrid().getCell(0, 1).getType().getName());
            assertEquals("AIR", restoredAbs.getGrid().getCell(1, 0).getType().getName());
            assertEquals("AIR", restoredAbs.getGrid().getCell(1, 1).getType().getName());
            
            assertEquals(1.0, restoredAbs.getGrid().getCell(0, 0).getPollutionLevel());
            assertEquals(0.5, restoredAbs.getGrid().getCell(0, 1).getPollutionLevel());
            assertEquals(0.1, restoredAbs.getGrid().getCell(1, 0).getPollutionLevel());
            
            assertEquals(1, restoredAbs.getAvgPollutionHistory().size());
            assertEquals(0.4, restoredAbs.getAvgPollutionHistory().get(0), 0.0001);

        } finally {
            // Cleanup
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
