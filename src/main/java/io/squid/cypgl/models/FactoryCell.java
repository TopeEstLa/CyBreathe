package io.squid.cypgl.models;

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
    public char getConsoleChar() {
        return '#';
    }

    @Override
    public void computeNextState(GridAbstraction grid, SimulationParameters params) {
        setNextPollutionLevel(getCustomRate());
    }

    @Override
    public void commitState() {
        super.commitState();
        // Factory locks its active pollution level at customRate
        setPollutionLevel(getCustomRate());
    }
}
