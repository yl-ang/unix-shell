package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CatArgsParserTest {

    @InjectMocks
    private CatArgsParser catArgsParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void isLineNumber_FlagPresent_True() throws CatException {
        String[] args = {"-n", "file.txt"};

        try {
            catArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage());
        }

        assertTrue(catArgsParser.isLineNumber(), "isLineNumber returns False when flag is present");
    }

    @Test
    void isLineNumber_FlagAbsent_False() throws CatException {
        String[] args = {"file.txt"};

        try {
            catArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage());
        }

        assertFalse(catArgsParser.isLineNumber(), "isLineNumber returns True when flag is absent");
    }

    @Test
    void getFileNames_FileNamesInArgs_FileNames() throws CatException {
        String[] args = {"file1.txt", "file2.txt"};

        try {
            catArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage());
        }

        List<String> expectedFileNames = Arrays.asList("file1.txt", "file2.txt");
        assertEquals(expectedFileNames, catArgsParser.getFileNames());
    }

    @Test
    void getFileNames_FileNamesNotInArgs_EmptyList() throws CatException {
        String[] args = {"-n"};

        try {
            catArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage());
        }

        List<String> expectedFileNames = new ArrayList<>();
        assertEquals(expectedFileNames, catArgsParser.getFileNames());
    }
}
