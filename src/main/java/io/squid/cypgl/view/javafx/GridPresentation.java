package io.squid.cypgl.view.javafx;

import io.squid.cypgl.controller.javafx.CellControl;
import io.squid.cypgl.controller.javafx.GridControl;
import io.squid.cypgl.models.AbstractCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.util.function.Supplier;

/**
 * JavaFX grid panel containing CellPresentations. Handles mouse paint brushes (drag, zone, click).
 *
 * @author TopeEstLa
 */
public class GridPresentation extends GridPane {

    private GridControl gridControl;

    // Suppliers to fetch active selection from SimulationPresentation at runtime
    private Supplier<String> activeBrushTypeSupplier; // "AIR", "TREE", "FACTORY", "BUILDING"
    private Supplier<String> activeBrushModeSupplier; // "BRUSH", "ZONE", "INDIVIDUAL"
    private Supplier<Double> activeCustomRateSupplier;

    // Track state for Zone selection box
    private int zoneStartX = -1;
    private int zoneStartY = -1;

    public void initializeGrid(
            GridControl control,
            Supplier<String> activeBrushTypeSupplier,
            Supplier<String> activeBrushModeSupplier,
            Supplier<Double> activeCustomRateSupplier) {

        this.gridControl = control;
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
        if (gridControl == null) return;

        int w = gridControl.getWidth();
        int h = gridControl.getHeight();

        // Calculate a responsive cell size based on grid size
        double cellSize = Math.clamp(600.0 / Math.max(w, h), 10.0, 40.0);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                CellControl cellCtrl = gridControl.getCellControl(x, y);

                // Create individual visual node
                CellPresentation cellPres = new CellPresentation(cellSize);
                cellCtrl.setPresentation(cellPres);

                // Setup interactive mouse events on this specific cell coordinates
                final int finalX = x;
                final int finalY = y;

                cellPres.setOnMousePressed(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        String mode = activeBrushModeSupplier.get();
                        if ("ZONE".equals(mode)) {
                            // Record drag start coordinate
                            zoneStartX = finalX;
                            zoneStartY = finalY;
                        } else {
                            // Single click edits cell immediately
                            applyActivePaint(finalX, finalY);
                        }
                    }
                });

                cellPres.setOnDragDetected(e -> {
                    // Start full drag-over paint
                    cellPres.startFullDrag();
                });

                cellPres.setOnMouseDragEntered(e -> {
                    // Paint cell when dragged over in BRUSH mode
                    if ("BRUSH".equals(activeBrushModeSupplier.get())) {
                        applyActivePaint(finalX, finalY);
                    }
                });

                cellPres.setOnMouseReleased(e -> {
                    if (e.getButton() == MouseButton.PRIMARY && "ZONE".equals(activeBrushModeSupplier.get())) {
                        // Apply bulk zone rectangle on drag release
                        if (zoneStartX != -1 && zoneStartY != -1) {
                            String type = activeBrushTypeSupplier.get();
                            gridControl.applyZone(zoneStartX, zoneStartY, finalX, finalY, type);

                            int minX = Math.max(0, Math.min(zoneStartX, finalX));
                            int maxX = Math.min(gridControl.getWidth() - 1, Math.max(zoneStartX, finalX));
                            int minY = Math.max(0, Math.min(zoneStartY, finalY));
                            int maxY = Math.min(gridControl.getHeight() - 1, Math.max(zoneStartY, finalY));

                            Double rate = activeCustomRateSupplier.get();
                            for (int zx = minX; zx <= maxX; zx++) {
                                for (int zy = minY; zy <= maxY; zy++) {
                                    CellControl zctrl = gridControl.getCellControl(zx, zy);
                                    if (zctrl != null && rate != null) {
                                        zctrl.setCustomRate(rate);
                                    }
                                }
                            }

                            zoneStartX = -1;
                            zoneStartY = -1;
                        }
                    }
                });

                // Add node to grid layout (column = x, row = y)
                add(cellPres, x, y);
            }
        }
    }

    private void applyActivePaint(int x, int y) {
        String brushType = activeBrushTypeSupplier.get();
        CellControl ctrl = gridControl.getCellControl(x, y);
        if (ctrl != null && brushType != null) {
            gridControl.setCellType(x, y, brushType);

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
