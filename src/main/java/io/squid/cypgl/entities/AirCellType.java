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
        double totalWeight = 0.0;
        double neighborAbsorptionSum = 0.0;

        WindDirection windDir = params.getWindDirection();
        double windStrength = params.getWindStrength();
        double wx = windDir.getDx();
        double wy = windDir.getDy();
        boolean hasWind = windDir != WindDirection.NONE && windStrength > 0.0;

        for (CellAbstraction neighbor : neighbors) {
            if (neighbor.getType() instanceof BuildingCellType) {
                continue;
            }

            double weight = 1.0;
            if (hasWind) {
                // Relative direction vector from the neighbor to the current cell (-dx, -dy)
                double dx = neighbor.getX() - cell.getX();
                double dy = neighbor.getY() - cell.getY();
                double vx = -dx;
                double vy = -dy;

                // Normalize neighbor-to-cell direction vector
                double lenV = Math.hypot(vx, vy); //sqrt(vx^2+vy^2)
                if (lenV > 0) {
                    vx /= lenV;
                    vy /= lenV;
                }

                // Normalize wind direction vector
                double lenW = Math.hypot(wx, wy);
                double wnx = wx / lenW;
                double wny = wy / lenW;

                // Dot product indicates alignment of wind direction with neighbor-to-cell vector
                double dot = vx * wnx + vy * wny;

                // Calculate wind influence weight: upstream neighbors get higher weight, downstream get lower
                weight = 1.0 + windStrength * dot;
                weight = Math.max(0.0, weight); // Guard against negative weights
            }

            sum += neighbor.getPollutionLevel() * weight;
            totalWeight += weight;
            
            if (neighbor.getType() instanceof TreeCellType) {
                neighborAbsorptionSum += params.getAbsorptionRate() * 0.5 * neighbor.getCustomRate();
            }
        }

        double avg = totalWeight == 0.0 ? cell.getPollutionLevel() : sum / totalWeight;
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
