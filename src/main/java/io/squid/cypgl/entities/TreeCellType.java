package io.squid.cypgl.entities;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;

/**
 * Concrete implementation of CellType for TREE cells.
 * Trees absorb pollution from their environment.
 * If exposed to excessive pollution over time, their health decays, and they eventually die (turning into DEAD_TREE).
 * If pollution remains low, they can recover health.
 *
 * @author TopeEstLa
 */
public class TreeCellType implements CellType {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "TREE";
    }

    @Override
    public char getConsoleChar() {
        return 'T';
    }

    @Override
    public void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params) {
        // Absorb pollution locally based on individual customRate multiplier
        double nextPollution = cell.getPollutionLevel() - (params.getAbsorptionRate() * cell.getCustomRate());
        cell.setNextPollutionLevel(Math.max(0.0, nextPollution));

        cell.setNextType(this);
    }

    @Override
    public void commitState(CellAbstraction cell) {
        cell.setPollutionLevel(cell.getNextPollutionLevel());
        cell.setType(cell.getNextType());
    }
}
