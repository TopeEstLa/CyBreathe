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
        int factoryCount = 0;
        int treeCount = 0;
        
        for (CellAbstraction neighbor : neighbors) {
            sum += neighbor.getPollutionLevel();
            if (neighbor.getType() instanceof FactoryCellType) {
                factoryCount++;
            } else if (neighbor.getType() instanceof TreeCellType) {
                treeCount++;
            }
        }
        
        double avg = neighbors.isEmpty() ? cell.getPollutionLevel() : sum / neighbors.size();
        double nextPollution = cell.getPollutionLevel() + params.getDiffusionRate() * (avg - cell.getPollutionLevel());
        
        // Factories actively generate/inject pollution to adjacent air
        nextPollution += factoryCount * params.getGenerationRate();
        
        // Trees absorb some pollution from adjacent air
        nextPollution -= treeCount * (params.getAbsorptionRate() * 0.5);
        
        cell.setNextPollutionLevel(nextPollution);
        cell.setNextType(this);
    }

    @Override
    public void commitState(CellAbstraction cell) {
        cell.setPollutionLevel(cell.getNextPollutionLevel());
        cell.setType(cell.getNextType());
    }
}
