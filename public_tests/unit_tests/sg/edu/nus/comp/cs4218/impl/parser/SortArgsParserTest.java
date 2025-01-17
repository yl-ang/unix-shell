package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SuppressWarnings("PMD.LongVariable") // Testing Purpose for clarity
class SortArgsParserTest {

    private SortArgsParser parser;
    public static final String FLAG_IS_FIRST_NUM = "-n";
    public static final String FLAG_IS_REV_ORDER = "-r";
    public static final String FLAG_IS_CASE_IGNORE = "-f";

    @BeforeEach
    void setUp() {
        parser = new SortArgsParser();
    }

    @Test
    void isFirstWordNumber_TestIsFirstWordNumber_FlagIsFirstWordNumber() throws InvalidArgsException {
        assertFalse(parser.isFirstWordNumber());
        parser.parse(FLAG_IS_FIRST_NUM);
        assertTrue(parser.isFirstWordNumber());
    }

    @Test
    void isReverseOrder_TestIsReverseOrder_FlagIsRevOrder() throws InvalidArgsException {
        assertFalse(parser.isReverseOrder());
        parser.parse(FLAG_IS_REV_ORDER);
        assertTrue(parser.isReverseOrder());
    }

    @Test
    void isCaseIndependent_TestIsCaseIndependent_FlagIsCaseIndependent() throws InvalidArgsException {
        assertFalse(parser.isCaseIndependent());
        parser.parse(FLAG_IS_CASE_IGNORE);
        assertTrue(parser.isCaseIndependent());
    }

    @Test
    void getFileNames_TestGetFileNames_ShouldGetCorrectFileNames() throws InvalidArgsException {
        parser.parse("-n", "file1.txt", "file2.txt");
        List<String> fileNames = parser.getFileNames();
        assertEquals(2, fileNames.size());
        assertEquals("file1.txt", fileNames.get(0));
        assertEquals("file2.txt", fileNames.get(1));
    }
}
