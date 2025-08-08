package io.github.seerainer.hexedit;

import static org.eclipse.swt.events.KeyListener.keyPressedAdapter;
import static org.eclipse.swt.events.MouseListener.mouseUpAdapter;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * The main class for the Hex Editor application. Handles UI creation, event
 * wiring, file operations, and editing logic. Integrates all managers and
 * controllers for a complete hex editing experience.
 */
class HexEditor {
    private static final int BYTES_PER_LINE = 16;
    private final boolean isDarkMode;
    private boolean modified;
    private byte[] fileData;
    private Shell shell;
    private String currentFile;
    private StyledText hexText;
    private StyledText asciiText;
    private StyledText offsetText;
    private Label statusLabel;
    private Font monospaceFont;
    private final FileManager fileManager;
    private final ThemeManager themeManager;
    private Controller editController;
    private DialogManager dialogManager;
    private DisplayManager displayManager;
    private MenuItem saveItem;
    private MenuItem saveAsItem;

    /**
     * Constructs the HexEditor, initializes managers, and sets up the UI.
     *
     * @param display the SWT Display instance
     */
    HexEditor(final Display display) {
	fileManager = new FileManager();
	themeManager = new ThemeManager();
	themeManager.detectAndSetupTheme(display);
	isDarkMode = themeManager.isDarkMode();
	createContents(display);
    }

    private static MenuItem separator(final Menu menu) {
	return new MenuItem(menu, SWT.SEPARATOR);
    }

    /**
     * Applies dark mode colors to the given control if dark mode is enabled.
     *
     * @param control the SWT Control to style
     */
    void applyDarkModeToControl(final Control control) {
	if (!isDarkMode) {
	    return;
	}
	control.setBackground(themeManager.getDarkBackground());
	control.setForeground(themeManager.getDarkForeground());
    }

    private void createContents(final Display display) {
	shell = new Shell(display, SWT.SHELL_TRIM);
	shell.addDisposeListener(_ -> dispose());
	shell.setText("Hex Editor");
	shell.setSize(900, 650);
	shell.setLayout(new GridLayout(1, false));
	applyDarkModeToControl(shell);

	final var fontData = display.getSystemFont().getFontData();
	for (final var fd : fontData) {
	    fd.setName("Consolas");
	    fd.setHeight(12);
	}
	monospaceFont = new Font(display, fontData);

	createMenuBar();
	createMainArea();
	createStatusBar();

	shell.open();
    }

