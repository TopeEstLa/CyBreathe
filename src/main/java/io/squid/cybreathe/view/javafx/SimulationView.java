package io.squid.cybreathe.view.javafx;

import io.squid.cybreathe.controller.javafx.SimulationController;
import io.squid.cybreathe.models.WindDirection;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

/**
 * JavaFX Dashboard presentation layer.
 *
 * @author TopeEstLa
 */
public class SimulationView extends BorderPane {

    private final SimulationController control;
    private final GridView gridView;
    private final Map<WindDirection, Button> windDirButtons = new HashMap<>();
    // Timeline loop state
    private Timer timerLoop;
    // UI Controls
    private ToggleGroup brushModeGroup;
    private ToggleGroup cellTypeGroup;
    private Label tickLabel;
    // Sliders
    private Slider speedSlider;
    private Slider diffusionSlider;
    private Slider absorptionSlider;
    // Brush custom rate controls
    private CheckBox randomRateCheckbox;
    private Slider brushCustomRateSlider;
    private Label brushCustomRateLabel;
    // Wind controls
    private Slider windStrengthSlider;
    private Label windStrengthLabel;
    // Stats Labels
    private Label statsSummaryLabel;

    // Charts
    private LineChart<Number, Number> pollutionChart;
    private XYChart.Series<Number, Number> pollutionSeries;
    private LineChart<Number, Number> pollutedAirChart;
    private XYChart.Series<Number, Number> pollutedAirSeries;

    /**
     * Constructs a SimulationView with the associated simulation controller.
     * Initializes all UI components, setups event listeners, and synchronizes properties.
     *
     * @param control the simulation controller
     */
    public SimulationView(SimulationController control) {
        this.control = control;
        this.gridView = new GridView();

        setupTopToolbar();
        setupLeftSidebar();
        setupCenterGrid();
        setupRightStatsPanel();

        this.control.setPresentation(this);

        rebuildGridDisplay();

        bindProperties();
    }

    private void setupTopToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #eceff1; -fx-border-color: #cfd8dc; -fx-border-width: 0 0 1 0;");

        Button playBtn = new Button("▶ Play");
        Button pauseBtn = new Button("❚❚ Pause");
        Button stepBtn = new Button("▶❚ Step");
        Button clearBtn = new Button("↺ Clear");
        Button saveBtn = new Button("💾 Save");
        Button loadBtn = new Button("📂 Load");

        tickLabel = new Label("Tick: 0");
        tickLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #37474f;");

        playBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        pauseBtn.setStyle("-fx-background-color: #ff8f00; -fx-text-fill: white; -fx-font-weight: bold;");
        stepBtn.setStyle("-fx-background-color: #0277bd; -fx-text-fill: white; -fx-font-weight: bold;");

        playBtn.setOnAction(e -> startSimulationLoop());
        pauseBtn.setOnAction(e -> stopSimulationLoop());
        stepBtn.setOnAction(e -> control.tick());

        clearBtn.setOnAction(e -> {
            stopSimulationLoop();
            control.clearSimulation();
            gridView.rebuildDisplay();
        });

