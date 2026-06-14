# CyBreathe 🗿🗿🗿🗿

Ing-1 IT project <br>
Subject 2D Cellular Pollution Simulation [subject.pdf](PGL_ING1_Cellules_2D_2025_2026.pdf) <br>
Report [report.pdf](report.pdf) <br>
Class diagram [ClassDiagram.png](ClassDiagram.png) <br>
Use case diagram [UseCaseDiagram.png](UseCaseDiagram.png) <br>

## Compilation

Build the executable fat JAR:

```bash
./gradlew build
```

## Execution

You can run the built JAR using standard Java. By default, it launches in interactive GUI mode.

### GUI Mode (JavaFX)

```bash
java -jar build/libs/CyBreathe-1.0-SNAPSHOT.jar
```

### CLI Mode (Interactive Console)

To launch in the interactive text-based console mode, pass the `--cli` argument:

```bash
java -jar build/libs/CyBreathe-1.0-SNAPSHOT.jar --cli
```

#### CLI Commands

- `help` : Show the CLI guide.
- `init <width> <height>` : Create a clean grid of specified dimensions.
- `show` : Render the grid visually in ASCII format.
- `tick [count]` : Run simulation for count ticks (default 1).
- `set <x> <y> <type> [pollution] [rate]` : Place a cell (AIR, VEGETATION, FACTORY, BUILDING) at (x, y) with optional pollution and custom rate multiplier.
- `random <type> <percentage>` : Randomly seed a % of clean cells with specified type.
- `stats` : View grid configurations and cell statistics.
- `config <param> <value>` : Set parameters:
  - `diffusion <0.0 - 1.0>`
  - `absorption <0.0 - 1.0>`
  - `wind_direction <NONE/NORTH/EAST/SOUTH/WEST/...>`
  - `wind_strength <0.0 - 1.0>`
- `save <filename>` : Save current simulation state to a binary file.
- `load <filename>` : Load a simulation state from a binary file.
- `exit` / `quit` : Terminate the application.

---

### Development Mode

During development, you can use the Gradle application runner:

```bash
# Launch GUI mode
./gradlew run

# Launch CLI mode
./gradlew run --args="--cli"
```

## Dependencies

- Java 21
- JavaFX (Controls)