    private void createMainArea() {
	final var mainComposite = new Composite(shell, SWT.NONE);
	final var gridLayout = new GridLayout(3, false);
	gridLayout.marginWidth = 12;
	gridLayout.marginHeight = 8;
	gridLayout.horizontalSpacing = 10;
	gridLayout.verticalSpacing = 6;
	mainComposite.setLayout(gridLayout);
	mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	applyDarkModeToControl(mainComposite);

	final var offsetLabel = new Label(mainComposite, SWT.NONE);
	offsetLabel.setText("Offset");
	offsetLabel.setFont(monospaceFont);
	offsetLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
	applyDarkModeToControl(offsetLabel);

	final var hexLabel = new Label(mainComposite, SWT.NONE);
	hexLabel.setText("Hex");
	hexLabel.setFont(monospaceFont);
	hexLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	applyDarkModeToControl(hexLabel);

	final var asciiLabel = new Label(mainComposite, SWT.NONE);
	asciiLabel.setText("ASCII");
	asciiLabel.setFont(monospaceFont);
	asciiLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
	applyDarkModeToControl(asciiLabel);

	offsetText = new StyledText(mainComposite, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
	final var offsetGd = new GridData(SWT.FILL, SWT.FILL, false, true);
	offsetGd.widthHint = 80;
	offsetText.setLayoutData(offsetGd);
	offsetText.setFont(monospaceFont);
	if (isDarkMode) {
	    offsetText.setBackground(themeManager.getDarkOffsetBackground());
	    offsetText.setForeground(themeManager.getDarkForeground());
	} else {
	    offsetText.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	}
	offsetText.setEnabled(false);

	hexText = new StyledText(mainComposite, SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
	final var hexGd = new GridData(SWT.FILL, SWT.FILL, true, true);
	hexGd.widthHint = 500;
	hexText.setLayoutData(hexGd);
	hexText.setFont(monospaceFont);
	if (isDarkMode) {
	    hexText.setBackground(themeManager.getDarkHexBackground());
	    hexText.setForeground(themeManager.getDarkForeground());
	} else {
	    hexText.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	}
	hexText.setEnabled(false);

	asciiText = new StyledText(mainComposite, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
	final var asciiGd = new GridData(SWT.FILL, SWT.FILL, false, true);
	asciiGd.widthHint = 160;
	asciiText.setLayoutData(asciiGd);
	asciiText.setFont(monospaceFont);
	if (isDarkMode) {
	    asciiText.setBackground(themeManager.getDarkAsciiBackground());
	    asciiText.setForeground(themeManager.getDarkForeground());
	} else {
	    asciiText.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
	}
	asciiText.setEnabled(false);

	hexText.addKeyListener(keyPressedAdapter(this::handleHexKeyPress));
	hexText.addMouseListener(mouseUpAdapter(_ -> positionCursorOnHexDigit()));
	hexText.getVerticalBar().addSelectionListener(widgetSelectedAdapter(_ -> {
	    final var selection = hexText.getVerticalBar().getSelection();
	    offsetText.getVerticalBar().setSelection(selection);
	    asciiText.getVerticalBar().setSelection(selection);

	    final var topIndex = hexText.getTopIndex();
	    offsetText.setTopIndex(topIndex);
	    asciiText.setTopIndex(topIndex);
	}));
	asciiText.getVerticalBar().addSelectionListener(widgetSelectedAdapter(_ -> {
	    final var selection = asciiText.getVerticalBar().getSelection();
	    offsetText.getVerticalBar().setSelection(selection);
	    hexText.getVerticalBar().setSelection(selection);

	    final var topIndex = asciiText.getTopIndex();
	    offsetText.setTopIndex(topIndex);
	    hexText.setTopIndex(topIndex);
	}));

	offsetText.getVerticalBar().addSelectionListener(widgetSelectedAdapter(_ -> {
	    final var selection = offsetText.getVerticalBar().getSelection();
	    hexText.getVerticalBar().setSelection(selection);
	    asciiText.getVerticalBar().setSelection(selection);

	    final var topIndex = offsetText.getTopIndex();
	    hexText.setTopIndex(topIndex);
	    asciiText.setTopIndex(topIndex);
	}));

	displayManager = new DisplayManager(offsetText, hexText, asciiText, BYTES_PER_LINE);
	editController = new Controller(hexText, this);
	dialogManager = new DialogManager(this);
    }

    private void createMenuBar() {
	final var menuBar = new Menu(shell, SWT.BAR);
	shell.setMenuBar(menuBar);

	final var fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
	fileMenuItem.setText("&File");
	final var fileMenu = new Menu(shell, SWT.DROP_DOWN);
	fileMenuItem.setMenu(fileMenu);

	final var openItem = new MenuItem(fileMenu, SWT.PUSH);
	openItem.setText("&Open...\tCtrl+O");
	openItem.setAccelerator(SWT.CTRL + 'O');
	openItem.addSelectionListener(widgetSelectedAdapter(_ -> openFile()));

	saveItem = new MenuItem(fileMenu, SWT.PUSH);
	saveItem.setText("&Save\tCtrl+S");
	saveItem.setAccelerator(SWT.CTRL + 'S');
	saveItem.addSelectionListener(widgetSelectedAdapter(_ -> saveFile()));
	saveItem.setEnabled(false);

	saveAsItem = new MenuItem(fileMenu, SWT.PUSH);
	saveAsItem.setText("Save &As...");
	saveAsItem.addSelectionListener(widgetSelectedAdapter(_ -> saveAsFile()));
	saveAsItem.setEnabled(false);

	separator(fileMenu);

	final var exitItem = new MenuItem(fileMenu, SWT.PUSH);
	exitItem.setText("E&xit");
	exitItem.addSelectionListener(widgetSelectedAdapter(_ -> shell.close()));

	final var editMenuItem = new MenuItem(menuBar, SWT.CASCADE);
	editMenuItem.setText("&Edit");
	final var editMenu = new Menu(shell, SWT.DROP_DOWN);
	editMenuItem.setMenu(editMenu);

	final var findItem = new MenuItem(editMenu, SWT.PUSH);
	findItem.setText("&Find...\tCtrl+F");
	findItem.setAccelerator(SWT.CTRL + 'F');
	findItem.addSelectionListener(widgetSelectedAdapter(_ -> openFindDialog()));

	final var gotoItem = new MenuItem(editMenu, SWT.PUSH);
	gotoItem.setText("&Go to Offset...\tCtrl+G");
	gotoItem.setAccelerator(SWT.CTRL + 'G');
	gotoItem.addSelectionListener(widgetSelectedAdapter(_ -> openGotoDialog()));
    }

    private void createStatusBar() {
	statusLabel = new Label(shell, SWT.HORIZONTAL);
	final var gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
	gd.verticalIndent = 4;
	statusLabel.setLayoutData(gd);
	statusLabel.setFont(monospaceFont);
	statusLabel.setText("Ready");
	applyDarkModeToControl(statusLabel);
    }

    private void displayHexData() {
	displayManager.displayHexData(fileData);
    }

    private void dispose() {
	if (monospaceFont != null && !monospaceFont.isDisposed()) {
	    monospaceFont.dispose();
	}
	if (themeManager != null) {
	    themeManager.dispose();
	}
    }

    void findInHex(final String hexString) {
	if (fileData == null) {
	    return;
	}
	try {
	    final var searchBytes = HexUtils.hexStringToBytes(hexString);
	    final var foundIndex = HexUtils.indexOf(fileData, searchBytes);
	    if (foundIndex >= 0) {
		gotoOffset(foundIndex);
		statusLabel.setText("Found at offset: 0x" + Integer.toHexString(foundIndex).toUpperCase());
	    } else {
		statusLabel.setText("Not found");
	    }
	} catch (final IllegalArgumentException e) {
	    messageBox("Error", "Invalid hex string", SWT.ICON_ERROR | SWT.OK);
	}
    }

    /**
     * Finds the hex position (byte index, nibble, and text offset) for a given text
     * offset.
     *
     * @param textOffset the offset in the hex text widget
     * @return a HexPosition object, or null if not found
     */
    HexPosition getHexPosition(final int textOffset) {
	final var text = hexText.getText();
	if (textOffset >= text.length()) {
	    return null;
	}

	// Find line number and position in line
	var lineStart = 0;
	var lineNum = 0;

	for (var i = 0; i < textOffset && i < text.length(); i++) {
	    if (text.charAt(i) == '\n') {
		lineStart = i + 1;
		lineNum++;
	    }
	}

	final var colInLine = textOffset - lineStart;

	// Find which byte this position corresponds to
	for (var i = 0; i < BYTES_PER_LINE; i++) {
	    var hexPos = i * 3;
	    if (i >= 8) {
		hexPos++; // Extra space after 8th byte
	    }
	    if (colInLine >= hexPos && colInLine <= hexPos + 1) {
		final var byteIndex = lineNum * BYTES_PER_LINE + i;
		final var isHighNibble = (colInLine == hexPos);
		final var correctOffset = lineStart + hexPos + (isHighNibble ? 0 : 1);
		return new HexPosition(byteIndex, isHighNibble, correctOffset);
	    }
	}
	return null;
    }

    private int getLineStartOffset(final int lineNumber) {
	final var text = hexText.getText();
	var offset = 0;
	var currentLine = 0;

	while (currentLine < lineNumber && offset < text.length()) {
	    if (text.charAt(offset) == '\n') {
		currentLine++;
	    }
	    offset++;
	}

	return offset;
    }

    /**
     * Gets the main application shell.
     *
     * @return the SWT Shell
     */
    Shell getShell() {
	return shell;
    }

    /**
     * Moves the caret to the specified byte offset in the file.
     *
     * @param offset the byte offset to move to
     */
    void gotoOffset(final int offset) {
	if (fileData == null || offset < 0 || offset >= fileData.length) {
	    return;
	}

	final var line = offset / BYTES_PER_LINE;
	final var byteInLine = offset % BYTES_PER_LINE;

	var column = byteInLine * 3;
	if (byteInLine >= 8) {
	    column++;
	}

	final var lineStartOffset = getLineStartOffset(line);
	final var caretOffset = lineStartOffset + column;

	hexText.setTopIndex(line);
	hexText.setCaretOffset(caretOffset);
	hexText.setFocus();
    }

    private void handleHexKeyPress(final KeyEvent e) {
	editController.handleHexKeyPress(e, fileData);
    }

    boolean isModified() {
	return modified;
    }

    /**
     * Shows a message box dialog with the given title, message, and style.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param style   the SWT style flags
     */
    void messageBox(final String title, final String message, final int style) {
	final var messageBox = new MessageBox(shell, style);
	messageBox.setText(title);
	messageBox.setMessage(message);
	messageBox.open();
    }

    /**
     * Moves the caret to the next hex position based on the current position.
     *
     * @param pos the current HexPosition
     */
    void moveToNextHexPosition(final HexPosition pos) {
	final int nextByteIndex;
	final boolean nextIsHighNibble;

	if (pos.isHighNibble) {
	    // Move to low nibble of same byte
	    nextByteIndex = pos.byteIndex;
	    nextIsHighNibble = false;
	} else {
	    // Move to high nibble of next byte
	    nextByteIndex = pos.byteIndex + 1;
	    nextIsHighNibble = true;
	}

	if (nextByteIndex >= fileData.length) {
	    return; // End of file
	}

	final var line = nextByteIndex / BYTES_PER_LINE;
	final var byteInLine = nextByteIndex % BYTES_PER_LINE;

	var hexPos = byteInLine * 3;
	if (byteInLine >= 8) {
	    hexPos++; // Extra space after 8th byte
	}

	final var lineStartOffset = getLineStartOffset(line);
	final var targetOffset = lineStartOffset + hexPos + (nextIsHighNibble ? 0 : 1);

	hexText.setCaretOffset(targetOffset);
    }

    private void openFile() {
	final var dialog = new FileDialog(shell, SWT.OPEN);
	dialog.setFilterNames(new String[] { "All Files (*.*)" });
	dialog.setFilterExtensions(new String[] { "*.*" });

	final var fileName = dialog.open();
	if (fileName != null) {
	    try {
		fileData = fileManager.openFile(fileName);
		currentFile = fileManager.getCurrentFile();
		refreshDisplay();
		setModified(false);
		updateTitle();
		statusLabel.setText(new StringBuilder().append("Loaded: ").append(fileName).append(" (")
			.append(fileData.length).append(" bytes)").toString());
		if (saveItem != null) {
		    saveItem.setEnabled(true);
		}
		if (saveAsItem != null) {
		    saveAsItem.setEnabled(true);
		}
		if (hexText != null) {
		    hexText.setEnabled(true);
		}
		if (asciiText != null) {
		    asciiText.setEnabled(true);
		}
		if (offsetText != null) {
		    offsetText.setEnabled(true);
		}
	    } catch (final IOException e) {
		messageBox("Error", "Failed to open file: " + e.getMessage(), SWT.ICON_ERROR | SWT.OK);
	    }
	}
    }

    private void openFindDialog() {
	dialogManager.openFindDialog();
    }

    private void openGotoDialog() {
	dialogManager.openGotoDialog();
    }

    private void positionCursorOnHexDigit() {
	final var offset = hexText.getCaretOffset();
	final var pos = getHexPosition(offset);

	if (pos != null) {
	    hexText.setCaretOffset(pos.textOffset);
	}
    }

    private void refreshDisplay() {
	if (fileData == null) {
	    displayHexData();
	    return;
	}
	final var caretOffset = hexText.getCaretOffset();
	displayManager.refreshDisplay(fileData, caretOffset);
    }

    private void saveAsFile() {
	final var dialog = new FileDialog(shell, SWT.SAVE);
	dialog.setFilterNames(new String[] { "All Files (*.*)" });
	dialog.setFilterExtensions(new String[] { "*.*" });

	final var fileName = dialog.open();
	if (fileName == null) {
	    return;
	}
	try {
	    fileManager.saveFileAs(fileName, fileData);
	    currentFile = fileManager.getCurrentFile();
	    saveFile();
	} catch (final IOException e) {
	    messageBox("Error", "Failed to save file: " + e.getMessage(), SWT.ICON_ERROR | SWT.OK);
	}
    }

    private void saveFile() {
	if (currentFile == null) {
	    saveAsFile();
	    return;
	}

	try {
	    fileManager.setCurrentFile(currentFile);
	    fileManager.saveFile(fileData);
	    setModified(false);
	    updateTitle();
	    statusLabel.setText("Saved: " + currentFile);
	} catch (final IOException e) {
	    messageBox("Error", "Failed to save file: " + e.getMessage(), SWT.ICON_ERROR | SWT.OK);
	}
    }

    void setModified(final boolean modified) {
	this.modified = modified;
	shell.setModified(modified);
    }

    /**
     * Updates the byte at the given hex position with the specified hex character.
     *
     * @param pos     the HexPosition to update
     * @param hexChar the hex character to write
     */
    void updateByteAtPosition(final HexPosition pos, final char hexChar) {
	final var currentByte = fileData[pos.byteIndex];
	final var currentValue = currentByte & 0xFF;
	final var newNibble = Character.digit(hexChar, 16);
	final int newValue;

	if (pos.isHighNibble) {
	    newValue = (newNibble << 4) | (currentValue & 0x0F);
	} else {
	    newValue = (currentValue & 0xF0) | newNibble;
	}

	fileData[pos.byteIndex] = (byte) newValue;
    }

    /**
     * Updates the display for a single byte at the given index.
     *
     * @param byteIndex the index of the byte to update
     * @param topIndex  the top visible line index to restore
     */
    void updateSingleByteDisplay(final int byteIndex, final int topIndex) {
	if (fileData == null) {
	    return;
	}
	final var caretOffset = hexText.getCaretOffset();
	displayManager.updateSingleByte(fileData, byteIndex, caretOffset, topIndex);
    }

    /**
     * Updates the window title to reflect the current file and modification state.
     */
    void updateTitle() {
	final var title = new StringBuilder("Hex Editor");
	if (currentFile != null) {
	    title.append(" - ").append(currentFile);
	    if (isModified()) {
		title.append(" *");
	    }
	}
	shell.setText(title.toString());
    }
}