package io.squid.cybreathe.controller.javafx;

import io.squid.cybreathe.models.AbstractCell;
import io.squid.cybreathe.models.Grid;
import io.squid.cybreathe.models.cells.AirCell;
import io.squid.cybreathe.models.cells.BuildingCell;
import io.squid.cybreathe.models.cells.FactoryCell;
import io.squid.cybreathe.models.cells.VegetationCell;

/**
 * Controller coordinating all grid CellControl elements for JavaFX GUI.
 *
 * @author TopeEstLa
 */
public class GridController {

    private final Grid abstraction;
    private final CellController[][] cellControllers;

    /**
     * Constructs a GridController for the given grid abstraction model and initializes
     * child CellController instances for all grid coordinates.
     *
     * @param abstraction the grid model containing the initial grid configuration
     * @throws IllegalStateException if any grid cell is not initialized in the grid model
     */
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

    /**
     * Gets the width of the grid model.
     *
     * @return the width of the grid
     */
    public int getWidth() {
        return abstraction.getWidth();
    }

    /**
     * Gets the height of the grid model.
     *
     * @return the height of the grid
     */
    public int getHeight() {
        return abstraction.getHeight();
    }

    /**
     * Gets the CellController for the cell at coordinates (x, y).
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return the associated CellController, or null if coordinates are out of bounds
     */
    public CellController getCellControl(int x, int y) {
        if (x >= 0 && x < abstraction.getWidth() && y >= 0 && y < abstraction.getHeight()) {
            return cellControllers[x][y];
        }
        return null;
    }

    /**
     * Updates the presentation (view) of all cells in the grid based on their current states.
     */
    public void updateCellPresentation() {
        for (int x = 0; x < abstraction.getWidth(); x++) {
            for (int y = 0; y < abstraction.getHeight(); y++) {
                if (cellControllers[x][y] != null) {
                    cellControllers[x][y].updatePresentation();
                }
            }
        }
    }

    /**
     * Applies a cell brush modification at coordinates (x, y).
     *
     * @param x        the x-coordinate of the target cell
     * @param y        the y-coordinate of the target cell
     * @param typeName the name of the new cell type (e.g. "AIR", "VEGETATION", "FACTORY", "BUILDING")
     */
    public void setCellType(int x, int y, String typeName) {
        CellController ctrl = getCellControl(x, y);
        if (ctrl != null) {
            double currentPollution = ctrl.getAbstraction().getPollutionLevel();
            double currentCustomRate = ctrl.getAbstraction().getCustomRate();

            AbstractCell newCell = switch (typeName.toUpperCase()) {
                case "AIR" -> new AirCell(x, y, currentPollution);
                case "VEGETATION" -> new VegetationCell(x, y, currentPollution);
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
     *
     * @param startX   the starting x-coordinate of the selection rectangle
     * @param startY   the starting y-coordinate of the selection rectangle
     * @param endX     the ending x-coordinate of the selection rectangle
     * @param endY     the ending y-coordinate of the selection rectangle
     * @param typeName the name of the cell type to fill the rectangle with
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
