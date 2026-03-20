# HexEdit Repository Guide

This repository contains a Java-based Hex Editor application using SWT for the UI. It targets Java 25 and uses Gradle for building.

## 1. Build, Run, and Test Commands

Use the provided Gradle wrapper (`./gradlew` on Unix, `gradlew.bat` on Windows).

### Core Commands
- **Build Project:**
  ```bash
  ./gradlew build
  ```
- **Run Application:**
  ```bash
  ./gradlew run
  ```
- **Clean:**
  ```bash
  ./gradlew clean
  ```

### Testing
The project uses JUnit 5 and AssertJ.
- **Run All Tests:**
  ```bash
  ./gradlew test
  ```
  Or explicitly:
  ```bash
  ./gradlew unitTest integrationTest
  ```
- **Run a Single Test Class:**
  ```bash
  ./gradlew test --tests "io.github.seerainer.hexedit.HexEditorTest"
  ```
- **Run a Specific Test Method:**
  ```bash
  ./gradlew test --tests "io.github.seerainer.hexedit.HexEditorTest.testSaveFile"
  ```

### Native Image (GraalVM)
- **Build Native Image:**
  ```bash
  ./gradlew nativeCompile
  ```

## 2. Code Style & Conventions

Strictly adhere to the following conventions to maintain consistency with the existing codebase.

### General
- **Language Level:** Java 25. Use modern features.
- **Indentation:** The project uses **Tabs** for indentation (verified via `od -c` on `Main.java`). **Always mimic the indentation of the file you are editing.** Do not reformat entire files.
- **Final Variables:** heavily enforce the use of `final var` for local variables.
  - *Right:* `final var display = new Display();`
  - *Wrong:* `Display display = new Display();`
- **Unused Parameters:** Use `_` for unused lambda parameters.
  - *Example:* `widgetSelectedAdapter(_ -> saveFile())`
- **File Encoding:** UTF-8
- **Documentation:** Use Javadoc for classes and public methods.


### Naming
- **Classes:** PascalCase (e.g., `HexEditor`, `FileManager`).
- **Methods/Variables:** camelCase (e.g., `createContents`, `fileData`).
- **Constants:** UPPER_SNAKE_CASE (e.g., `BYTES_PER_LINE`).
- **Packages:** `io.github.seerainer.hexedit`.

### Imports
- **Explicit Imports:** Prefer explicit class imports over wildcards.
  - *Right:* `import org.eclipse.swt.widgets.Display;`
  - *Wrong:* `import org.eclipse.swt.widgets.*;`
- **Static Imports:** Used frequently for SWT listeners/adapters.
  - *Example:* `import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;`

### UI Development (SWT)
- **Threading:** SWT widgets must be accessed on the UI thread. Use `Display.getDefault().asyncExec(() -> ...)` if strictly necessary, but standard listeners run on the UI thread automatically.
- **Disposal:** Be mindful of SWT resource management (Colors, Fonts). The `HexEditor` class demonstrates disposing resources in a `dispose()` method.
- **Dark Mode:** The project supports dark mode. Use `ThemeManager` and `applyDarkModeToControl` methods when creating new UI elements.

### Error Handling
- Use `try-catch` blocks for I/O operations.
- Display errors to the user using `MessageBox` rather than just logging to stderr.
  ```java
  messageBox("Error", "Message content", SWT.ICON_ERROR | SWT.OK);
  ```

### Testing
- **Framework:** JUnit 5 (`@Test`, `@BeforeEach`, etc.).
- **Assertions:** AssertJ (`assertThat(...)`).
- **Structure:** `src/test/java` mirrors the package structure of `src/main/java`.
