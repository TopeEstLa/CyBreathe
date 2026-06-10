package io.squid.cybreathe.models;

import io.squid.cybreathe.models.cells.AirCell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraction model for the 2D grid, managing cells and neighbors.
 *
 * @author TopeEstLa
 */
public class Grid implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int width;
    private final int height;
    private final AbstractCell[][] cells;

    /**
     * Constructs a Grid with the specified dimensions and fills it with default clean AirCell instances.
     *
     * @param width  the width of the grid
     * @param height the height of the grid
     */
    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new AbstractCell[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new AirCell(x, y);
            }
        }
    }

    /**
     * Gets the width of the grid.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the grid.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the cell at the specified coordinates.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return the AbstractCell at (x, y), or null if the coordinates are out of bounds
     */
    public AbstractCell getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x][y];
        }
        return null;
    }

    /**
     * Sets the cell at the specified coordinates.
     *
     * @param x    the x-coordinate where the cell should be placed
     * @param y    the y-coordinate where the cell should be placed
     * @param cell the cell to set
     */
    public void setCell(int x, int y, AbstractCell cell) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[x][y] = cell;
        }
    }

    public void computeNextStates(Simulation simulation) {
        int w = getWidth();
        int h = getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                cells[x][y].computeNextState(simulation.getParameters(), simulation.getGrid(), simulation.getTickCount());
            }
        }
    }

    public void commitNextStates() {
        int w = getWidth();
        int h = getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                cells[x][y].commitState();
            }
        }
    }

    /**
     * Gets the Moore neighbors (8 orthogonal and diagonal directions) of the cell at (x, y),
     * ignoring any neighbors that are out of bounds.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return a list of neighboring cells
     */
    public List<AbstractCell> getNeighbors(int x, int y) {
        List<AbstractCell> neighbors = new ArrayList<>();

        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < dx.length; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                if (cells[nx][ny] != null) {
                    neighbors.add(cells[nx][ny]);
                }
            }
        }

        return neighbors;
    }

    /**
     * Calculates the count of cells for each type in the grid.
     * Splitting Air cells into clean "AIR" and "POLLUTED AIR" (pollution > 0).
     *
     * @return a map containing cell type names and their respective counts
     */
    public Map<String, Integer> getCellTypeCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("AIR", 0);
        counts.put("POLLUTED AIR", 0);
        counts.put("VEGETATION", 0);
        counts.put("FACTORY", 0);
        counts.put("BUILDING", 0);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                AbstractCell cell = cells[x][y];
                if (cell != null) {
                    String name = cell.getName().toUpperCase();
                    if ("AIR".equals(name) && cell.getPollutionLevel() > 0.0) {
                        name = "POLLUTED AIR";
                    }
                    counts.put(name, counts.getOrDefault(name, 0) + 1);
                }
            }
        }
        return counts;
    }

    /**
     * Calculates the percentage distribution of cells for each type in the grid.
     *
     * @return a map containing cell type names and their respective percentage values (0.0 to 1.0)
     */
    public Map<String, Double> getCellTypePercentages() {
        Map<String, Integer> counts = getCellTypeCounts();
        Map<String, Double> percentages = new LinkedHashMap<>();
        int total = width * height;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            percentages.put(entry.getKey(), total > 0 ? (double) entry.getValue() / total : 0.0);
        }
        return percentages;
    }
}

