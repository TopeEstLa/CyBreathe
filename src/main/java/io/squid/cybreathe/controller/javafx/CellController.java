package io.squid.cybreathe.controller.javafx;

import io.squid.cybreathe.models.AbstractCell;
import io.squid.cybreathe.models.Grid;
import io.squid.cybreathe.models.SimulationParameters;
import io.squid.cybreathe.view.javafx.CellView;

/**
 * Controller mediating communication between AbstractCell model and JavaFX CellPresentation view.
 *
 * @author TopeEstLa
 */
public class CellController {

    private AbstractCell abstraction;
    private CellView presentation;

    /**
     * Constructs a CellController with the given cell model abstraction.
     *
     * @param abstraction the cell model abstraction
     */
    public CellController(AbstractCell abstraction) {
        this.abstraction = abstraction;
    }

    /**
     * Gets the cell model abstraction.
     *
     * @return the cell model
     */
    public AbstractCell getAbstraction() {
        return abstraction;
    }

    /**
     * Gets the cell view presentation.
     *
     * @return the cell view
     */
    public CellView getPresentation() {
        return presentation;
    }

    /**
     * Sets the cell view presentation and updates it.
     *
     * @param presentation the cell view presentation to associate with this controller
     */
    public void setPresentation(CellView presentation) {
        this.presentation = presentation;
        updatePresentation();
    }

    /**
     * Triggers the cell model to compute its double-buffered next state.
     *
     * @param grid   the grid containing this cell and its neighbors
     * @param params the global simulation parameters
     */
    public void computeNextState(Grid grid, SimulationParameters params) {
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
     *
     * @param newCell the new cell model instance
     */
    public void setCellType(AbstractCell newCell) {
        this.abstraction = newCell;
        updatePresentation();
    }

    /**
     * Changes the pollution level directly.
     *
     * @param pollution the new pollution level
     */
    public void setPollution(double pollution) {
        abstraction.setPollutionLevel(pollution);
        abstraction.setNextPollutionLevel(pollution);
        updatePresentation();
    }

    /**
     * Changes the custom rate directly.
     *
     * @param rate the new custom rate
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
