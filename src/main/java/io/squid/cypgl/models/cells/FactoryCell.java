package io.squid.cypgl.models.cells;

import io.squid.cypgl.models.AbstractCell;
import io.squid.cypgl.models.Grid;
import io.squid.cypgl.models.SimulationParameters;

/**
 * Concrete implementation of AbstractCell representing a FACTORY cell.
 * Continuous source of pollution, locking its pollution level at its customRate.
 *
 * @author TopeEstLa
 */
public class FactoryCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    public FactoryCell(int x, int y, double initialPollution) {
        super(x, y, initialPollution);
    }

    @Override
    public String getName() {
        return "FACTORY";
    }

    @Override
    public void computeNextState(Grid grid, SimulationParameters params) {
        setNextPollutionLevel(getCustomRate());
    }

    @Override
    public void commitState() {
        super.commitState();
        // Factory locks its active pollution level at customRate
        setPollutionLevel(getCustomRate());
    }
}
