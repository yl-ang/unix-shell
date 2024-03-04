package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;

import static org.junit.jupiter.api.Assertions.*;

class GrepArgsParserTest {

    @InjectMocks
    private GrepArgsParser grepArgsParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void isInvert_FlagPresent_True() throws GrepException {
        String[] args = {"-v", "pattern", "file.txt"};

        try {
            grepArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage());
        }

        assertTrue(grepArgsParser.isInvert(), "isInvert returns False when flag is present");
    }

    @Test
    void isInvert_FlagAbsent_False() throws GrepException {
        String[] args = {"pattern", "file.txt"};

        try {
            grepArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage());
        }

        assertFalse(grepArgsParser.isInvert(), "isInvert returns True when flag is absent");
    }

    @Test
    void getPattern_PatternInArgs_Pattern() throws GrepException {
        String[] args = {"-v", "pattern", "file.txt"};

        try {
            grepArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage());
        }

        assertEquals("pattern", grepArgsParser.getPattern());
    }

    @Test
    void getPattern_PatternNotInArgs_Null() throws GrepException {
        String[] args = {"-v"};

        try {
            grepArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage());
        }

        assertNull(grepArgsParser.getPattern());
    }

    @Test
    void getFileNames_FilesInArgs_Files() throws GrepException {
        String[] args = {"-v", "pattern", "file1.txt", "file2.txt"};

        try {
            grepArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage());
        }

        String[] expectedFiles = {"file1.txt", "file2.txt"};
        assertArrayEquals(expectedFiles, grepArgsParser.getFileNames());
    }

    @Test
    void getFileNames_FilesNotInArgs_Null() throws GrepException {
        String[] args = {"-v", "pattern"};

        try {
            grepArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage());
        }

        assertNull(grepArgsParser.getFileNames());
    }
}
