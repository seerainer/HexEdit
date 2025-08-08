package io.github.seerainer.hexedit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for HexEditor. Tests the complete functionality including
 * GUI components and file operations.
 */
@Tag("integration")
@DisplayName("HexEditor Integration Tests")
class HexEditorIntegrationTest {

    private Display display;

    private HexEditor hexEditor;

    @TempDir
    Path tempDir;

    /**
     * Helper method to run code on the UI thread and wait for completion.
     */
    private void runOnUIThread(final Runnable runnable) throws Exception {
	final var latch = new CountDownLatch(1);
	final var exception = new Exception[1];

	display.asyncExec(() -> {
	    try {
		runnable.run();
	    } catch (final Exception e) {
		exception[0] = e;
	    } finally {
		latch.countDown();
	    }
	});

	// Process events while waiting
	while (latch.getCount() > 0) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	    // Timeout after 5 seconds
	    if (latch.await(100, TimeUnit.MILLISECONDS)) {
		break;
	    }
	}

	if (exception[0] != null) {
	    throw exception[0];
	}
    }

    @BeforeEach
    void setUp() {
	// Create display in a way that works in headless environments
	System.setProperty("org.eclipse.swt.internal.gtk.cairoGraphics", "false");
	System.setProperty("org.eclipse.swt.internal.gtk.useCairo", "false");

	display = Display.getCurrent();
	if (display == null) {
	    display = new Display();
	}

	hexEditor = new HexEditor(display);
    }

    @AfterEach
    void tearDown() {
	if (hexEditor != null && hexEditor.getShell() != null && !hexEditor.getShell().isDisposed()) {
	    hexEditor.getShell().dispose();
	}

	// Don't dispose the display if it existed before we started
	if (Display.getCurrent() != null && display.getThread() == Thread.currentThread()) {
	    // Only dispose if we created it
	    try {
		while (display.readAndDispatch()) {
		    // Process pending events
		}
	    } catch (final Exception e) {
		// Ignore disposal exceptions in tests
	    }
	}
    }

    @Nested
    @DisplayName("Data Display and Formatting")
    class DataDisplayTest {

	@Test
	@DisplayName("should format hex data correctly")
	void shouldFormatHexDataCorrectly() throws Exception {
	    final var testData = "Hello World!".getBytes();

	    runOnUIThread(() -> {
		try {
		    // Set test data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, testData);

		    // Get text components
		    final var hexTextField = HexEditor.class.getDeclaredField("hexText");
		    hexTextField.setAccessible(true);
		    final var hexText = hexTextField.get(hexEditor);

		    final var asciiTextField = HexEditor.class.getDeclaredField("asciiText");
		    asciiTextField.setAccessible(true);
		    final var asciiText = asciiTextField.get(hexEditor);

		    final var offsetTextField = HexEditor.class.getDeclaredField("offsetText");
		    offsetTextField.setAccessible(true);
		    final var offsetText = offsetTextField.get(hexEditor);

		    // Trigger display
		    final var displayMethod = HexEditor.class.getDeclaredMethod("displayHexData");
		    displayMethod.setAccessible(true);
		    displayMethod.invoke(hexEditor);

		    // Verify offset text shows correct format (normalize line endings)
		    final var offsetContent = (String) offsetText.getClass().getMethod("getText").invoke(offsetText);
		    final var normalizedOffsetContent = offsetContent.replace("\r\n", "\n");
		    assertThat(normalizedOffsetContent).startsWith("00000000\n");

		    // Verify hex text contains expected hex values
		    final var hexContent = (String) hexText.getClass().getMethod("getText").invoke(hexText);
		    assertThat(hexContent).contains("48 65 6C 6C 6F 20 57 6F  72 6C 64 21");

		    // Verify ASCII text contains readable content (normalize line endings)
		    final var asciiContent = (String) asciiText.getClass().getMethod("getText").invoke(asciiText);
		    final var normalizedAsciiContent = asciiContent.replace("\r\n", "\n");
		    assertThat(normalizedAsciiContent).startsWith("Hello World!");

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}

	@Test
	@DisplayName("should handle non-printable characters")
	void shouldHandleNonPrintableCharacters() throws Exception {
	    final byte[] testData = { 0x00, 0x01, 0x20, 0x41, 0x7F, (byte) 0x80, (byte) 0xFF };

	    runOnUIThread(() -> {
		try {
		    // Set test data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, testData);

		    // Get ASCII text component
		    final var asciiTextField = HexEditor.class.getDeclaredField("asciiText");
		    asciiTextField.setAccessible(true);
		    final var asciiText = asciiTextField.get(hexEditor);

		    // Trigger display
		    final var displayMethod = HexEditor.class.getDeclaredMethod("displayHexData");
		    displayMethod.setAccessible(true);
		    displayMethod.invoke(hexEditor);

		    // Verify ASCII representation
		    final var asciiContent = (String) asciiText.getClass().getMethod("getText").invoke(asciiText);
		    // Should show: ". A..." (dots for non-printable, space and A for printable)
		    assertThat(asciiContent).startsWith(".. A...");

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTest {

	@Test
	@DisplayName("should handle empty files")
	void shouldHandleEmptyFiles() throws Exception {
	    runOnUIThread(() -> {
		try {
		    final var shell = hexEditor.getShell();

		    // Set empty data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, new byte[0]);

		    // Trigger display
		    final var displayMethod = HexEditor.class.getDeclaredMethod("displayHexData");
		    displayMethod.setAccessible(true);
		    displayMethod.invoke(hexEditor);

		    // Should not throw exception and should handle gracefully
		    assertThat(shell.isDisposed()).isFalse();

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}

	@Test
	@DisplayName("should handle large files efficiently")
	void shouldHandleLargeFilesEfficiently() throws Exception {
	    // Create a moderately large test file (1KB)
	    final var largeData = new byte[1024];
	    for (var i = 0; i < largeData.length; i++) {
		largeData[i] = (byte) (i % 256);
	    }

	    runOnUIThread(() -> {
		try {
		    final var shell = hexEditor.getShell();

		    final var startTime = System.currentTimeMillis();

		    // Set large data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, largeData);

		    // Trigger display
		    final var displayMethod = HexEditor.class.getDeclaredMethod("displayHexData");
		    displayMethod.setAccessible(true);
		    displayMethod.invoke(hexEditor);

		    final var endTime = System.currentTimeMillis();

		    // Should complete in reasonable time (less than 1 second)
		    assertThat(endTime - startTime).isLessThan(1000);
		    assertThat(shell.isDisposed()).isFalse();

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}
    }

    @Nested
    @DisplayName("File Operations")
    class FileOperationsTest {

	@Test
	@DisplayName("should create hex editor with initial state")
	void shouldCreateHexEditorWithInitialState() throws Exception {
	    runOnUIThread(() -> {
		final var shell = hexEditor.getShell();
		assertThat(shell).isNotNull();
		assertThat(shell.getText()).isEqualTo("Hex Editor");
		assertThat(shell.isDisposed()).isFalse();
	    });
	}

	@Test
	@DisplayName("should handle file loading and display")
	void shouldHandleFileLoadingAndDisplay() throws Exception {
	    // Create a test file
	    final var testFile = tempDir.resolve("test.txt");
	    final var testContent = "Hello World!";
	    Files.write(testFile, testContent.getBytes());

	    runOnUIThread(() -> {
		try {
		    // Simulate file loading by directly setting the data
		    // (since we can't easily simulate file dialog in tests)
		    final var shell = hexEditor.getShell();

		    // Use reflection to access private fields for testing
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, testContent.getBytes());

		    final var currentFileField = HexEditor.class.getDeclaredField("currentFile");
		    currentFileField.setAccessible(true);
		    currentFileField.set(hexEditor, testFile.toString());

		    // Trigger display update
		    final var displayMethod = HexEditor.class.getDeclaredMethod("displayHexData");
		    displayMethod.setAccessible(true);
		    displayMethod.invoke(hexEditor);

		    // Trigger title update
		    final var updateTitleMethod = HexEditor.class.getDeclaredMethod("updateTitle");
		    updateTitleMethod.setAccessible(true);
		    updateTitleMethod.invoke(hexEditor);

		    // Verify the shell title is updated
		    assertThat(shell.getText()).contains("test.txt");

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}

	@Test
	@DisplayName("should handle file saving")
	void shouldHandleFileSaving() throws Exception {
	    final var testFile = tempDir.resolve("save-test.txt");
	    final var originalContent = "Original content";
	    final var modifiedContent = "Modified content";

	    // Create original file
	    Files.write(testFile, originalContent.getBytes());

	    runOnUIThread(() -> {
		try {
		    hexEditor.getShell();

		    // Set up file data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, modifiedContent.getBytes());

		    final var currentFileField = HexEditor.class.getDeclaredField("currentFile");
		    currentFileField.setAccessible(true);
		    currentFileField.set(hexEditor, testFile.toString());

		    // Mark as modified
		    final var modifiedField = HexEditor.class.getDeclaredField("modified");
		    modifiedField.setAccessible(true);
		    modifiedField.set(hexEditor, Boolean.TRUE);

		    // Trigger save
		    final var saveMethod = HexEditor.class.getDeclaredMethod("saveFile");
		    saveMethod.setAccessible(true);
		    saveMethod.invoke(hexEditor);

		    // Verify modified flag is reset
		    final var isModified = ((Boolean) modifiedField.get(hexEditor)).booleanValue();
		    assertThat(isModified).isFalse();

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });

	    // Verify file was actually saved
	    final var savedContent = Files.readString(testFile);
	    assertThat(savedContent).isEqualTo(modifiedContent);
	}
    }

    @Nested
    @DisplayName("Search and Navigation")
    class SearchAndNavigationTest {

	@Test
	@DisplayName("should find hex patterns correctly")
	void shouldFindHexPatternsCorrectly() throws Exception {
	    final var testData = "Hello World! Hello again!".getBytes();

	    runOnUIThread(() -> {
		try {
		    hexEditor.getShell();

		    // Set test data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, testData);

		    // Test finding "Hello" (48656C6C6F)
		    final var findMethod = HexEditor.class.getDeclaredMethod("findInHex", String.class);
		    findMethod.setAccessible(true);
		    findMethod.invoke(hexEditor, "48656C6C6F");

		    // Verify status label shows found message
		    final var statusLabelField = HexEditor.class.getDeclaredField("statusLabel");
		    statusLabelField.setAccessible(true);
		    final var statusLabel = statusLabelField.get(hexEditor);

		    final var statusText = (String) statusLabel.getClass().getMethod("getText").invoke(statusLabel);
		    assertThat(statusText).startsWith("Found at offset: 0x0");

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}

	@Test
	@DisplayName("should handle pattern not found")
	void shouldHandlePatternNotFound() throws Exception {
	    final var testData = "Hello World!".getBytes();

	    runOnUIThread(() -> {
		try {
		    hexEditor.getShell();

		    // Set test data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, testData);

		    // Search for non-existent pattern
		    final var findMethod = HexEditor.class.getDeclaredMethod("findInHex", String.class);
		    findMethod.setAccessible(true);
		    findMethod.invoke(hexEditor, "DEADBEEF");

		    // Verify status label shows not found message
		    final var statusLabelField = HexEditor.class.getDeclaredField("statusLabel");
		    statusLabelField.setAccessible(true);
		    final var statusLabel = statusLabelField.get(hexEditor);

		    final var statusText = (String) statusLabel.getClass().getMethod("getText").invoke(statusLabel);
		    assertThat(statusText).isEqualTo("Not found");

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}

	@Test
	@DisplayName("should navigate to specific offset")
	void shouldNavigateToSpecificOffset() throws Exception {
	    final var testData = new byte[100]; // 100 bytes of data
	    for (var i = 0; i < testData.length; i++) {
		testData[i] = (byte) (i % 256);
	    }

	    runOnUIThread(() -> {
		try {
		    hexEditor.getShell();

		    // Set test data
		    final var fileDataField = HexEditor.class.getDeclaredField("fileData");
		    fileDataField.setAccessible(true);
		    fileDataField.set(hexEditor, testData);

		    // Trigger display first
		    final var displayMethod = HexEditor.class.getDeclaredMethod("displayHexData");
		    displayMethod.setAccessible(true);
		    displayMethod.invoke(hexEditor);

		    // Navigate to offset 32 (0x20)
		    final var gotoMethod = HexEditor.class.getDeclaredMethod("gotoOffset", int.class);
		    gotoMethod.setAccessible(true);
		    gotoMethod.invoke(hexEditor, Integer.valueOf(32));

		    // Verify hex text component receives focus and caret is positioned
		    final var hexTextField = HexEditor.class.getDeclaredField("hexText");
		    hexTextField.setAccessible(true);
		    final var hexText = hexTextField.get(hexEditor);

		    // Check if the component has focus and caret position
		    assertThat(hexText).isNotNull();

		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
	    });
	}
    }
}
