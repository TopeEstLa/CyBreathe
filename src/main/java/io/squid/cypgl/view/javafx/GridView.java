package io.squid.cypgl.view.javafx;

import io.squid.cypgl.controller.javafx.CellController;
import io.squid.cypgl.controller.javafx.GridController;
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
     * Constructs the visual representation of the grid board.
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
