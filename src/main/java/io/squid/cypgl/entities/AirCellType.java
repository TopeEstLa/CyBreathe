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
        double neighborAbsorptionSum = 0.0;
        
        for (CellAbstraction neighbor : neighbors) {
            sum += neighbor.getPollutionLevel();
            if (neighbor.getType() instanceof TreeCellType) {
                // Tree customRate determines its absorption strength
                neighborAbsorptionSum += params.getAbsorptionRate() * 0.5 * neighbor.getCustomRate();
            }
        }
        
        double neighborGenerationSum = 0.0;
        int cx = cell.getX();
        int cy = cell.getY();
        int maxRadius = 3;
        int startX = Math.max(0, cx - maxRadius);
        int endX = Math.min(grid.getWidth() - 1, cx + maxRadius);
        int startY = Math.max(0, cy - maxRadius);
        int endY = Math.min(grid.getHeight() - 1, cy + maxRadius);

        for (int nx = startX; nx <= endX; nx++) {
            for (int ny = startY; ny <= endY; ny++) {
                if (nx == cx && ny == cy) continue;
                CellAbstraction other = grid.getCell(nx, ny);
                if (other != null && other.getType() instanceof FactoryCellType) {
                    double R = other.getCustomRate();
                    int d = Math.max(Math.abs(nx - cx), Math.abs(ny - cy)); // Chebyshev distance
                    
                    // The factory only emits directly to distance d if its customRate supports that radius
                    if (d <= Math.ceil(R)) {
                        neighborGenerationSum += (params.getGenerationRate() * R) / (d * d);
                    }
                }
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
