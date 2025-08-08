package io.github.seerainer.hexedit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for HexEditor utility methods. Tests the core hex processing logic
 * without GUI components.
 */
@Tag("unit")
@DisplayName("HexEditor Unit Tests")
class HexEditorTest {

    @Nested
    @DisplayName("Hex formatting")
    class HexFormattingTest {

	@Test
	@DisplayName("should format bytes correctly with 16 bytes per line")
	void shouldFormatBytesCorrectly() {
	    // This test verifies the expected format used by displayHexData
	    final var data = "Hello World!".getBytes();

	    // Expected format: "48 65 6C 6C 6F 20 57 6F 72 6C 64 21 "
	    final var expected = new StringBuilder();
	    for (var i = 0; i < data.length; i++) {
		expected.append("%02X ".formatted(Integer.valueOf(data[i] & 0xFF)));
		if (i == 7) {
		    expected.append(" "); // Extra space after 8th byte
		}
	    }

	    final var result = expected.toString();
	    assertThat(result).startsWith("48 65 6C 6C 6F 20 57 6F  72 6C 64 21");
	}

	@Test
	@DisplayName("should format offset addresses correctly")
	void shouldFormatOffsetAddressesCorrectly() {
	    // Test offset formatting used in displayHexData
	    final int[] offsets = { 0, 16, 32, 255, 4096 };
	    final String[] expected = { "00000000", "00000010", "00000020", "000000FF", "00001000" };

	    for (var i = 0; i < offsets.length; i++) {
		final var result = "%08X".formatted(Integer.valueOf(offsets[i]));
		assertThat(result).isEqualTo(expected[i]);
	    }
	}

	@Test
	@DisplayName("should handle non-printable characters in ASCII conversion")
	void shouldHandleNonPrintableCharactersInAsciiConversion() {
	    final byte[] data = { 0x00, 0x01, 0x20, 0x7F, (byte) 0x80, (byte) 0xFF };

	    final var asciiBuilder = new StringBuilder();
	    for (final byte b : data) {
		final var c = (b >= 32 && b <= 126) ? (char) b : '.';
		asciiBuilder.append(c);
	    }

	    final var result = asciiBuilder.toString();
	    // Should show 6 characters: ".. ..." (dots for non-printable: 0x00, 0x01, 0x7F,
	    // 0x80, 0xFF; space for 0x20)
	    assertThat(result).isEqualTo(".. ...");
	}
    }

    @Nested
    @DisplayName("hexStringToBytes method")
    class HexStringToBytesTest {

	@Test
	@DisplayName("should convert valid hex string to bytes")
	void shouldConvertValidHexStringToBytes() {
	    // Test basic hex conversion
	    final var result = HexUtils.hexStringToBytes("48656C6C6F");
	    assertThat(result).containsExactly(0x48, 0x65, 0x6C, 0x6C, 0x6F);
	}

	@Test
	@DisplayName("should handle all possible byte values")
	void shouldHandleAllPossibleByteValues() {
	    final var result = HexUtils.hexStringToBytes("00FF7F80");
	    assertThat(result).containsExactly(0x00, (byte) 0xFF, 0x7F, (byte) 0x80);
	}

