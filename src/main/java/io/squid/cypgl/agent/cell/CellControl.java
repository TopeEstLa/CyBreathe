package io.squid.cypgl.agent.cell;

import io.squid.cypgl.agent.grid.GridAbstraction;
import io.squid.cypgl.model.CellType;
import io.squid.cypgl.model.SimulationParameters;

/**
 * Control layer in the PAC architecture for a Cell agent.
 * Acts as the mediator between CellAbstraction (data) and CellPresentation (UI).
 * Coordinates local cell logic ticks and user interactions.
 * 
 * @author TopeEstLa
 */
public class CellControl {
    
    private final CellAbstraction abstraction;
    private CellPresentation presentation; // Optional, null in CLI mode

    public CellControl(CellAbstraction abstraction) {
        this.abstraction = abstraction;
    }

    public CellAbstraction getAbstraction() {
        return abstraction;
    }

    public CellPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(CellPresentation presentation) {
        this.presentation = presentation;
        updatePresentation(); // Initial styling
    }

    /**
     * Executes the cell-type-specific rules to compute the double-buffered next state.
     */
    public void computeNextState(GridAbstraction grid, SimulationParameters params) {
        abstraction.getType().computeNextState(abstraction, grid, params);
    }

    /**
     * Commits the double-buffered next state to the active state and updates the UI representation.
     */
    public void commitState() {
        abstraction.getType().commitState(abstraction);
        updatePresentation();
    }

    /**
     * Programmatically changes the cell's active type (e.g., via a user click/brush event).
     */
    public void setCellType(CellType newType) {
        abstraction.setType(newType);
        abstraction.resetNextBuffer();
        updatePresentation();
    }

    /**
     * Programmatically changes the cell's active pollution level.
     */
    public void setPollution(double pollution) {
        abstraction.setPollutionLevel(pollution);
        abstraction.setNextPollutionLevel(pollution);
        updatePresentation();
    }

    /**
     * Programmatically replants a dead tree with a healthy green tree.
     */
    public void replantTree(CellType treeType) {
        abstraction.setType(treeType);
        abstraction.setHealth(1.0);
        abstraction.setPollutionLevel(0.0);
        abstraction.resetNextBuffer();
        updatePresentation();
    }

    /**
     * Triggers the Presentation layer (if active) to redraw reflecting current Abstraction data.
     */
    public void updatePresentation() {
        if (presentation != null) {
            presentation.draw(abstraction.getType(), abstraction.getPollutionLevel(), abstraction.getHealth());
        }
    }
}
