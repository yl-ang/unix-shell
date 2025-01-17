package unit_tests.sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    private final Properties props = System.getProperties();

    @Test
    void testIsBlank_EmptyString_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(""));
    }

    @Test
    void testIsBlank_WhitespaceString_ReturnsTrue() {
        assertTrue(StringUtils.isBlank("    "));
    }

    @Test
    void testIsBlank_NullString_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    void testIsBlank_NonEmptyString_ReturnsFalse() {
        assertFalse(StringUtils.isBlank("abc"));
    }

    @Test
    void testTokenize_NormalString_ReturnsTokens() {
        String input = "hello to this world";
        String[] expectedTokens = {"hello", "to", "this", "world"};
        assertArrayEquals(expectedTokens, StringUtils.tokenize(input));
    }

    @Test
    void testTokenize_EmptyString_ReturnsEmptyArray() {
        String input = "";
        String[] expectedTokens = new String[0];
        assertArrayEquals(expectedTokens, StringUtils.tokenize(input));
    }

    @Test
    void testFileSeparator_OnWindows_ReturnsBackslash() {
        // Simulating Windows OS
        System.setProperty("os.name", "Windows");
        assertEquals("\\" + File.separator, StringUtils.fileSeparator());
    }

    @Test
    void testFileSeparator_OnMac_ReturnsForwardslash() {
        System.setProperty("os.name", "Mac OS");
        assertEquals("/", StringUtils.fileSeparator());
    }

    @Test
    void testFileSeparator_OnUnix_ReturnsForwardSlash() {
        // Simulating Unix-like OS
        System.setProperty("os.name", "Linux");
        assertEquals("/", StringUtils.fileSeparator());
    }

    @Test
    void testMultiplyChar_PositiveTimes_ReturnsRepeatedString() {
        assertEquals("AAAAA", StringUtils.multiplyChar('A', 5));
    }

    @Test
    void testMultiplyChar_ZeroTimes_ReturnsEmptyString() {
        assertEquals("", StringUtils.multiplyChar('A', 0));
    }

    @Test
    void testMultiplyChar_NegativeTimes_ReturnsEmptyString() {
        assertEquals("", StringUtils.multiplyChar('A', -3));
    }

    @Test
    void testIsNumber_ValidNumber_ReturnsTrue() {
        assertTrue(StringUtils.isNumber("12345"));
    }

    @Test
    void testIsNumber_InvalidNumber_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("12a"));
    }

}

