package io.squid.cypgl.models;

import io.squid.cypgl.models.cells.AirCell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
}
