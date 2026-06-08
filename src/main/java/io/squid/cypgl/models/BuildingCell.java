package io.squid.cypgl.models;

/**
 * Concrete implementation of AbstractCell representing a solid BUILDING cell.
 * Prevents pollution diffusion and advection, keeping its level locked at 0.0.
 *
 * @author TopeEstLa
 */
public class BuildingCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    public BuildingCell(int x, int y, double initialPollution) {
        super(x, y, 0.0);
    }

    @Override
    public String getName() {
        return "BUILDING";
    }

    @Override
    public void computeNextState(Grid grid, SimulationParameters params) {
        setNextPollutionLevel(0.0);
    }
}
