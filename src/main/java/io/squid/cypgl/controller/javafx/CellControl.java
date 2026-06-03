package io.squid.cypgl.controller.javafx;

import io.squid.cypgl.models.*;
import io.squid.cypgl.view.javafx.CellPresentation;

/**
 * Controller mediating communication between AbstractCell model and JavaFX CellPresentation view.
 *
 * @author TopeEstLa
 */
public class CellControl {

    private AbstractCell abstraction;
    private CellPresentation presentation;

    public CellControl(AbstractCell abstraction) {
        this.abstraction = abstraction;
    }

    public AbstractCell getAbstraction() {
        return abstraction;
    }

    public CellPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(CellPresentation presentation) {
        this.presentation = presentation;
        updatePresentation();
    }

    /**
     * Triggers the cell model to compute its double-buffered next state.
     */
    public void computeNextState(GridAbstraction grid, SimulationParameters params) {
        abstraction.computeNextState(grid, params);
    }

    /**
     * Commits the computed double-buffered state and updates the UI visual display.
     */
    public void commitState() {
        abstraction.commitState();
        updatePresentation();
    }

    /**
     * Programmatically changes the cell type by swapping the subclass instance in-place.
     */
    public void setCellType(AbstractCell newCell) {
        this.abstraction = newCell;
        updatePresentation();
    }

    /**
     * Changes the pollution level directly.
     */
    public void setPollution(double pollution) {
        abstraction.setPollutionLevel(pollution);
        abstraction.setNextPollutionLevel(pollution);
        updatePresentation();
    }

    /**
     * Changes the custom rate directly.
     */
    public void setCustomRate(double rate) {
        abstraction.setCustomRate(rate);
        updatePresentation();
    }

    /**
     * Redraws the Presentation layer using current Abstraction state values.
     */
    public void updatePresentation() {
        if (presentation != null) {
            presentation.draw(abstraction.getName(), abstraction.getPollutionLevel(), abstraction.getCustomRate());
        }
    }
}
