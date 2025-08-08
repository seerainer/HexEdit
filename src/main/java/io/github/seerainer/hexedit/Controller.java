package io.github.seerainer.hexedit;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;

/**
 * Handles key press events and editing logic for the hex area of the editor.
 * Validates input, updates file data, and manages caret movement and UI
 * updates.
 */
class Controller {
    private final StyledText hexText;
    private final HexEditor hexEditor;

    /**
     * Constructs a Controller for handling hex editing events.
     *
     * @param hexText   the StyledText widget for hex data
     * @param hexEditor the HexEditor instance
     */
    Controller(final StyledText hexText, final HexEditor hexEditor) {
	this.hexText = hexText;
	this.hexEditor = hexEditor;
    }

    /**
     * Handles key press events in the hex area, updating the file data and UI as
     * needed.
     *
     * @param e        the KeyEvent triggered by user input
     * @param fileData the file data being edited
     */
    void handleHexKeyPress(final KeyEvent e, final byte[] fileData) {
	if (fileData == null) {
	    e.doit = false;
	    return;
	}
	final var c = e.character;
	if (!HexUtils.isHexChar(c)) {
	    e.doit = false;
	    return;
	}
	e.doit = false;
	final var offset = hexText.getCaretOffset();
	final var pos = hexEditor.getHexPosition(offset);
	if (pos == null || pos.byteIndex >= fileData.length) {
	    return;
	}
	final var topIndex = hexText.getTopIndex();
	hexEditor.updateByteAtPosition(pos, c);
	hexEditor.updateSingleByteDisplay(pos.byteIndex, topIndex);
	hexEditor.moveToNextHexPosition(pos);
	hexEditor.setModified(true);
	hexEditor.updateTitle();
    }
}