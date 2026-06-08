package io.squid.cypgl.models;

import java.util.List;

/**
 * Concrete implementation of AbstractCell representing an AIR cell.
 * Handles advection-diffusion calculations and building reflection logic under wind conditions.
 *
 * @author TopeEstLa
 */
public class AirCell extends AbstractCell {
    private static final long serialVersionUID = 1L;

    public AirCell(int x, int y, double initialPollution) {
        super(x, y, initialPollution);
    }

    @Override
    public String getName() {
        return "AIR";
    }

    @Override
    public char getConsoleChar() {
        return '.';
    }

    @Override
    public void computeNextState(Grid grid, SimulationParameters params) {
        List<AbstractCell> neighbors = grid.getNeighbors(x, y);
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
                // Relative direction vector from the neighbor to the current cell (-dx, -dy)
                double dx = neighbor.getX() - x;
                double dy = neighbor.getY() - y;
                double vx = -dx;
                double vy = -dy;

                // Normalize neighbor-to-cell direction vector
                double lenV = Math.hypot(vx, vy);
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
            
            if (neighbor instanceof TreeCell) {
                neighborAbsorptionSum += params.getAbsorptionRate() * 0.5 * neighbor.getCustomRate();
            }
        }

        double avg = totalWeight == 0.0 ? pollutionLevel : sum / totalWeight;
        double nextPollution = pollutionLevel + params.getDiffusionRate() * (avg - pollutionLevel);

        nextPollution -= neighborAbsorptionSum;

        setNextPollutionLevel(nextPollution);
    }
}
