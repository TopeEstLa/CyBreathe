package io.squid.cypgl.entities;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;

/**
 * Concrete implementation of CellType for FACTORY cells.
 * Factories act as continuous, infinite sources of air pollution.
 * They lock their own pollution level at maximum (1.0) and emit smoke into neighbors.
 * 
 * @author TopeEstLa
 */
public class FactoryCellType implements CellType {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "FACTORY";
    }

    @Override
    public char getConsoleChar() {
        return '#';
    }

    @Override
    public void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params) {
        // Factory pollution is locked at maximum
        cell.setNextPollutionLevel(1.0);
        cell.setNextType(this);
    }

    @Override
    public void commitState(CellAbstraction cell) {
        cell.setPollutionLevel(1.0); // Ensure it is exactly 1.0
        cell.setType(cell.getNextType());
    }
}
