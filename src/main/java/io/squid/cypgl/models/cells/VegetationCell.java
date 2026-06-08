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

    public VegetationCell(int x, int y, double initialPollution) {
        super(x, y, initialPollution);
    }

    @Override
    public String getName() {
        return "VEGETATION";
    }

    @Override
    public void computeNextState(Grid grid, SimulationParameters params) {
        double nextPollution = pollutionLevel - (params.getAbsorptionRate() * getCustomRate());
        setNextPollutionLevel(Math.max(0.0, nextPollution));
    }
}
