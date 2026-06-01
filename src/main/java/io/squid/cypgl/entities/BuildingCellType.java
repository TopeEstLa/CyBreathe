package io.squid.cypgl.entities;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;

/**
 * Concrete implementation of CellType for BUILDING cells.
 * Buildings act as solid impermeable obstacles blocking the passage of pollution.
 * They lock their own pollution level at 0.0.
 * 
 * @author TopeEstLa
 */
public class BuildingCellType implements CellType {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "BUILDING";
    }

    @Override
    public char getConsoleChar() {
        return 'B';
    }

    @Override
    public void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params) {
        cell.setNextPollutionLevel(0.0);
        cell.setNextType(this);
    }

    @Override
    public void commitState(CellAbstraction cell) {
        cell.setPollutionLevel(0.0);
        cell.setType(cell.getNextType());
    }
}
