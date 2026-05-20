package io.squid.cypgl.entities;

/**
 * @author TopeEstLa
 */
public class Grid {

    private Cell[][] cells;
    private int tick;

    public Grid(int width, int height) {
        this.cells = new Cell[width][height];
    }

    public void tick() {
        tick++;
    }

    public void setCell(int x, int y, Cell cell) {
        this.cells[x][y] = cell;
    }

    public Cell getCell(int x, int y) {
        return this.cells[x][y];
    }

}
