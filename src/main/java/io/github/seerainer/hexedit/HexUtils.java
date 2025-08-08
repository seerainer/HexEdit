package io.github.seerainer.hexedit;

import java.util.HexFormat;

/**
 * Utility class for hex string and byte array conversions and search
 * operations. Provides methods for validating hex characters, converting
 * between hex strings and bytes, and searching for byte sequences within
 * arrays.
 */
class HexUtils {
    private HexUtils() {
	throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a hex string to a byte array.
     *
     * @param hexString the hex string to convert (may contain whitespace)
     * @return the corresponding byte array
     * @throws NumberFormatException if the string is not valid hex
     */
    static byte[] hexStringToBytes(final String hexString) {
	final var str = hexString.replaceAll("\\s+", "");
	if (str.length() % 2 != 0) {
	    throw new NumberFormatException("Hex string must have even length");
	}
	for (var i = 0; i < str.length(); i++) {
	    if (!isHexChar(str.charAt(i))) {
		throw new NumberFormatException(new StringBuilder().append("Invalid hex character: '")
			.append(str.charAt(i)).append("' at position ").append(i).toString());
	    }
	}
	try {
	    return HexFormat.of().parseHex(str);
	} catch (final IllegalArgumentException e) {
	    throw new NumberFormatException("Invalid hex string: " + e.getMessage());
	}
    }

    /**
     * Finds the index of the first occurrence of the target byte array in the
     * source array.
     *
     * @param array  the source byte array
     * @param target the target byte array to search for
     * @return the index of the first occurrence, or -1 if not found
     */
    static int indexOf(final byte[] array, final byte[] target) {
	if (target.length == 0) {
	    return 0;
	}
	for (var i = 0; i <= array.length - target.length; i++) {
	    var found = true;
	    for (var j = 0; j < target.length; j++) {
		if (array[i + j] != target[j]) {
		    found = false;
		    break;
		}
	    }
	    if (found) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Checks if a character is a valid hexadecimal digit (0-9, a-f, A-F).
     *
     * @param c the character to check
     * @return true if the character is a hex digit, false otherwise
     */
    static boolean isHexChar(final char c) {
	return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    // Optionally, add a bytesToHex method using HexFormat
    // static String bytesToHex(final byte[] bytes) {
    // return HexFormat.of().formatHex(bytes);
    // }

}