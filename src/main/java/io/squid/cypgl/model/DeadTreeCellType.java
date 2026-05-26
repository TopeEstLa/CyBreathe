package io.squid.cypgl.model;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;
import java.util.List;

/**
 * Concrete implementation of CellType for DEAD_TREE cells.
 * Created when a TREE cell dies due to pollution exposure.
 * It provides no pollution absorption and behaves passively like AIR for diffusion.
 * 
 * @author TopeEstLa
 */
public class DeadTreeCellType implements CellType {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "DEAD_TREE";
    }

    @Override
    public char getConsoleChar() {
        return 'x';
    }

    @Override
    public void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params) {
        // Dead trees act like AIR cells for diffusion (they let pollution flow through them)
        // but they do not absorb any pollution themselves.
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
        
        // Factories inject pollution
        nextPollution += factoryCount * params.getGenerationRate();
        
        // Neighbors (if trees) absorb from us
        nextPollution -= treeCount * (params.getAbsorptionRate() * 0.5);

        cell.setNextPollutionLevel(nextPollution);
        cell.setNextType(this);
        cell.setNextHealth(0.0); // Health stays at 0.0 unless replanted
    }

    @Override
    public void commitState(CellAbstraction cell) {
        cell.setPollutionLevel(cell.getNextPollutionLevel());
        cell.setType(cell.getNextType());
        cell.setHealth(0.0);
        cell.incrementAge();
    }
}
