package io.squid.cypgl.model;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;

/**
 * Concrete implementation of CellType for TREE cells.
 * Trees absorb pollution from their environment.
 * If exposed to excessive pollution over time, their health decays, and they eventually die (turning into DEAD_TREE).
 * If pollution remains low, they can recover health.
 * 
 * @author TopeEstLa
 */
public class TreeCellType implements CellType {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "TREE";
    }

    @Override
    public char getConsoleChar() {
        return 'T';
    }

    @Override
    public void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params) {
        // 1. Absorb pollution locally
        double nextPollution = cell.getPollutionLevel() - params.getAbsorptionRate();
        cell.setNextPollutionLevel(Math.max(0.0, nextPollution));

        // 2. Determine health changes based on exposure to pollution
        double nextHealth = cell.getHealth();
        if (cell.getPollutionLevel() > params.getTreePollutionThreshold()) {
            // Highly polluted: decay health
            nextHealth -= params.getTreeDecayRate();
        } else {
            // Clean air: recover health
            nextHealth += params.getTreeRecoveryRate();
        }
        nextHealth = Math.clamp(nextHealth, 0.0, 1.0);
        cell.setNextHealth(nextHealth);

        // 3. Handle death cycle
        if (nextHealth <= 0.0) {
            // Tree dies: transition to DEAD_TREE type
            cell.setNextType(new DeadTreeCellType());
            cell.setNextHealth(0.0);
        } else {
            cell.setNextType(this);
        }
    }

    @Override
    public void commitState(CellAbstraction cell) {
        cell.setPollutionLevel(cell.getNextPollutionLevel());
        cell.setType(cell.getNextType());
        cell.setHealth(cell.getNextHealth());
        cell.incrementAge();
    }
}
