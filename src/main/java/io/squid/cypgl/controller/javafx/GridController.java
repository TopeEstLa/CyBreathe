package io.squid.cypgl.controller.javafx;

import io.squid.cypgl.models.*;
import io.squid.cypgl.view.javafx.GridPresentation;

/**
 * Controller coordinating all grid CellControl elements for JavaFX GUI.
 *
 * @author TopeEstLa
 */
public class GridController {

    private final Grid abstraction;
    private final CellController[][] cellControllers;
    private GridPresentation presentation;

    public GridController(Grid abstraction) {
        this.abstraction = abstraction;
        int w = abstraction.getWidth();
        int h = abstraction.getHeight();
        this.cellControllers = new CellController[w][h];

        // Initialize child cell controls
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                AbstractCell cellAbs = abstraction.getCell(x, y);
                if (cellAbs == null) {
                    throw new IllegalStateException("Grid cells not initialized in Abstraction!");
                }
                this.cellControllers[x][y] = new CellController(cellAbs);
            }
        }
    }

    public int getWidth() {
        return abstraction.getWidth();
    }

    public int getHeight() {
        return abstraction.getHeight();
    }

    public GridPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(GridPresentation presentation) {
        this.presentation = presentation;
    }

    public CellController[][] getCellControls() {
        return cellControllers;
    }

    public CellController getCellControl(int x, int y) {
        if (x >= 0 && x < abstraction.getWidth() && y >= 0 && y < abstraction.getHeight()) {
            return cellControllers[x][y];
        }
        return null;
    }

    /**
     * Ticks Phase 1: Triggers all cells to compute their double-buffered next states.
     */
    public void computeNextStates(SimulationParameters params) {
        int w = abstraction.getWidth();
        int h = abstraction.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                cellControllers[x][y].computeNextState(abstraction, params);
            }
        }
    }

    /**
     * Ticks Phase 2: Commits the double-buffered states.
     */
    public void commitStates() {
        int w = abstraction.getWidth();
        int h = abstraction.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                cellControllers[x][y].commitState();
            }
        }
    }

    /**
     * Applies a cell brush modification at coordinates (x, y).
     */
    public void setCellType(int x, int y, String typeName) {
        CellController ctrl = getCellControl(x, y);
        if (ctrl != null) {
            double currentPollution = ctrl.getAbstraction().getPollutionLevel();
            double currentCustomRate = ctrl.getAbstraction().getCustomRate();

            AbstractCell newCell = switch (typeName.toUpperCase()) {
                case "AIR" -> new AirCell(x, y, currentPollution);
                case "TREE" -> new TreeCell(x, y, currentPollution);
                case "FACTORY" -> new FactoryCell(x, y, currentPollution);
                case "BUILDING" -> new BuildingCell(x, y, currentPollution);
                default -> new AirCell(x, y, currentPollution);
            };
            newCell.setCustomRate(currentCustomRate);

            abstraction.setCell(x, y, newCell);
            ctrl.setCellType(newCell);
        }
    }

    /**
     * Applies a zone-selection brush filling a rectangle with a cell type.
     */
    public void applyZone(int startX, int startY, int endX, int endY, String typeName) {
        int minX = Math.max(0, Math.min(startX, endX));
        int maxX = Math.min(abstraction.getWidth() - 1, Math.max(startX, endX));
        int minY = Math.max(0, Math.min(startY, endY));
        int maxY = Math.min(abstraction.getHeight() - 1, Math.max(startY, endY));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                setCellType(x, y, typeName);
            }
        }
    }

    /**
     * Restores/recreates cell controls after deserializing a new GridAbstraction state.
     */
    public void rebuildCellControls() {
        int w = abstraction.getWidth();
        int h = abstraction.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                AbstractCell cellAbs = abstraction.getCell(x, y);
                this.cellControllers[x][y] = new CellController(cellAbs);
            }
        }
    }

    /**
     * Triggers updatePresentation on all individual CellControls.
     */
    public void updateAllCellPresentations() {
        int w = abstraction.getWidth();
        int h = abstraction.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (cellControllers[x][y] != null) {
                    cellControllers[x][y].updatePresentation();
                }
            }
        }
    }
}
