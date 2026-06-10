package io.squid.cypgl.models.cells;

import io.squid.cypgl.models.AbstractCell;
import io.squid.cypgl.models.Grid;
import io.squid.cypgl.models.SimulationParameters;

/**
 * Concrete implementation of AbstractCell representing a Vegetation cell.
 * Absorbs pollution from its own cell.
 *
 * @author TopeEstLa
 */
public class VegetationCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a VegetationCell at the specified coordinates with an initial pollution level.
     *
     * @param x                the x-coordinate of the cell
     * @param y                the y-coordinate of the cell
     * @param initialPollution the initial pollution level
     */
    public VegetationCell(int x, int y, double initialPollution) {
        super(x, y, initialPollution);
    }

    /**
     * Gets the name of the cell type.
     *
     * @return "VEGETATION"
     */
    @Override
    public String getName() {
        return "VEGETATION";
    }

    /**
     * Computes the next state of the cell by absorbing pollution from its own cell.
     *
     * @param grid   the grid containing this cell
     * @param params the simulation parameters controlling the absorption rate
     */
    @Override
    public void computeNextState(Grid grid, SimulationParameters params) {
        double nextPollution = pollutionLevel - (params.getAbsorptionRate() * getCustomRate());
        setNextPollutionLevel(Math.max(0.0, nextPollution));
    }
}
