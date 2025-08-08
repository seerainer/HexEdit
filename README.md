# HexEdit

A simple, cross-platform Hex Editor built with Java and SWT.

## Features

- **Open, view, and edit binary files** in hexadecimal and ASCII
- **Dark mode** support (auto-detects system theme)
- **Find** bytes by hex string
- **Go to offset** (hexadecimal navigation)
- **Edit bytes** directly in the hex view
- **Cross-platform**: Windows, macOS, Linux (auto-selects SWT dependency)

## Getting Started

### Prerequisites
- Java 21 or newer
- Gradle (or use the included `gradlew` wrapper)

### Build & Run

```sh
# Clone the repository
$ git clone https://github.com/seerainer/HexEdit.git
$ cd HexEdit

# Build the project
$ ./gradlew build

# Run the application
$ ./gradlew run
```

## Usage
- **Open File**: `File > Open...` or `Ctrl+O`
- **Save File**: `File > Save` or `Ctrl+S`
- **Find**: `Edit > Find...` or `Ctrl+F` (enter hex string, e.g. `DE AD BE EF`)
- **Go to Offset**: `Edit > Go to Offset...` or `Ctrl+G` (enter hex offset, e.g. `1A3F`)
- **Edit Bytes**: Click in the hex area and type hex digits

## Project Structure

- `src/main/java/io/github/seerainer/hexedit/`
  - `Main.java` — Application entry point
  - `HexEditor.java` — Main UI and logic (modularized)
  - `FileManager.java` — File open/save logic
  - `DisplayManager.java` — Hex/ASCII display logic
  - `Controller.java` — Editing and caret logic
  - `DialogManager.java` — Find/goto dialogs
  - `ThemeManager.java` — Dark mode and theming
  - `HexUtils.java` — Utility methods
  - `HexPosition.java` — Byte/caret mapping

## Testing

- **Unit and integration tests** are in `src/test/java/io/github/seerainer/hexedit/`
- Run all tests:
  ```sh
  ./gradlew allTests
  ```

## Credits

- [Eclipse SWT](https://www.eclipse.org/swt/)

---

**HexEdit** — Fast, simple, hex editing.
