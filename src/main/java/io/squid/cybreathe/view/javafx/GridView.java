package io.squid.cybreathe.view.javafx;

import io.squid.cybreathe.controller.javafx.CellController;
import io.squid.cybreathe.controller.javafx.GridController;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.util.function.Supplier;

/**
 * JavaFX grid panel containing CellPresentations. Handles mouse paint brushes (drag, zone, click).
 *
 * @author TopeEstLa
 */
public class GridView extends GridPane {

    private GridController gridController;

    // Suppliers to fetch active selection from SimulationPresentation at runtime
    private Supplier<String> activeBrushTypeSupplier; // "AIR", "VEGETATION", "FACTORY", "BUILDING"
    private Supplier<String> activeBrushModeSupplier; // "BRUSH", "ZONE", "INDIVIDUAL"
    private Supplier<Double> activeCustomRateSupplier;

    // Track state for Zone selection box
    private int zoneStartX = -1;
    private int zoneStartY = -1;
    private boolean dragDetected = false;
    private boolean zoneApplied = false;

    /**
     * Initializes the GridView with its controller and the configuration suppliers.
     *
     * @param control                  the grid controller mediating cell model updates
     * @param activeBrushTypeSupplier  supplier for the current active cell brush type (e.g. "AIR", "VEGETATION")
     * @param activeBrushModeSupplier  supplier for the current active brush mode (e.g. "BRUSH", "ZONE", "INDIVIDUAL")
     * @param activeCustomRateSupplier supplier for the current active custom rate parameter
     */
    public void initializeGrid(
            GridController control,
            Supplier<String> activeBrushTypeSupplier,
            Supplier<String> activeBrushModeSupplier,
            Supplier<Double> activeCustomRateSupplier) {

        this.gridController = control;
        this.activeBrushTypeSupplier = activeBrushTypeSupplier;
        this.activeBrushModeSupplier = activeBrushModeSupplier;
        this.activeCustomRateSupplier = activeCustomRateSupplier;

        rebuildDisplay();
    }

