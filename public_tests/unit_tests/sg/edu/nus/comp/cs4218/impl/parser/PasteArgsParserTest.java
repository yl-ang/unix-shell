package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasteArgsParserTest {

    @InjectMocks
    private PasteArgsParser pasteArgsParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void isSerial_FlagPresent_True() throws InvalidArgsException {
        String[] args = {"-s", "file.txt"};

        pasteArgsParser.parse(args);

        assertTrue(pasteArgsParser.isSerial(), "isSerial returns False when flag is present");
    }

    @Test
    void isSerial_FlagAbsent_False() throws InvalidArgsException {
        String[] args = {"file.txt"};

        pasteArgsParser.parse(args);

        assertFalse(pasteArgsParser.isSerial(), "isSerial returns True when flag is absent");
    }

    @Test
    void getFileNames_FileNamesInArgs_FileNames() throws InvalidArgsException {
        String[] args = {"file1.txt", "file2.txt"};

        pasteArgsParser.parse(args);

        List<String> expectedFileNames = Arrays.asList("file1.txt", "file2.txt");
        assertEquals(expectedFileNames, pasteArgsParser.getFileNames());
    }

    @Test
    void getFileNames_FileNamesNotInArgs_EmptyList() throws InvalidArgsException {
        String[] args = {"-s"};

        pasteArgsParser.parse(args);

        List<String> expectedFileNames = new ArrayList<>();
        assertEquals(expectedFileNames, pasteArgsParser.getFileNames());
    }
}