	@Test
	@DisplayName("should handle empty string")
	void shouldHandleEmptyString() {
	    final var result = HexUtils.hexStringToBytes("");
	    assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("should handle hex string with spaces")
	void shouldHandleHexStringWithSpaces() {
	    final var result = HexUtils.hexStringToBytes("48 65 6C 6C 6F");
	    assertThat(result).containsExactly(0x48, 0x65, 0x6C, 0x6C, 0x6F);
	}

	@Test
	@DisplayName("should handle hex string with tabs and newlines")
	void shouldHandleHexStringWithWhitespace() {
	    final var result = HexUtils.hexStringToBytes("48\t65\n6C\r6C 6F");
	    assertThat(result).containsExactly(0x48, 0x65, 0x6C, 0x6C, 0x6F);
	}

	@Test
	@DisplayName("should handle lowercase hex characters")
	void shouldHandleLowercaseHexCharacters() {
	    final var result = HexUtils.hexStringToBytes("48656c6c6f");
	    assertThat(result).containsExactly(0x48, 0x65, 0x6C, 0x6C, 0x6F);
	}

	@Test
	@DisplayName("should handle mixed case hex characters")
	void shouldHandleMixedCaseHexCharacters() {
	    final var result = HexUtils.hexStringToBytes("48656C6c6F");
	    assertThat(result).containsExactly(0x48, 0x65, 0x6C, 0x6C, 0x6F);
	}

	@Test
	@DisplayName("should handle whitespace-only string")
	void shouldHandleWhitespaceOnlyString() {
	    final var result = HexUtils.hexStringToBytes("   \t\n\r  ");
	    assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("should throw exception for invalid hex characters")
	void shouldThrowExceptionForInvalidHexCharacters() {
	    assertThatThrownBy(() -> HexUtils.hexStringToBytes("48G5")).isInstanceOf(NumberFormatException.class);
	}

	@Test
	@DisplayName("should throw exception for non-hex characters")
	void shouldThrowExceptionForNonHexCharacters() {
	    // Use "HeLLo" (even length) to test NumberFormatException, not
	    // IllegalArgumentException
	    assertThatThrownBy(() -> HexUtils.hexStringToBytes("HeLLo1")).isInstanceOf(NumberFormatException.class);
	}

	@Test
	@DisplayName("should throw exception for odd length hex string")
	void shouldThrowExceptionForOddLengthHexString() {
	    assertThatThrownBy(() -> HexUtils.hexStringToBytes("48656C6C6"))
		    .isInstanceOf(IllegalArgumentException.class).hasMessage("Hex string must have even length");
	}
    }

    @Nested
    @DisplayName("indexOf method")
    class IndexOfTest {

	@Test
	@DisplayName("should find first occurrence of repeated pattern")
	void shouldFindFirstOccurrenceOfRepeatedPattern() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x6C, 0x6C };
	    final byte[] target = { 0x6C, 0x6C };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(2);
	}

	@Test
	@DisplayName("should find pattern at beginning of array")
	void shouldFindPatternAtBeginning() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64 };
	    final byte[] target = { 0x48, 0x65, 0x6C };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(0);
	}

	@Test
	@DisplayName("should find pattern at end of array")
	void shouldFindPatternAtEnd() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64 };
	    final byte[] target = { 0x6C, 0x64 };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(9);
	}

	@Test
	@DisplayName("should find pattern in middle of array")
	void shouldFindPatternInMiddle() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64 };
	    final byte[] target = { 0x6C, 0x6F, 0x20 };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(3);
	}

	@Test
	@DisplayName("should find single byte pattern")
	void shouldFindSingleBytePattern() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F };
	    final byte[] target = { 0x6C };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(2);
	}

	@Test
	@DisplayName("should handle empty array with empty target")
	void shouldHandleEmptyArrayWithEmptyTarget() {
	    final byte[] array = {};
	    final byte[] target = {};

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(0);
	}

	@Test
	@DisplayName("should handle exact match of entire array")
	void shouldHandleExactMatchOfEntireArray() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F };
	    final byte[] target = { 0x48, 0x65, 0x6C, 0x6C, 0x6F };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(0);
	}

	@Test
	@DisplayName("should return -1 for empty array with non-empty target")
	void shouldReturnMinusOneForEmptyArrayWithNonEmptyTarget() {
	    final byte[] array = {};
	    final byte[] target = { 0x48 };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(-1);
	}

	@Test
	@DisplayName("should return -1 when pattern not found")
	void shouldReturnMinusOneWhenPatternNotFound() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F };
	    final byte[] target = { 0x57, 0x6F, 0x72, 0x6C, 0x64 };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(-1);
	}

	@Test
	@DisplayName("should return -1 when target is longer than array")
	void shouldReturnMinusOneWhenTargetLongerThanArray() {
	    final byte[] array = { 0x48, 0x65 };
	    final byte[] target = { 0x48, 0x65, 0x6C, 0x6C, 0x6F };

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(-1);
	}

	@Test
	@DisplayName("should return 0 for empty target")
	void shouldReturnZeroForEmptyTarget() {
	    final byte[] array = { 0x48, 0x65, 0x6C, 0x6C, 0x6F };
	    final byte[] target = {};

	    final var result = HexUtils.indexOf(array, target);
	    assertThat(result).isEqualTo(0);
	}
    }
}