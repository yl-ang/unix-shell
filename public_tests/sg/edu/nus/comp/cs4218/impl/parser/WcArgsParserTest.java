package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.WcException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class WcArgsParserTest {
    WcArgsParser wcArgsParser;

    @BeforeEach
    void setUp() {
        wcArgsParser = new WcArgsParser();
    }

    @Test
    void isByteCount_flagPresent_True() throws WcException {
        String[] args = {"-c"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        assertTrue(wcArgsParser.isByteCount(), "isByteCount returns False when flag is present");
    }

    @Test
    void isByteCount_flagAbsent_False() throws WcException {
        String[] args = {"-l", "-w"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        assertFalse(wcArgsParser.isByteCount(), "isByteCount returns False when flag is present");
    }

    @Test
    void isLineCount_flagPresent_True() throws WcException {
        String[] args = {"-l"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        assertTrue(wcArgsParser.isLineCount(), "isLineCount returns False when flag is present");
    }

    @Test
    void isLineCount_flagAbsent_False() throws WcException {
        String[] args = {"-w"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        assertFalse(wcArgsParser.isLineCount(), "isLineCount returns False when flag is present");
    }

    @Test
    void isWordCount_flagPresent_True() throws WcException {
        String[] args = {"-w"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        assertTrue(wcArgsParser.isWordCount(), "isWordCount returns False when flag is present");
    }

    @Test
    void isWordCount_flagAbsent_False() throws WcException {
        String[] args = {"-c"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        assertFalse(wcArgsParser.isWordCount(), "isWordCount returns False when flag is present");
    }

    @Test
    void parse_threeFlagsGivenTogether_threeFlagsPresent() throws WcException {
        String[] args = {"-clw"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        assertTrue(wcArgsParser.isWordCount(), "isWordCount returns False when flag -clw is given");
        assertTrue(wcArgsParser.isLineCount(), "isLineCount returns False when flag -clw is given");
        assertTrue(wcArgsParser.isByteCount(), "isByteCount returns False when flag -clw is given");
    }


    @Test
    void getFileNames_FileNamesInArgs_fileNames() throws WcException {
        String[] args = {"file1.txt", "file2.txt", "-c"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        List<String> expectedFileNames = Arrays.asList("file1.txt", "file2.txt");
        assertEquals(expectedFileNames, wcArgsParser.getFileNames());
    }

    @Test
    void getFileNames_FileNamesNotInArgs_emptyList() throws WcException {
        String[] args = {"-c"};
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        List<String> expectedFileNames = new ArrayList<>();
        assertEquals(expectedFileNames, wcArgsParser.getFileNames());
    }
}