package io.squid.cybreathe;

import io.squid.cybreathe.models.*;
import io.squid.cybreathe.models.cells.AirCell;
import io.squid.cybreathe.models.cells.BuildingCell;
import io.squid.cybreathe.models.cells.FactoryCell;
import io.squid.cybreathe.models.cells.VegetationCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Unit Tests verifying the PAC polymorphic model behaviors.
 *
 * @author TopeEstLa
 */
public class CyBreatheTest {

    private SimulationParameters params;

    @BeforeEach
    public void setUp() {
        params = new SimulationParameters();
        params.setDiffusionRate(0.5);
        params.setAbsorptionRate(0.2);
    }

    @Test
    public void testBoundedMooreNeighbors() {
        Grid grid = new Grid(3, 3);

        // Populate grid
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                grid.setCell(x, y, new AirCell(x, y, 0.0));
            }
        }

        // Cell (0, 0) should have exactly 3 neighbors in a Bounded grid with Moore neighborhood
        List<AbstractCell> neighbors00 = grid.getNeighbors(0, 0);
        assertEquals(3, neighbors00.size());

        // Cell (1, 1) should have exactly 8 neighbors
        List<AbstractCell> neighbors11 = grid.getNeighbors(1, 1);
        assertEquals(8, neighbors11.size());
    }

    @Test
    public void testAirDiffusionMath() {
        Grid grid = new Grid(3, 3);

        grid.getCell(0, 0).setPollutionLevel(1.0);
        grid.getCell(0, 2).setPollutionLevel(1.0);
        grid.getCell(2, 0).setPollutionLevel(1.0);
        grid.getCell(2, 2).setPollutionLevel(1.0);

        AbstractCell center = grid.getCell(1, 1);
        assertEquals(0.0, center.getPollutionLevel());

        // Run compute next state on the center cell
        center.computeNextState(params, grid);

        // Expected average of neighbors: (1.0 * 4 + 0.0 * 4) / 8 = 0.5
        // Expected next state: 0.0 + 0.5 * (0.5 - 0.0) = 0.25
        assertEquals(0.25, center.getNextPollutionLevel(), 0.0001);

        // Commit state
        center.commitState();
        assertEquals(0.25, center.getPollutionLevel(), 0.0001);
    }

    @Test
    public void testFactoryCreation() {
        Grid grid = new Grid(1, 1);
        AbstractCell cell = new FactoryCell(0, 0, 0.0);
        cell.setCustomRate(1.0);
        grid.setCell(0, 0, cell);

        // Factory computeNextState during running hours (e.g., tick 480 -> 8:00 AM)
        cell.computeNextState(params, grid, 480);
        assertEquals(1.0, cell.getNextPollutionLevel(), 0.0001);

        cell.commitState();
        assertEquals(1.0, cell.getPollutionLevel(), 0.0001);

        // Factory computeNextState during off-hours (e.g., tick 0 -> 12:00 AM)
        cell.computeNextState(params, grid, 0);
        assertEquals(0.8, cell.getNextPollutionLevel(), 0.0001);

        cell.commitState();
        assertEquals(0.8, cell.getPollutionLevel(), 0.0001);
    }

    @Test
    public void testVegetationAbsorption() {
        Grid grid = new Grid(1, 1);
        AbstractCell cell = new VegetationCell(0, 0, 0.8);
        grid.setCell(0, 0, cell);
        cell.setCustomRate(1.0);

        // Absorption: 0.8 - absorptionRate (0.2) = 0.6
        cell.computeNextState(params, grid);

        assertEquals(0.6, cell.getNextPollutionLevel(), 0.0001);
        assertEquals("VEGETATION", cell.getName());

        // Commit
        cell.commitState();
        assertEquals(0.6, cell.getPollutionLevel(), 0.0001);
    }

    @Test
    public void testBuildingBlocksPollution() {
        Grid grid = new Grid(3, 3);

        // (1, 1) is BUILDING
        grid.setCell(1, 1, new BuildingCell(1, 1, 0.0));

        // Populate others as AIR
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (x == 1 && y == 1) continue;
                grid.setCell(x, y, new AirCell(x, y, 0.0));
            }
        }

        // Set all neighbors of (1, 2) to 1.0 (excluding (1, 1) which is BUILDING)
        // Neighbors of (1, 2) in a 3x3 grid are: (0, 1), (1, 1) BUILDING, (2, 1), (0, 2), (2, 2)
        grid.getCell(0, 1).setPollutionLevel(1.0);
        grid.getCell(2, 1).setPollutionLevel(1.0);
        grid.getCell(0, 2).setPollutionLevel(1.0);
        grid.getCell(2, 2).setPollutionLevel(1.0);

        AbstractCell testCell = grid.getCell(1, 2);
        assertEquals(0.0, testCell.getPollutionLevel());

        // Compute next state on testCell
        testCell.computeNextState(params, grid);

        // Expected avg of active neighbors: (1.0 * 4) / 4 = 1.0 (completely ignoring (1, 1) BUILDING)
        // nextPollution = 0.0 + 0.5 * (1.0 - 0.0) = 0.5
        assertEquals(0.5, testCell.getNextPollutionLevel(), 0.0001);
    }

    @Test
    public void testWindAdvectionMath() {
        Grid grid = new Grid(3, 3);
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                grid.setCell(x, y, new AirCell(x, y, 0.0));
            }
        }

        grid.getCell(1, 1).setPollutionLevel(1.0);

        params.setWindDirection(WindDirection.EAST);
        params.setWindStrength(1.0);

        AbstractCell eastCell = grid.getCell(2, 1);
        AbstractCell westCell = grid.getCell(0, 1);

        eastCell.computeNextState(params, grid);
        westCell.computeNextState(params, grid);

        assertTrue(eastCell.getNextPollutionLevel() > westCell.getNextPollutionLevel(),
                "Downstream cell must receive higher pollution under easterly wind.");
        assertEquals(0.0, westCell.getNextPollutionLevel(), 0.0001,
                "Upstream cell should get zero contribution from easterly wind since its weight is clamped to 0.0.");
    }

    @Test
    public void testBinarySerialization() throws IOException, ClassNotFoundException {
        Simulation originalAbs = new Simulation(2, 2);
        originalAbs.getGrid().setCell(0, 0, new FactoryCell(0, 0, 1.0));
        originalAbs.getGrid().setCell(0, 1, new VegetationCell(0, 1, 0.5));
        originalAbs.getGrid().setCell(1, 0, new AirCell(1, 0, 0.1));
        originalAbs.getGrid().setCell(1, 1, new AirCell(1, 1, 0.0));

        originalAbs.getParameters().setDiffusionRate(0.44);
        originalAbs.recordStats(0.4, 2);

        File tempFile = new File("run/temp_simulation_test.cyp");
        tempFile.getParentFile().mkdirs();

        try {
            originalAbs.saveToFile(tempFile);
            assertTrue(tempFile.exists());

            Simulation restoredAbs = Simulation.loadFromFile(tempFile);
            assertNotNull(restoredAbs);

            assertEquals(0.44, restoredAbs.getParameters().getDiffusionRate(), 0.0001);
            assertEquals("FACTORY", restoredAbs.getGrid().getCell(0, 0).getName());
            assertEquals("VEGETATION", restoredAbs.getGrid().getCell(0, 1).getName());
            assertEquals("AIR", restoredAbs.getGrid().getCell(1, 0).getName());
            assertEquals("AIR", restoredAbs.getGrid().getCell(1, 1).getName());

            assertEquals(1.0, restoredAbs.getGrid().getCell(0, 0).getPollutionLevel());
            assertEquals(0.5, restoredAbs.getGrid().getCell(0, 1).getPollutionLevel());
            assertEquals(0.1, restoredAbs.getGrid().getCell(1, 0).getPollutionLevel());

            assertEquals(1, restoredAbs.getAvgPollutionHistory().size());
            assertEquals(0.4, restoredAbs.getAvgPollutionHistory().get(0), 0.0001);
            assertEquals(1, restoredAbs.getPollutedAirHistory().size());
            assertEquals(2, restoredAbs.getPollutedAirHistory().get(0));

        } finally {
            // Cleanup
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    public void testCellTypeStats() {
        Grid grid = new Grid(2, 3);
        grid.setCell(0, 0, new FactoryCell(0, 0, 1.0));
        grid.setCell(0, 1, new VegetationCell(0, 1, 0.5));
        grid.setCell(1, 0, new BuildingCell(1, 0, 0.0));
        grid.setCell(1, 1, new AirCell(1, 1, 0.5)); // Polluted Air cell

        Map<String, Integer> counts = grid.getCellTypeCounts();
        assertEquals(1, counts.get("FACTORY"));
        assertEquals(1, counts.get("VEGETATION"));
        assertEquals(1, counts.get("BUILDING"));
        assertEquals(2, counts.get("AIR"));
        assertEquals(1, counts.get("POLLUTED AIR"));

        Map<String, Double> percentages = grid.getCellTypePercentages();
        assertEquals(1.0 / 6.0, percentages.get("FACTORY"), 0.0001);
        assertEquals(1.0 / 6.0, percentages.get("VEGETATION"), 0.0001);
        assertEquals(1.0 / 6.0, percentages.get("BUILDING"), 0.0001);
        assertEquals(2.0 / 6.0, percentages.get("AIR"), 0.0001);
        assertEquals(1.0 / 6.0, percentages.get("POLLUTED AIR"), 0.0001);
    }
}
