package io.squid.cypgl.agent.grid;

import io.squid.cypgl.agent.cell.CellAbstraction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction layer in the PAC architecture for the Grid agent.
 * Manages the grid cell array and calculates local Bounded Moore neighborhood relationships.
 * 
 * @author TopeEstLa
 */
public class GridAbstraction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int width;
    private final int height;
    private final CellAbstraction[][] cells;

    public GridAbstraction(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new CellAbstraction[width][height];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public CellAbstraction getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x][y];
        }
        return null;
    }

    public void setCell(int x, int y, CellAbstraction cell) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[x][y] = cell;
        }
    }

    /**
     * Retrieves Bounded Moore neighbors (8 orthogonal and diagonal directions, ignoring out of bounds).
     */
    public List<CellAbstraction> getNeighbors(int x, int y) {
        List<CellAbstraction> neighbors = new ArrayList<>();
        
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < dx.length; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                neighbors.add(cells[nx][ny]);
            }
        }

        return neighbors;
    }
}
