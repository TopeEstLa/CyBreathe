package io.squid.cypgl.models;

/**
 * Concrete implementation of AbstractCell representing a TREE cell.
 * Absorbs pollution from its own cell.
 *
 * @author TopeEstLa
 */
public class TreeCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    public TreeCell(int x, int y, double initialPollution) {
        super(x, y, initialPollution);
    }

    @Override
    public String getName() {
        return "TREE";
    }

    @Override
    public char getConsoleChar() {
        return 'T';
    }

    @Override
    public void computeNextState(GridAbstraction grid, SimulationParameters params) {
        double nextPollution = pollutionLevel - (params.getAbsorptionRate() * getCustomRate());
        setNextPollutionLevel(Math.max(0.0, nextPollution));
    }
}
