package io.squid.cypgl.entities;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.grid.GridAbstraction;

import java.io.Serializable;

/**
 * Strategy pattern interface for cell behaviors.
 * Implementing classes define how cells of a specific type interact with their neighbors,
 * update their pollution level, and handle internal properties like health and age.
 *
 * @author TopeEstLa
 */
public interface CellType extends Serializable {

    /**
     * @return The display name of the cell type (e.g. "AIR", "TREE", "FACTORY").
     */
    String getName();

    /**
     * @return Single character representation of this cell type for CLI view.
     */
    char getConsoleChar();

    /**
     * Phase 1 of simulation tick: Calculates the next state of the cell (pollution, health, type changes)
     * and stores it in double-buffered fields (e.g. nextPollutionLevel) to avoid update-order dependency.
     *
     * @param cell   The cell being updated.
     * @param grid   The overall grid model to fetch neighbor states.
     * @param params Global simulation parameters.
     */
    void computeNextState(CellAbstraction cell, GridAbstraction grid, SimulationParameters params);

    /**
     * Phase 2 of simulation tick: Commits the computed double-buffered state to the active state.
     *
     * @param cell The cell committing its state.
     */
    void commitState(CellAbstraction cell);
}
