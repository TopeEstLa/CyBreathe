package io.squid.cypgl.agent.grid;

import io.squid.cypgl.agent.cell.CellAbstraction;
import io.squid.cypgl.agent.cell.CellControl;
import io.squid.cypgl.agent.cell.CellPresentation;
import io.squid.cypgl.entities.CellType;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import java.util.function.Supplier;

/**
 * Presentation layer in the PAC architecture for the Grid agent.
 * Implements a JavaFX GridPane containing cell nodes, and hooks up mouse interaction events
 * (Brush, Zone selection, and Individual clicks) for fluid real-time simulation paint actions.
 * 
 * @author TopeEstLa
 */
public class GridPresentation extends GridPane {

    private GridControl gridControl;
    
    // Suppliers to fetch active selection from SimulationControl at runtime
    private Supplier<CellType> activeBrushTypeSupplier;
    private Supplier<String> activeBrushModeSupplier; // "BRUSH", "ZONE", "INDIVIDUAL"

    // Track state for Zone selection box
    private int zoneStartX = -1;
    private int zoneStartY = -1;

    public void initializeGrid(
            GridControl control,
            Supplier<CellType> activeBrushTypeSupplier,
            Supplier<String> activeBrushModeSupplier) {
        
        this.gridControl = control;
        this.activeBrushTypeSupplier = activeBrushTypeSupplier;
        this.activeBrushModeSupplier = activeBrushModeSupplier;
        
        rebuildDisplay();
    }

    /**
     * Constructs the visual representation of the grid board.
     */
    public void rebuildDisplay() {
        getChildren().clear();
        if (gridControl == null) return;

        int w = gridControl.getAbstraction().getWidth();
        int h = gridControl.getAbstraction().getHeight();

        // Calculate a responsive cell size based on grid size
        double cellSize = Math.clamp(600.0 / Math.max(w, h), 10.0, 40.0);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                CellControl cellCtrl = gridControl.getCellControl(x, y);
                CellAbstraction cellAbs = cellCtrl.getAbstraction();

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
                            gridControl.applyZone(zoneStartX, zoneStartY, finalX, finalY, activeBrushTypeSupplier.get());
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
        CellType brushType = activeBrushTypeSupplier.get();
        CellControl ctrl = gridControl.getCellControl(x, y);
        if (ctrl != null && brushType != null) {
            ctrl.setCellType(brushType);
        }
    }
}
