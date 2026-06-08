package io.squid.cypgl.models;

/**
 * Enumeration representing wind directions and their corresponding 2D grid offset vectors.
 * Implements standard directional mapping for Moor neighborhood advection.
 *
 * @author TopeEstLa
 */
public enum WindDirection {
    NONE(0, 0),
    NORTH(0, -1),
    NORTH_EAST(1, -1),
    EAST(1, 0),
    SOUTH_EAST(1, 1),
    SOUTH(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(-1, -1);

    private final int dx;
    private final int dy;

    WindDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
