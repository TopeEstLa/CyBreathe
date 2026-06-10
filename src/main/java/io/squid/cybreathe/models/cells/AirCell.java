package io.squid.cybreathe.models.cells;

import io.squid.cybreathe.models.AbstractCell;
import io.squid.cybreathe.models.Grid;
import io.squid.cybreathe.models.SimulationParameters;
import io.squid.cybreathe.models.WindDirection;

import java.util.List;

/**
 * Concrete implementation of AbstractCell representing an AIR cell.
 * Handles advection-diffusion calculations and building reflection logic under wind conditions.
 *
 * @author TopeEstLa
 */
public class AirCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an AirCell at the specified coordinates with an initial pollution level.
     *
     * @param x                the x-coordinate of the cell
     * @param y                the y-coordinate of the cell
     * @param initialPollution the initial pollution level
     */
    public AirCell(int x, int y, double initialPollution) {
        super(x, y, initialPollution);
    }

    /**
     * Constructs an AirCell at the specified coordinates with a default pollution level of 0.0.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     */
    public AirCell(int x, int y) {
        this(x, y, 0.0);
    }

    /**
     * Gets the name of the cell type.
     *
     * @return "AIR"
     */
    @Override
    public String getName() {
        return "AIR";
    }

    /**
     * Calculates the next state of the air cell, applying wind-weighted advection-diffusion
     * and subtracting neighboring vegetation absorption.
     *
     * @param params the simulation parameters to use for calculations
     * @param grid   the grid containing this cell and its neighbors
     * @param tick   the current simulation tick count
     */
    @Override
    public void computeNextState(SimulationParameters params, Grid grid, int tick) {
        List<AbstractCell> neighbors = grid.getNeighbors(getX(), getY());
        double sum = 0.0;
        double totalWeight = 0.0;
        double neighborAbsorptionSum = 0.0;

        WindDirection windDir = params.getWindDirection();
        double windStrength = params.getWindStrength();
        double wx = windDir.getDx();
        double wy = windDir.getDy();
        boolean hasWind = windDir != WindDirection.NONE && windStrength > 0.0;

        for (AbstractCell neighbor : neighbors) {
            if (neighbor instanceof BuildingCell) {
                continue;
            }

            double weight = 1.0;
            if (hasWind) {
                double dx = neighbor.getX() - getX();
                double dy = neighbor.getY() - getY();
                double vx = -dx;
                double vy = -dy;

                double lenV = Math.hypot(vx, vy);
                if (lenV > 0) {
                    vx /= lenV;
                    vy /= lenV;
                }

                double lenW = Math.hypot(wx, wy);
                double wnx = wx / lenW;
                double wny = wy / lenW;

                double dot = vx * wnx + vy * wny;

                weight = 1.0 + windStrength * dot;
                weight = Math.max(0.0, weight);
            }

            sum += neighbor.getPollutionLevel() * weight;
            totalWeight += weight;

            if (neighbor instanceof VegetationCell) {
                neighborAbsorptionSum += params.getAbsorptionRate() * 0.5 * neighbor.getCustomRate();
            }
        }

        double avg = totalWeight == 0.0 ? getPollutionLevel() : sum / totalWeight;
        double nextPollution = getPollutionLevel() + params.getDiffusionRate() * (avg - getPollutionLevel());

        nextPollution -= neighborAbsorptionSum;

        setNextPollutionLevel(nextPollution);
    }
}