        saveBtn.setOnAction(e -> {
            stopSimulationLoop();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Simulation State");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CyPGL Binary (*.cyp)", "*.cyp"));
            File file = fileChooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                try {
                    control.saveSimulation(file);
                    showInfoAlert("Export Success", "Simulation exported successfully to:\n" + file.getName());
                } catch (Exception ex) {
                    showErrorAlert("Export Failed", ex.getMessage());
                }
            }
        });

        loadBtn.setOnAction(e -> {
            stopSimulationLoop();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Simulation State");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CyPGL Binary (*.cyp)", "*.cyp"));
            File file = fileChooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                try {
                    control.loadSimulation(file);
                    showInfoAlert("Import Success", "Simulation loaded successfully from:\n" + file.getName());
                } catch (Exception ex) {
                    showErrorAlert("Import Failed", ex.getMessage());
                }
            }
        });

        CheckBox debugCheckbox = new CheckBox("🐞 Debug Values");
        debugCheckbox.setStyle("-fx-font-weight: bold; -fx-text-fill: #37474f;");
        debugCheckbox.setOnAction(e -> {
            CellView.setShowDebugValues(debugCheckbox.isSelected());
            control.getGridControl().updateAllCellPresentations();
        });

        toolbar.getChildren().addAll(playBtn, pauseBtn, stepBtn, clearBtn, new Separator(), saveBtn, loadBtn, new Separator(), debugCheckbox, new Pane(), tickLabel);
        HBox.setHgrow(toolbar.getChildren().get(9), Priority.ALWAYS); // Spacer

        setTop(toolbar);
    }

    private void setupLeftSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #f5f7f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        // 0. Grid Size Configuration
        VBox sizeBox = new VBox(8);
        sizeBox.setStyle("-fx-border-color: #cfd8dc; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: white;");
        Label sizeHeading = new Label("Grid Size Configuration");
        sizeHeading.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a237e;");

        HBox inputsBox = new HBox(8);
        inputsBox.setAlignment(Pos.CENTER_LEFT);

        Label wLabel = new Label("W:");
        wLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #37474f; -fx-font-weight: bold;");
        Spinner<Integer> widthSpinner = new Spinner<>(5, 100, control.getGridWidth(), 1);
        widthSpinner.setPrefWidth(70);
        widthSpinner.setEditable(true);

        Label hLabel = new Label("H:");
        hLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #37474f; -fx-font-weight: bold;");
        Spinner<Integer> heightSpinner = new Spinner<>(5, 100, control.getGridHeight(), 1);
        heightSpinner.setPrefWidth(70);
        heightSpinner.setEditable(true);

        inputsBox.getChildren().addAll(wLabel, widthSpinner, hLabel, heightSpinner);

        Button resizeBtn = new Button("Apply & Reset");
        resizeBtn.setMaxWidth(Double.MAX_VALUE);
        resizeBtn.setStyle("-fx-background-color: #0277bd; -fx-text-fill: white; -fx-font-weight: bold;");
        resizeBtn.setOnAction(e -> {
            stopSimulationLoop();
            int newW = widthSpinner.getValue();
            int newH = heightSpinner.getValue();
            control.resizeAndReset(newW, newH);
        });

        sizeBox.getChildren().addAll(sizeHeading, inputsBox, resizeBtn);

        // 1. Brush Tool Configuration
        VBox brushBox = new VBox(8);
        brushBox.setStyle("-fx-border-color: #cfd8dc; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: white;");
        Label brushHeading = new Label("Brush Configuration");
        brushHeading.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a237e;");

        // Modes
        brushModeGroup = new ToggleGroup();
        RadioButton individualRadio = new RadioButton("Individual Click");
        RadioButton brushRadio = new RadioButton("Brush (Drag Paint)");
        RadioButton zoneRadio = new RadioButton("Zone Selection (Drag Box)");
        individualRadio.setToggleGroup(brushModeGroup);
        brushRadio.setToggleGroup(brushModeGroup);
        zoneRadio.setToggleGroup(brushModeGroup);
        brushModeGroup.selectToggle(individualRadio);

        // Types
        Label typeLabel = new Label("Cell Type:");
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #78909c;");
        cellTypeGroup = new ToggleGroup();
        RadioButton airRadio = new RadioButton("AIR");
        RadioButton vegetationRadio = new RadioButton("VEGETATION");
        RadioButton factoryRadio = new RadioButton("FACTORY");
        RadioButton buildingRadio = new RadioButton("BUILDING");
        airRadio.setToggleGroup(cellTypeGroup);
        vegetationRadio.setToggleGroup(cellTypeGroup);
        factoryRadio.setToggleGroup(cellTypeGroup);
        buildingRadio.setToggleGroup(cellTypeGroup);
        cellTypeGroup.selectToggle(vegetationRadio);

        randomRateCheckbox = new CheckBox("Randomize Power (0.5x - 2.0x)");
        randomRateCheckbox.setStyle("-fx-font-size: 11px; -fx-text-fill: #37474f;");
        randomRateCheckbox.setSelected(false);

        brushCustomRateLabel = new Label("Brush Power: 1.0x");
        brushCustomRateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #78909c; -fx-font-weight: bold;");

        brushCustomRateSlider = new Slider(0.1, 3.0, 1.0);
        brushCustomRateSlider.setShowTickLabels(true);
        brushCustomRateSlider.setShowTickMarks(true);
        brushCustomRateSlider.setMajorTickUnit(1.0);
        brushCustomRateSlider.setMinorTickCount(9);
        brushCustomRateSlider.setBlockIncrement(0.1);

        brushCustomRateSlider.valueProperty().addListener((obs, ov, nv) -> {
            brushCustomRateLabel.setText(String.format("Brush Power: %.1fx", nv.doubleValue()));
        });

        // Disable slider/label when random is checked
        brushCustomRateSlider.disableProperty().bind(randomRateCheckbox.selectedProperty());
        brushCustomRateLabel.disableProperty().bind(randomRateCheckbox.selectedProperty());

        brushBox.getChildren().addAll(
                brushHeading, individualRadio, brushRadio, zoneRadio, new Separator(),
                typeLabel, airRadio, vegetationRadio, factoryRadio, buildingRadio, new Separator(),
                randomRateCheckbox, brushCustomRateLabel, brushCustomRateSlider
        );

        VBox windBox = new VBox(8);
        windBox.setStyle("-fx-border-color: #cfd8dc; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: white;");
        Label windHeading = new Label("Wind Settings");
        windHeading.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a237e;");

        GridPane compassGrid = new GridPane();
        compassGrid.setHgap(5);
        compassGrid.setVgap(5);
        compassGrid.setAlignment(Pos.CENTER);

        setupCompassButton(compassGrid, WindDirection.NORTH_WEST, "NW", 0, 0);
        setupCompassButton(compassGrid, WindDirection.NORTH, "N ⬆", 0, 1);
        setupCompassButton(compassGrid, WindDirection.NORTH_EAST, "NE", 0, 2);
        setupCompassButton(compassGrid, WindDirection.WEST, "W ⬅", 1, 0);
        setupCompassButton(compassGrid, WindDirection.NONE, "╳ OFF", 1, 1);
        setupCompassButton(compassGrid, WindDirection.EAST, "E ➡", 1, 2);
        setupCompassButton(compassGrid, WindDirection.SOUTH_WEST, "SW", 2, 0);
        setupCompassButton(compassGrid, WindDirection.SOUTH, "S ⬇", 2, 1);
        setupCompassButton(compassGrid, WindDirection.SOUTH_EAST, "SE", 2, 2);

        windStrengthLabel = new Label("Wind Strength: 50%");
        windStrengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #37474f; -fx-font-weight: bold;");

        windStrengthSlider = new Slider(0.0, 1.0, 0.5);
        windStrengthSlider.setShowTickLabels(true);
        windStrengthSlider.setShowTickMarks(true);
        windStrengthSlider.setMajorTickUnit(0.5);
        windStrengthSlider.setMinorTickCount(4);
        windStrengthSlider.setBlockIncrement(0.1);

        windStrengthSlider.valueProperty().addListener((obs, ov, nv) -> {
            windStrengthLabel.setText(String.format("Wind Strength: %.0f%%", nv.doubleValue() * 100));
            control.setWindStrength(nv.doubleValue());
        });

        windBox.getChildren().addAll(
                windHeading,
                new Label("Direction:"), compassGrid,
                windStrengthLabel, windStrengthSlider
        );

        VBox ratesBox = new VBox(8);
        ratesBox.setStyle("-fx-border-color: #cfd8dc; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: white;");
        Label ratesHeading = new Label("Simulation Rates");
        ratesHeading.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a237e;");

        diffusionSlider = new Slider(0.05, 0.8, 0.3);
        absorptionSlider = new Slider(0.02, 0.5, 0.15);
        speedSlider = new Slider(50, 1000, 200);

        ratesBox.getChildren().addAll(
                ratesHeading,
                new Label("Diffusion Rate:"), diffusionSlider,
                new Label("Absorption Power:"), absorptionSlider,
                new Label("Tick Delay (ms):"), speedSlider
        );

        sidebar.getChildren().addAll(brushBox, windBox, ratesBox, sizeBox);

        ScrollPane scroller = new ScrollPane(sidebar);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setLeft(scroller);
    }

    private void setupCenterGrid() {
        gridView.setAlignment(Pos.CENTER);
        StackPane gridContainer = new StackPane(gridView);
        gridContainer.setPadding(new Insets(15));
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.setStyle("-fx-background-color: #ffffff;");

        ScrollPane gridScroller = new ScrollPane(gridContainer);
        gridScroller.setFitToWidth(true);
        gridScroller.setFitToHeight(true);

        setCenter(gridScroller);
    }

    private void setupRightStatsPanel() {
        VBox rightBar = new VBox(15);
        rightBar.setPadding(new Insets(15));
        rightBar.setPrefWidth(320);
        rightBar.setStyle("-fx-background-color: #f5f7f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 0 0 1;");

        Label statsTitle = new Label("Simulation Analytics");
        statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #37474f;");

        statsSummaryLabel = new Label("Initializing statistics...");
        statsSummaryLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #cfd8dc; -fx-border-radius: 4; -fx-padding: 8;");
        statsSummaryLabel.setPrefWidth(Double.MAX_VALUE);

        NumberAxis x2 = new NumberAxis();
        NumberAxis y2 = new NumberAxis();
        x2.setLabel("Historical Ticks");
        y2.setLabel("Level (0 to 1)");
        pollutionChart = new LineChart<>(x2, y2);
        pollutionChart.setTitle("Avg Pollution");
        pollutionChart.setCreateSymbols(false);
        pollutionChart.setPrefHeight(200);
        pollutionChart.setLegendVisible(false);

        pollutionSeries = new XYChart.Series<>();
        pollutionChart.getData().add(pollutionSeries);

        NumberAxis x3 = new NumberAxis();
        NumberAxis y3 = new NumberAxis();
        x3.setLabel("Historical Ticks");
        y3.setLabel("Count");
        pollutedAirChart = new LineChart<>(x3, y3);
        pollutedAirChart.setTitle("Polluted Air Cells Count");
        pollutedAirChart.setCreateSymbols(false);
        pollutedAirChart.setPrefHeight(200);
        pollutedAirChart.setLegendVisible(false);

        pollutedAirSeries = new XYChart.Series<>();
        pollutedAirChart.getData().add(pollutedAirSeries);

        rightBar.getChildren().addAll(statsTitle, statsSummaryLabel, pollutionChart, pollutedAirChart);

        ScrollPane scroller = new ScrollPane(rightBar);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setRight(scroller);
    }

    private void bindProperties() {
        syncUIWithModel();

        diffusionSlider.valueProperty().addListener((obs, ov, nv) -> control.setDiffusionRate(nv.doubleValue()));
        absorptionSlider.valueProperty().addListener((obs, ov, nv) -> control.setAbsorptionRate(nv.doubleValue()));

        speedSlider.valueProperty().addListener((obs, ov, nv) -> {
            control.setSpeedDelayMs(nv.intValue());
            if (timerLoop != null) {
                startSimulationLoop();
            }
        });
    }

    /**
     * Synchronizes all sliders and wind controls with the current simulation model parameters.
     */
    public void syncUIWithModel() {
        diffusionSlider.setValue(control.getDiffusionRate());
        absorptionSlider.setValue(control.getAbsorptionRate());
        speedSlider.setValue(control.getSpeedDelayMs());

        windStrengthSlider.setValue(control.getWindStrength());
        windStrengthLabel.setText(String.format("Wind Strength: %.0f%%", control.getWindStrength() * 100));
        updateWindUISelection();
    }

    /**
     * Rebuilds the grid visual display.
     */
    public void rebuildGridDisplay() {
        gridView.initializeGrid(
                this.control.getGridControl(),
                this::getSelectedCellType,
                this::getSelectedBrushMode,
                this::getBrushCustomRate
        );
    }

    private Double getBrushCustomRate() {
        if (randomRateCheckbox != null && randomRateCheckbox.isSelected()) {
            return 0.5 + Math.random() * 1.5;
        }
        return brushCustomRateSlider != null ? brushCustomRateSlider.getValue() : 1.0;
    }

    private String getSelectedCellType() {
        RadioButton selected = (RadioButton) cellTypeGroup.getSelectedToggle();
        if (selected == null) return "AIR";
        return selected.getText(); // returns "AIR", "VEGETATION", "FACTORY", or "BUILDING"
    }

    private String getSelectedBrushMode() {
        RadioButton selected = (RadioButton) brushModeGroup.getSelectedToggle();
        if (selected == null) return "INDIVIDUAL";
        return switch (selected.getText()) {
            case "Brush (Drag Paint)" -> "BRUSH";
            case "Zone Selection (Drag Box)" -> "ZONE";
            default -> "INDIVIDUAL";
        };
    }

    /**
     * Starts the periodic background simulation execution loop safely in the JavaFX thread.
     */
    private synchronized void startSimulationLoop() {
        stopSimulationLoop();
        timerLoop = new Timer(true);
        timerLoop.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(control::tick);
            }
        }, 0, control.getSpeedDelayMs());
    }

    /**
     * Stops the running simulation background thread loop.
     */
    private synchronized void stopSimulationLoop() {
        if (timerLoop != null) {
            timerLoop.cancel();
            timerLoop = null;
        }
    }

    /**
     * Updates statistical text and graphs on the UI panel.
     *
     * @param tickCount           the current tick count of the simulation
     * @param avgPollutionHistory the historical list of average pollution levels
     * @param pollutedAirHistory  the historical list of polluted air cell counts
     */
    public void updateDashboard(
            int tickCount,
            List<Double> avgPollutionHistory,
            List<Integer> pollutedAirHistory) {

        // 1. Update Tick Count Label
        tickLabel.setText("Tick: " + tickCount);

        int w = control.getGridWidth();
        int h = control.getGridHeight();
        int total = w * h;

        double pollution = avgPollutionHistory.isEmpty() ? 0.0 : avgPollutionHistory.getLast();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("GRID SIZE : %d x %d %n", w, h));
        sb.append(String.format("TOTALS    : %d cells%n", total));
        sb.append(String.format("POLLUTION : %.4f (avg)%n%n", pollution));
        sb.append(String.format("CELL DISTRIBUTION:%n"));

        Map<String, Integer> counts = control.getCellTypeCounts();
        Map<String, Double> percentages = control.getCellTypePercentages();
        for (String type : counts.keySet()) {
            int count = counts.get(type);
            double pct = percentages.get(type) * 100.0;
            sb.append(String.format("- %-10s: %d (%.1f%%)%n", type, count, pct));
        }

        statsSummaryLabel.setText(sb.toString());

        updateSeriesDouble(pollutionSeries, avgPollutionHistory);
        updateSeries(pollutedAirSeries, pollutedAirHistory);
    }

    private void updateSeries(XYChart.Series<Number, Number> series, List<Integer> history) {
        series.getData().clear();
        for (int i = 0; i < history.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, history.get(i)));
        }
    }

    private void updateSeriesDouble(XYChart.Series<Number, Number> series, List<Double> history) {
        series.getData().clear();
        for (int i = 0; i < history.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, history.get(i)));
        }
    }

    /**
     * Displays an information alert dialog.
     *
     * @param title   the title of the alert dialog
     * @param content the context message to display
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays an error alert dialog.
     *
     * @param title   the title of the alert dialog
     * @param content the error message context to display
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Sets up a single compass direction button in the grid layout.
     *
     * @param grid  the grid layout pane to place the button in
     * @param dir   the wind direction associated with the button
     * @param label the text label of the button
     * @param row   the grid row index
     * @param col   the grid column index
     */
    private void setupCompassButton(GridPane grid, WindDirection dir, String label, int row, int col) {
        Button btn = new Button(label);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setPrefSize(60, 30);
        btn.setStyle("-fx-background-color: #f0f4c3; -fx-text-fill: #37474f; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-border-color: #d4e157; -fx-border-radius: 4;");

        btn.setOnMouseEntered(e -> {
            if (control.getWindDirection() != dir) {
                btn.setStyle("-fx-background-color: #d4e157; -fx-text-fill: #1a237e; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-border-color: #afb42b; -fx-border-radius: 4;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (control.getWindDirection() != dir) {
                btn.setStyle("-fx-background-color: #f0f4c3; -fx-text-fill: #37474f; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-border-color: #d4e157; -fx-border-radius: 4;");
            }
        });

        btn.setOnAction(e -> {
            control.setWindDirection(dir);
            updateWindUISelection();
        });

        grid.add(btn, col, row);
        windDirButtons.put(dir, btn);
    }

    private void updateWindUISelection() {
        WindDirection currentDir = control.getWindDirection();
        for (Map.Entry<WindDirection, Button> entry : windDirButtons.entrySet()) {
            WindDirection dir = entry.getKey();
            Button btn = entry.getValue();
            if (dir == currentDir) {
                btn.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-border-color: #0d47a1; -fx-border-radius: 4;");
            } else {
                btn.setStyle("-fx-background-color: #f0f4c3; -fx-text-fill: #37474f; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-border-color: #d4e157; -fx-border-radius: 4;");
            }
        }
    }

    /**
     * Cleans up simulation resources, stopping the execution loop.
     */
    public void cleanup() {
        stopSimulationLoop();
    }
}
