package io.squid.cybreathe.models.cells;

import io.squid.cybreathe.models.AbstractCell;
import io.squid.cybreathe.models.Grid;
import io.squid.cybreathe.models.Simulation;
import io.squid.cybreathe.models.SimulationParameters;

/**
 * Concrete implementation of AbstractCell representing a solid BUILDING cell.
 * Prevents pollution diffusion and advection, keeping its level locked at 0.0.
 *
 * @author TopeEstLa
 */
public class BuildingCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a BuildingCell at the specified coordinates.
     * The pollution level is locked at 0.0.
     *
     * @param x                the x-coordinate of the cell
     * @param y                the y-coordinate of the cell
     * @param initialPollution ignored, as buildings always have 0.0 pollution
     */
    public BuildingCell(int x, int y, double initialPollution) {
        super(x, y, 0.0);
    }

    /**
     * Gets the name of the cell type.
     *
     * @return "BUILDING"
     */
    @Override
    public String getName() {
        return "BUILDING";
    }

    /**
     * Keeps the next state pollution level locked at 0.0.
     *
     * @param params the simulation parameters to use for calculations
     * @param grid   the grid containing this cell and its neighbors
     * @param tick   the current simulation tick count
     */
    @Override
    public void computeNextState(SimulationParameters params, Grid grid, int tick) {
        setNextPollutionLevel(0.0);
    }
}
