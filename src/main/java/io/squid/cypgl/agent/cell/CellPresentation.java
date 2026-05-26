package io.squid.cypgl.agent.cell;

import io.squid.cypgl.model.CellType;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Presentation layer in the PAC architecture for a Cell agent.
 * Implements a JavaFX StackPane that renders the cell color, health,
 * and pollution levels dynamically with modern rich aesthetics.
 * 
 * @author TopeEstLa
 */
public class CellPresentation extends StackPane {

    private final Rectangle borderRect;
    private final Circle statusNode; // Custom overlay representing tree health or factory core

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

        getChildren().addAll(borderRect, statusNode);
    }

    /**
     * Renders the cell visually according to its type, pollution level, and health.
     */
    public void draw(CellType type, double pollutionLevel, double health) {
        String typeName = type.getName();
        Color cellColor;
        
        switch (typeName) {
            case "FACTORY" -> {
                // Sleek industrial metallic gray
                cellColor = Color.web("#37474f");
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#263238"));
                
                // Pulsing factory core showing active emission
                statusNode.setFill(Color.web("#ff5722")); // Orange glow
                statusNode.setVisible(true);
            }
            case "TREE" -> {
                // Rich organic forest green, transitioning to yellow/olive depending on health
                Color healthyGreen = Color.web("#2e7d32");
                Color damagedYellow = Color.web("#c0ca33");
                
                cellColor = healthyGreen.interpolate(damagedYellow, 1.0 - health);
                borderRect.setFill(cellColor);
                borderRect.setStroke(cellColor.darker());
                
                // Add a small leaf-like circle inside
                statusNode.setFill(Color.web("#1b5e20"));
                statusNode.setVisible(true);
            }
            case "DEAD_TREE" -> {
                // Barren, withered brown tree stump
                cellColor = Color.web("#5d4037");
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#3e2723"));
                
                statusNode.setFill(Color.web("#8d6e63"));
                statusNode.setVisible(true);
            }
            default -> { // AIR
                // Beautiful fluid gradient from fresh celestial blue to thick smoky industrial purple-black
                Color freshBlue = Color.web("#e0f7fa"); // Soft clean air
                Color smokyPurple = Color.web("#4a148c"); // Thick carbon soot/pollution
                
                cellColor = freshBlue.interpolate(smokyPurple, pollutionLevel);
                borderRect.setFill(cellColor);
                borderRect.setStroke(Color.web("#cfd8dc"));
                
                statusNode.setVisible(false);
            }
        }
    }
}
