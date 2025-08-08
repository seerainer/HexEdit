package io.github.seerainer.hexedit;

/**
 * Represents a position within the hex area of the editor, including the byte
 * index, whether the position is on the high nibble or low nibble, and the text
 * offset in the widget.
 */
class HexPosition {
    /** The index of the byte in the file data. */
    final int byteIndex;
    /** True if the position is on the high nibble, false if on the low nibble. */
    final boolean isHighNibble;
    /** The offset in the hex text widget corresponding to this position. */
    final int textOffset;

    /**
     * Constructs a HexPosition.
     *
     * @param byteIndex    the index of the byte in the file data
     * @param isHighNibble true if high nibble, false if low nibble
     * @param textOffset   the offset in the hex text widget
     */
    HexPosition(final int byteIndex, final boolean isHighNibble, final int textOffset) {
	this.byteIndex = byteIndex;
	this.isHighNibble = isHighNibble;
	this.textOffset = textOffset;
    }
}