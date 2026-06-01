package io.squid.cypgl.view.javafx;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * JavaFX component rendering a Cell's state visually.
 *
 * @author TopeEstLa
 */
public class CellPresentation extends StackPane {

    private final Rectangle borderRect;
    private final Circle statusNode; // Custom overlay representing tree health or factory core
    private final Label debugLabel;

    private static boolean showDebugValues = false;

    public static void setShowDebugValues(boolean show) {
        showDebugValues = show;
    }

    public static boolean isShowDebugValues() {
        return showDebugValues;
    }

    public CellPresentation(double cellSize) {
        setPrefSize(cellSize, cellSize);
        setMinSize(cellSize, cellSize);
        setMaxSize(cellSize, cellSize);

        // Core border/background rectangle
        this.borderRect = new Rectangle(cellSize - 1, cellSize - 1);
        this.borderRect.setStroke(Color.web("#e0e0e0"));
        this.borderRect.setStrokeWidth(0.5);

        // Visual status overlay circle
        this.statusNode = new Circle((cellSize - 2) / 4.0);
        this.statusNode.setVisible(false);

        // Visual debug label overlay
        this.debugLabel = new Label();
        this.debugLabel.setMouseTransparent(true);

        getChildren().addAll(borderRect, statusNode, debugLabel);
    }

    /**
     * Renders the cell visually according to its type name, pollution level, and customRate strength.
     */
    public void draw(String typeName, double pollutionLevel, double customRate) {
        Color cellColor;

        switch (typeName) {
            case "BUILDING" -> {
                // Solid concrete gray obstacle with clean dark border
                cellColor = Color.web("#757575");
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#424242"));
                borderRect.setStrokeWidth(1.5); // Thicker wall border
                statusNode.setVisible(false);
            }
            case "FACTORY" -> {
                // Sleek industrial metallic gray
                cellColor = Color.web("#37474f");
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#263238"));
                borderRect.setStrokeWidth(0.5);

                // Scale factory core orange glow based on individual generation capacity
                double baseRadius = (borderRect.getWidth() - 2) / 4.0;
                statusNode.setRadius(Math.clamp(baseRadius * customRate, baseRadius * 0.4, baseRadius * 1.8));
                statusNode.setFill(Color.web("#ff5722"));
                statusNode.setVisible(true);
            }
            case "TREE" -> {
                // Rich organic forest green
                cellColor = Color.web("#2e7d32");
                borderRect.setFill(cellColor);
                borderRect.setStroke(cellColor.darker());
                borderRect.setStrokeWidth(0.5);

                // Scale leaf circle size based on individual absorption capacity
                double baseRadius = (borderRect.getWidth() - 2) / 4.0;
                statusNode.setRadius(Math.clamp(baseRadius * customRate, baseRadius * 0.4, baseRadius * 1.8));
                statusNode.setFill(Color.web("#1b5e20"));
                statusNode.setVisible(true);
            }
            default -> { // AIR
                // Beautiful fluid gradient from fresh celestial blue to thick smoky industrial purple-black
                Color freshBlue = Color.web("#e0f7fa"); // Soft clean air
                Color smokyPurple = Color.web("#4a148c"); // Thick carbon soot/pollution

                cellColor = freshBlue.interpolate(smokyPurple, Math.clamp(pollutionLevel, 0.0, 1.0));
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#cfd8dc"));
                borderRect.setStrokeWidth(0.5);

                statusNode.setVisible(false);
            }
        }

        // Manage Debug Label display
        if (showDebugValues) {
            debugLabel.setText(String.format("%.2f", pollutionLevel));
            // High contrast text coloring
            if (typeName.equals("AIR") && pollutionLevel <= 0.4) {
                debugLabel.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: #37474f;");
            } else {
                debugLabel.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: white;");
            }
            debugLabel.setVisible(true);
        } else {
            debugLabel.setVisible(false);
        }
    }
}
