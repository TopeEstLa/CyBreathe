package io.squid.cypgl.models;

import io.squid.cypgl.models.cells.AirCell;

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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public AbstractCell getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x][y];
        }
        return null;
    }

    public void setCell(int x, int y, AbstractCell cell) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[x][y] = cell;
        }
    }

    /**
     * Moore neighbors (8 orthogonal and diagonal directions, ignoring out of bounds).
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
     * Calculates the percentage of cells for each type in the grid.
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

