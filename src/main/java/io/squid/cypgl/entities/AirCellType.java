package io.squid.cypgl.entities;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;

import java.io.Serial;
import java.util.List;

/**
 * Concrete implementation of CellType for AIR cells.
 * Air cells permit standard atmospheric diffusion of pollution across their neighbors.
 *
 * @author TopeEstLa
 */
public class AirCellType implements CellType {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "AIR";
    }

    @Override
    public char getConsoleChar() {
        return '.';
    }

    /**
     * nextPollutionState = current + (diffuRate * (avg[neighbor pollution] - current))
     * nextPollutionState - nearAbsorption
     * @param cell   The cell being updated.
     * @param grid   The overall grid model to fetch neighbor states.
     * @param params Global simulation parameters.
     */
    @Override
    public void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params) {
        List<CellAbstraction> neighbors = grid.getNeighbors(cell.getX(), cell.getY());
        double sum = 0.0;
        int activeNeighborsCount = 0;
        double neighborAbsorptionSum = 0.0;

        for (CellAbstraction neighbor : neighbors) {
            if (neighbor.getType() instanceof BuildingCellType) {
                continue;
            }
            sum += neighbor.getPollutionLevel();
            activeNeighborsCount++;
            
            if (neighbor.getType() instanceof TreeCellType) {
                neighborAbsorptionSum += params.getAbsorptionRate() * 0.5 * neighbor.getCustomRate();
            }
        }

        double avg = activeNeighborsCount == 0 ? cell.getPollutionLevel() : sum / activeNeighborsCount;
        double nextPollution = cell.getPollutionLevel() + params.getDiffusionRate() * (avg - cell.getPollutionLevel());

        nextPollution -= neighborAbsorptionSum;

        cell.setNextPollutionLevel(nextPollution);
        cell.setNextType(this);
    }

    @Override
    public void commitState(CellAbstraction cell) {
        cell.setPollutionLevel(cell.getNextPollutionLevel());
        cell.setType(cell.getNextType());
    }
}
