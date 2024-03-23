package unit_tests.sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.util.RegexArgument;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

class RegexArgumentTest {
    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIR_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "unit_tests", "regexArgument"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIR_ARR);
    private static final String SECOND_REGEX_ARG = "regexArg2";

    private RegexArgument regexArg;

    @BeforeEach
    void setUp() {
        regexArg = new RegexArgument();
    }

    @Test
    void testConstructor_WithoutArgs_EmptyStringNotRegex() {
        assertFalse(regexArg.isRegex());
        assertTrue(regexArg.isEmpty());
    }

    @Test
    void testConstructorWithArgs_nonEmptyString_stringNotRegex() {
        regexArg = new RegexArgument("test");
        assertFalse(regexArg.isRegex());
        assertFalse(regexArg.isEmpty());
    }

    @Test
    void testConstructorWithArgs_emptyString_EmptyStringNotRegex() {
        regexArg = new RegexArgument("");
        assertFalse(regexArg.isRegex());
        assertTrue(regexArg.isEmpty());
    }

    @Test
    void append_nonEmptyChar_charAppended() {
        regexArg.append('c');
        assertFalse(regexArg.isEmpty());
        assertEquals("c", regexArg.toString());
    }

    @Test
    void append_nonEmptyExistingPlaintext_charAppendedToExisting() {
        regexArg.append('c');
        assertEquals("c", regexArg.toString());

        regexArg.append('d');
        assertFalse(regexArg.isEmpty());
        assertEquals("cd", regexArg.toString());
    }

    @Test
    void appendAsterisk_noArgs_asteriskAppended() {
        regexArg.appendAsterisk();
        assertFalse(regexArg.isEmpty());
        assertEquals("*", regexArg.toString());
    }

    @Test
    void appendAsterisk_noArgs_isRegex() {
        regexArg.appendAsterisk();
        assertTrue(regexArg.isRegex());
    }

    @Test
    void merge_nullStringArg_nothingHappens() {
        RegexArgument regexArg2 = new RegexArgument(null);
        regexArg.merge(regexArg2);
        assertEquals("", regexArg.toString());
    }

    @Test
    void merge_regexArgumentInstanceProvided_plainTextUpdated() {
        RegexArgument regexArg2 = new RegexArgument(SECOND_REGEX_ARG);
        regexArg.merge(regexArg2);
        assertEquals(SECOND_REGEX_ARG, regexArg.toString());
    }

    @Test
    void merge_regexArgumentInstanceProvided_isRegexUpdated() {
        assertFalse(regexArg.isRegex());
        RegexArgument regexArg2 = new RegexArgument(SECOND_REGEX_ARG);
        regexArg2.appendAsterisk();

        regexArg.merge(regexArg2);
        assertTrue(regexArg.isRegex());
    }

    @Test
    void merge_stringProvided_plainTextUpdated() {
        regexArg.merge(SECOND_REGEX_ARG);
        assertEquals(SECOND_REGEX_ARG, regexArg.toString());
    }

    @Test
    void globFiles_isNotRegex_plaintext() {
        regexArg.merge(SECOND_REGEX_ARG);
        assertFalse(regexArg.isRegex());

        List<String> expectedList = List.of(SECOND_REGEX_ARG);
        assertEquals(expectedList, regexArg.globFiles());
    }

    @Test
    void globFiles_isRegexButListOfGlobbedFilesEmpty_plaintext() {
        regexArg.merge(SECOND_REGEX_ARG);
        regexArg.appendAsterisk();
        assertTrue(regexArg.isRegex());

        List<String> expectedList = List.of("regexArg2*");
        assertEquals(expectedList, regexArg.globFiles());
    }

    @Test
    void globFiles_isRegex_listOfGlobbedFiles() {
        Environment.currentDirectory = TEST_DIRECTORY;

        regexArg.appendAsterisk();
        regexArg.merge(".txt");
        assertTrue(regexArg.isRegex());

        List<String> expectedList = List.of("test1.txt", "test2.txt");
        assertEquals(expectedList, regexArg.globFiles());
        Environment.currentDirectory = ROOT_DIRECTORY;
    }

    @Test
    void testToString_nonEmptyPlaintext_plaintext() {
        regexArg.append('c');
        assertEquals("c", regexArg.toString());
    }
}