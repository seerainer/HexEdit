package io.github.seerainer.hexedit;

import org.eclipse.swt.custom.StyledText;

/**
 * Manages the display of hex, ASCII, and offset data in the Hex Editor UI.
 * Handles rendering, refreshing, and updating of the display areas for file
 * data.
 */
class DisplayManager {
    private final StyledText offsetText;
    private final StyledText hexText;
    private final StyledText asciiText;
    private final int bytesPerLine;

    /**
     * Constructs a DisplayManager for the given text widgets and bytes per line.
     *
     * @param offsetText   the StyledText widget for offsets
     * @param hexText      the StyledText widget for hex data
     * @param asciiText    the StyledText widget for ASCII data
     * @param bytesPerLine the number of bytes to display per line
     */
    DisplayManager(final StyledText offsetText, final StyledText hexText, final StyledText asciiText,
	    final int bytesPerLine) {
	this.offsetText = offsetText;
	this.hexText = hexText;
	this.asciiText = asciiText;
	this.bytesPerLine = bytesPerLine;
    }

    /**
     * Displays the given file data in the offset, hex, and ASCII areas.
     *
     * @param fileData the file data to display
     */
    void displayHexData(final byte[] fileData) {
	if (fileData == null) {
	    offsetText.setText("");
	    hexText.setText("");
	    asciiText.setText("");
	    return;
	}
	final var hexBuilder = new StringBuilder();
	final var asciiBuilder = new StringBuilder();
	final var offsetBuilder = new StringBuilder();
	for (var i = 0; i < fileData.length; i += bytesPerLine) {
	    offsetBuilder.append("%08X%n".formatted(Integer.valueOf(i)));
	    for (var j = 0; j < bytesPerLine; j++) {
		if (i + j < fileData.length) {
		    final var b = fileData[i + j] & 0xFF;
		    hexBuilder.append("%02X ".formatted(Integer.valueOf(b)));
		} else {
		    hexBuilder.append("   ");
		}
		if (j == 7) {
		    hexBuilder.append(" ");
		}
	    }
	    hexBuilder.append("\n");
	    for (var j = 0; j < bytesPerLine && i + j < fileData.length; j++) {
		final var b = fileData[i + j];
		final var c = (b >= 32 && b <= 126) ? (char) b : '.';
		asciiBuilder.append(c);
	    }
	    asciiBuilder.append("\n");
	}
	offsetText.setText(offsetBuilder.toString());
	hexText.setText(hexBuilder.toString());
	asciiText.setText(asciiBuilder.toString());
    }

    /**
     * Refreshes the display with the given file data and caret offset.
     *
     * @param fileData    the file data to display
     * @param caretOffset the caret offset to restore
     */
    void refreshDisplay(final byte[] fileData, final int caretOffset) {
	displayHexData(fileData);
	if (caretOffset <= hexText.getCharCount()) {
	    hexText.setCaretOffset(caretOffset);
	}
    }

    /**
     * Refreshes the display while preserving the scroll position and caret offset.
     *
     * @param fileData    the file data to display
     * @param caretOffset the caret offset to restore
     * @param topIndex    the top visible line index to restore
     */
    void refreshDisplayPreservingScroll(final byte[] fileData, final int caretOffset, final int topIndex) {
	offsetText.setRedraw(false);
	hexText.setRedraw(false);
	asciiText.setRedraw(false);
	try {
	    displayHexData(fileData);
	    offsetText.setTopIndex(topIndex);
	    hexText.setTopIndex(topIndex);
	    asciiText.setTopIndex(topIndex);
	    if (caretOffset <= hexText.getCharCount()) {
		hexText.setCaretOffset(caretOffset);
	    }
	} finally {
	    offsetText.setRedraw(true);
	    hexText.setRedraw(true);
	    asciiText.setRedraw(true);
	}
    }

    private void updateAsciiByte(final int line, final int byteInLine, final byte b) {
	final var asciiLineLength = bytesPerLine + 1; // 1 char per byte + newline
	final var lineStartOffset = line * asciiLineLength;
	final var byteOffset = lineStartOffset + byteInLine;

	final var c = (b >= 32 && b <= 126) ? String.valueOf((char) b) : ".";

	asciiText.replaceTextRange(byteOffset, 1, c);
    }

    private void updateHexByte(final int line, final int byteInLine, final byte b) {
	final var hexLineLength = bytesPerLine * 3 + 1 + 1; // 3 chars per byte + extra space after 8th byte + newline
	final var lineStartOffset = line * hexLineLength;

	var bytePosition = byteInLine * 3;
	if (byteInLine >= 8) {
	    bytePosition++; // Extra space after 8th byte
	}

	final var byteOffset = lineStartOffset + bytePosition;
	final var hexValue = "%02X".formatted(Integer.valueOf(b & 0xFF));

	hexText.replaceTextRange(byteOffset, 2, hexValue);
    }

    /**
     * Updates the display for a single byte at the given index.
     *
     * @param fileData    the file data
     * @param byteIndex   the index of the byte to update
     * @param caretOffset the caret offset to restore
     * @param topIndex    the top visible line index to restore
     */
    void updateSingleByte(final byte[] fileData, final int byteIndex, final int caretOffset, final int topIndex) {
	if (fileData == null || byteIndex < 0 || byteIndex >= fileData.length) {
	    return;
	}

	hexText.setRedraw(false);
	asciiText.setRedraw(false);
	try {
	    final var line = byteIndex / bytesPerLine;
	    final var byteInLine = byteIndex % bytesPerLine;

	    updateHexByte(line, byteInLine, fileData[byteIndex]);
	    updateAsciiByte(line, byteInLine, fileData[byteIndex]);

	    hexText.setTopIndex(topIndex);
	    asciiText.setTopIndex(topIndex);
	    if (caretOffset <= hexText.getCharCount()) {
		hexText.setCaretOffset(caretOffset);
	    }
	} finally {
	    hexText.setRedraw(true);
	    asciiText.setRedraw(true);
	}
    }
}