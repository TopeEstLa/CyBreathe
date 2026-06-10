package io.squid.cybreathe.models.cells;

import io.squid.cybreathe.models.AbstractCell;
import io.squid.cybreathe.models.Grid;
import io.squid.cybreathe.models.SimulationParameters;

/**
 * Concrete implementation of AbstractCell representing a FACTORY cell.
 * Continuous source of pollution, locking its pollution level at its customRate.
 *
 * @author TopeEstLa
 */
public class FactoryCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a FactoryCell at the specified coordinates with an initial pollution level.
     *
     * @param x                the x-coordinate of the cell
     * @param y                the y-coordinate of the cell
     * @param initialPollution the initial pollution level
     */
    public FactoryCell(int x, int y, double initialPollution) {
        super(x, y, initialPollution);
    }

    /**
     * Gets the name of the cell type.
     *
     * @return "FACTORY"
     */
    @Override
    public String getName() {
        return "FACTORY";
    }

    /**
     * Computes the next state of the cell by locking its pollution level at its custom rate.
     *
     * @param params the simulation parameters to use for calculations
     * @param grid   the grid containing this cell and its neighbors
     * @param tick   the current simulation tick count
     */
    @Override
    public void computeNextState(SimulationParameters params, Grid grid, int tick) {
        setNextPollutionLevel(getCustomRate());
    }

}
