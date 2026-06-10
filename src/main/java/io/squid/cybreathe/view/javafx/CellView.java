package io.squid.cybreathe.view.javafx;

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
public class CellView extends StackPane {

    private static boolean showDebugValues = false;
    private final Rectangle borderRect;
    private final Circle statusNode;
    private final Label debugLabel;

    private boolean highlighted;
    private String lastTypeName;
    private double lastPollutionLevel;
    private double lastCustomRate;

    /**
     * Constructs a CellView of the specified graphical size.
     *
     * @param cellSize the width and height of the cell visual square
     */
    public CellView(double cellSize) {
        setPrefSize(cellSize, cellSize);
        setMinSize(cellSize, cellSize);
        setMaxSize(cellSize, cellSize);

        this.borderRect = new Rectangle(cellSize - 1, cellSize - 1);
        this.borderRect.setStroke(Color.web("#e0e0e0"));
        this.borderRect.setStrokeWidth(0.5);

        this.statusNode = new Circle((cellSize - 2) / 4.0);
        this.statusNode.setVisible(false);

        this.debugLabel = new Label();
        this.debugLabel.setMouseTransparent(true);

        this.highlighted = false;
        this.lastTypeName = "AIR";
        this.lastPollutionLevel = 0.0;
        this.lastCustomRate = 0.0;

        getChildren().addAll(borderRect, statusNode, debugLabel);
    }

    /**
     * Enables or disables debug values display globally.
     *
     * @param show true to show debug values overlay, false to hide
     */
    public static void setShowDebugValues(boolean show) {
        showDebugValues = show;
    }

    /**
     * Renders the cell visually according to its type name, pollution level, and customRate strength.
     *
     * @param typeName       the cell type name (e.g. "AIR", "VEGETATION", "FACTORY", "BUILDING")
     * @param pollutionLevel the current pollution level of the cell
     * @param customRate     the custom rate multiplier (absorption/production strength)
     */
    public void draw(String typeName, double pollutionLevel, double customRate) {
        this.lastTypeName = typeName;
        this.lastPollutionLevel = pollutionLevel;
        this.lastCustomRate = customRate;

        Color cellColor;

        switch (typeName) {
            case "BUILDING" -> {
                cellColor = Color.web("#757575");
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#424242"));
                borderRect.setStrokeWidth(1.5);
                statusNode.setVisible(false);
            }
            case "FACTORY" -> {
                cellColor = Color.web("#37474f");
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#263238"));
                borderRect.setStrokeWidth(0.5);

                double baseRadius = (borderRect.getWidth() - 2) / 4.0;
                statusNode.setRadius(Math.clamp(baseRadius * customRate, baseRadius * 0.4, baseRadius * 1.8));
                statusNode.setFill(Color.web("#ff5722"));
                statusNode.setVisible(true);
            }
            case "VEGETATION" -> {
                cellColor = Color.web("#2e7d32");
                borderRect.setFill(cellColor);
                borderRect.setStroke(cellColor.darker());
                borderRect.setStrokeWidth(0.5);

                double baseRadius = (borderRect.getWidth() - 2) / 4.0;
                statusNode.setRadius(Math.clamp(baseRadius * customRate, baseRadius * 0.4, baseRadius * 1.8));
                statusNode.setFill(Color.web("#1b5e20"));
                statusNode.setVisible(true);
            }
            default -> { // AIR
                Color freshBlue = Color.web("#e0f7fa");
                Color smokyPurple = Color.web("#4a148c");

                cellColor = freshBlue.interpolate(smokyPurple, Math.clamp(pollutionLevel, 0.0, 1.0));
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#cfd8dc"));
                borderRect.setStrokeWidth(0.5);

                statusNode.setVisible(false);
            }
        }

        if (highlighted) {
            borderRect.setStroke(Color.GOLD);
            borderRect.setStrokeWidth(2.0);
        }

        if (showDebugValues) {
            debugLabel.setText(String.format("%.2f", pollutionLevel));
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

    /**
     * Sets the highlighted border style for this cell view (e.g. during zone-brush selections).
     *
     * @param highlighted true to highlight the cell, false to restore normal border
     */
    public void setHighlighted(boolean highlighted) {
        if (this.highlighted != highlighted) {
            this.highlighted = highlighted;
            draw(lastTypeName, lastPollutionLevel, lastCustomRate);
        }
    }
}
