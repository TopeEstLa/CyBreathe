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

    @Override
    public void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params) {
        List<CellAbstraction> neighbors = grid.getNeighbors(cell.getX(), cell.getY());
        double sum = 0.0;
        double neighborGenerationSum = 0.0;
        double neighborAbsorptionSum = 0.0;
        
        for (CellAbstraction neighbor : neighbors) {
            sum += neighbor.getPollutionLevel();
            if (neighbor.getType() instanceof FactoryCellType) {
                // Factory customRate determines its pollution output
                neighborGenerationSum += params.getGenerationRate() * neighbor.getCustomRate();
            } else if (neighbor.getType() instanceof TreeCellType) {
                // Tree customRate determines its absorption strength
                neighborAbsorptionSum += params.getAbsorptionRate() * 0.5 * neighbor.getCustomRate();
            }
        }
        
        double avg = neighbors.isEmpty() ? cell.getPollutionLevel() : sum / neighbors.size();
        double nextPollution = cell.getPollutionLevel() + params.getDiffusionRate() * (avg - cell.getPollutionLevel());
        
        // Add neighboring factory emissions and subtract neighboring tree absorption
        nextPollution += neighborGenerationSum;
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