    /**
     * Rebuilds the visual grid by clear-adding all individual CellView children
     * and setting up their mouse and drag event handlers.
     */
    public void rebuildDisplay() {
        getChildren().clear();
        if (gridController == null) return;

        int w = gridController.getWidth();
        int h = gridController.getHeight();

        double cellSize = Math.clamp(600.0 / Math.max(w, h), 10.0, 40.0);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                CellController cellCtrl = gridController.getCellControl(x, y);

                CellView cellPres = new CellView(cellSize);
                cellCtrl.setPresentation(cellPres);

                final int finalX = x;
                final int finalY = y;

                cellPres.setOnMousePressed(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        String mode = activeBrushModeSupplier.get();
                        if ("ZONE".equals(mode)) {
                            zoneStartX = finalX;
                            zoneStartY = finalY;
                            dragDetected = false;
                            zoneApplied = false;
                        } else {
                            applyActivePaint(finalX, finalY);
                        }
                    }
                });

                cellPres.setOnDragDetected(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        String mode = activeBrushModeSupplier.get();
                        if ("ZONE".equals(mode)) {
                            dragDetected = true;
                        }
                        cellPres.startFullDrag();
                    }
                });

                cellPres.setOnMouseDragEntered(e -> {
                    String mode = activeBrushModeSupplier.get();
                    if ("BRUSH".equals(mode)) {
                        applyActivePaint(finalX, finalY);
                    } else if ("ZONE".equals(mode)) {
                        if (zoneStartX != -1 && zoneStartY != -1) {
                            highlightZone(zoneStartX, zoneStartY, finalX, finalY);
                        }
                    }
                });

                cellPres.setOnMouseDragReleased(e -> {
                    if ("ZONE".equals(activeBrushModeSupplier.get())) {
                        if (zoneStartX != -1 && zoneStartY != -1) {
                            applyZonePaint(zoneStartX, zoneStartY, finalX, finalY);
                        }
                    }
                });

                cellPres.setOnMouseReleased(e -> {
                    if (e.getButton() == MouseButton.PRIMARY && "ZONE".equals(activeBrushModeSupplier.get())) {
                        if (!dragDetected) {
                            if (zoneStartX != -1 && zoneStartY != -1) {
                                applyZonePaint(zoneStartX, zoneStartY, finalX, finalY);
                            }
                        } else {
                            javafx.application.Platform.runLater(() -> {
                                if (!zoneApplied && zoneStartX != -1) {
                                    clearHighlight();
                                    zoneStartX = -1;
                                    zoneStartY = -1;
                                }
                            });
                        }
                    }
                });

                add(cellPres, x, y);
            }
        }
    }

    /**
     * Applies the active paint tool to a rectangular zone defined by start and end coordinates.
     *
     * @param startX the starting x-coordinate of the rectangle
     * @param startY the starting y-coordinate of the rectangle
     * @param endX   the ending x-coordinate of the rectangle
     * @param endY   the ending y-coordinate of the rectangle
     */
    private void applyZonePaint(int startX, int startY, int endX, int endY) {
        String type = activeBrushTypeSupplier.get();
        gridController.applyZone(startX, startY, endX, endY, type);

        int minX = Math.max(0, Math.min(startX, endX));
        int maxX = Math.min(gridController.getWidth() - 1, Math.max(startX, endX));
        int minY = Math.max(0, Math.min(startY, endY));
        int maxY = Math.min(gridController.getHeight() - 1, Math.max(startY, endY));

        Double rate = activeCustomRateSupplier.get();
        for (int zx = minX; zx <= maxX; zx++) {
            for (int zy = minY; zy <= maxY; zy++) {
                CellController zctrl = gridController.getCellControl(zx, zy);
                if (zctrl != null && rate != null) {
                    zctrl.setCustomRate(rate);
                }
            }
        }

        clearHighlight();
        zoneStartX = -1;
        zoneStartY = -1;
        zoneApplied = true;
    }

    /**
     * Highlights the rectangular area during active zone drag.
     *
     * @param startX the starting x-coordinate
     * @param startY the starting y-coordinate
     * @param endX   the ending x-coordinate
     * @param endY   the ending y-coordinate
     */
    private void highlightZone(int startX, int startY, int endX, int endY) {
        int minX = Math.max(0, Math.min(startX, endX));
        int maxX = Math.min(gridController.getWidth() - 1, Math.max(startX, endX));
        int minY = Math.max(0, Math.min(startY, endY));
        int maxY = Math.min(gridController.getHeight() - 1, Math.max(startY, endY));

        int w = gridController.getWidth();
        int h = gridController.getHeight();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                CellController ctrl = gridController.getCellControl(x, y);
                if (ctrl != null) {
                    CellView view = ctrl.getPresentation();
                    if (view != null) {
                        boolean inZone = (x >= minX && x <= maxX && y >= minY && y <= maxY);
                        view.setHighlighted(inZone);
                    }
                }
            }
        }
    }

    /**
     * Clears highlights from all grid cells.
     */
    private void clearHighlight() {
        int w = gridController.getWidth();
        int h = gridController.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                CellController ctrl = gridController.getCellControl(x, y);
                if (ctrl != null) {
                    CellView view = ctrl.getPresentation();
                    if (view != null) {
                        view.setHighlighted(false);
                    }
                }
            }
        }
    }

    /**
     * Applies the active paint type and custom rate to a single cell coordinate.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     */
    private void applyActivePaint(int x, int y) {
        String brushType = activeBrushTypeSupplier.get();
        CellController ctrl = gridController.getCellControl(x, y);
        if (ctrl != null && brushType != null) {
            gridController.setCellType(x, y, brushType);

            // Assign customRate from active brush settings
            Double rate = activeCustomRateSupplier.get();
            if (rate != null) {
                ctrl.setCustomRate(rate);
            } else {
                ctrl.updatePresentation();
            }
        }
    }
}
