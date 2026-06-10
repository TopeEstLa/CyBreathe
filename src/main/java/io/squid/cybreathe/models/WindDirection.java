package io.squid.cybreathe.models;

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

    /**
     * Constructs a WindDirection enum value with the specified grid offset components.
     *
     * @param dx the horizontal offset step (-1, 0, or 1)
     * @param dy the vertical offset step (-1, 0, or 1)
     */
    WindDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Gets the horizontal offset step of the wind.
     *
     * @return the x-coordinate offset
     */
    public int getDx() {
        return dx;
    }

    /**
     * Gets the vertical offset step of the wind.
     *
     * @return the y-coordinate offset
     */
    public int getDy() {
        return dy;
    }
}
