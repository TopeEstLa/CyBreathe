package io.squid.cypgl.agent.grid;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.cell.CellControl;
import io.squid.cypgl.entities.CellType;
import io.squid.cypgl.entities.SimulationParameters;
import java.util.Random;

/**
 * Control layer in the PAC architecture for the Grid agent.
 * Coordinates all individual child CellAgents and propagates ticks and user grid actions (brush, zone, random fill).
 * 
 * @author TopeEstLa
 */
public class GridControl {

    private final GridAbstraction abstraction;
    private GridPresentation presentation; // Optional, null in CLI mode
    private final CellControl[][] cellControls;

    public GridControl(GridAbstraction abstraction) {
        this.abstraction = abstraction;
        int w = abstraction.getWidth();
        int h = abstraction.getHeight();
        this.cellControls = new CellControl[w][h];

        // Initialize child cell agents
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                CellAbstraction cellAbs = abstraction.getCell(x, y);
                if (cellAbs == null) {
                    throw new IllegalStateException("Grid cells not initialized in Abstraction!");
                }
                this.cellControls[x][y] = new CellControl(cellAbs);
            }
        }
    }

    public GridAbstraction getAbstraction() {
        return abstraction;
    }

    public GridPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(GridPresentation presentation) {
        this.presentation = presentation;
    }

    public CellControl[][] getCellControls() {
        return cellControls;
    }

    public CellControl getCellControl(int x, int y) {
        if (x >= 0 && x < abstraction.getWidth() && y >= 0 && y < abstraction.getHeight()) {
            return cellControls[x][y];
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
                cellControls[x][y].computeNextState(abstraction, params);
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
                cellControls[x][y].commitState();
            }
        }
    }

    /**
     * Applies a cell brush modification at coordinates (x, y).
     */
    public void setCellType(int x, int y, CellType type) {
        CellControl ctrl = getCellControl(x, y);
        if (ctrl != null) {
            ctrl.setCellType(type);
        }
    }

    /**
     * Applies a zone-selection brush filling a rectangle with a cell type.
     */
    public void applyZone(int startX, int startY, int endX, int endY, CellType type) {
        int minX = Math.max(0, Math.min(startX, endX));
        int maxX = Math.min(abstraction.getWidth() - 1, Math.max(startX, endX));
        int minY = Math.max(0, Math.min(startY, endY));
        int maxY = Math.min(abstraction.getHeight() - 1, Math.max(startY, endY));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                setCellType(x, y, type);
            }
        }
    }

    /**
     * Performs a mass random spawn of a specific CellType across a percentage of empty (AIR) tiles.
     * 
     * @param type The CellType to spawn.
     * @param percentage Value from 0.0 to 1.0 representing coverage.
     */
    public void massSpawn(CellType type, double percentage) {
        Random rand = new Random();
        int w = abstraction.getWidth();
        int h = abstraction.getHeight();
        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // Spawn only on existing AIR cells to avoid overwriting factories/other structures
                if (abstraction.getCell(x, y).getType().getName().equals("AIR")) {
                    if (rand.nextDouble() < percentage) {
                        cellControls[x][y].setCellType(type);
                    }
                }
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
                CellAbstraction cellAbs = abstraction.getCell(x, y);
                this.cellControls[x][y] = new CellControl(cellAbs);
            }
        }
    }
}
