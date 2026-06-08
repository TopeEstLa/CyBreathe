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

    private static boolean showDebugValues = false;
    private final Rectangle borderRect;
    private final Circle statusNode;
    private final Label debugLabel;

    public CellPresentation(double cellSize) {
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

        getChildren().addAll(borderRect, statusNode, debugLabel);
    }

    public static boolean isShowDebugValues() {
        return showDebugValues;
    }

    public static void setShowDebugValues(boolean show) {
        showDebugValues = show;
    }

    /**
     * Renders the cell visually according to its type name, pollution level, and customRate strength.
     */
    public void draw(String typeName, double pollutionLevel, double customRate) {
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
            case "TREE" -> {
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
}
