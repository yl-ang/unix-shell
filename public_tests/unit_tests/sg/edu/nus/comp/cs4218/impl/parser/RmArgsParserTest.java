package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RmArgsParserTest {

    @InjectMocks
    private RmArgsParser rmArgsParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void isRecursive_FlagPresent_True() throws InvalidArgsException {
        String[] args = {"-r", "file.txt"};

        rmArgsParser.parse(args);

        assertTrue(rmArgsParser.isRecursive());
    }

    @Test
    void isRecursive_FlagAbsent_False() throws InvalidArgsException {
        String[] args = {"file.txt"};

        rmArgsParser.parse(args);

        assertFalse(rmArgsParser.isRecursive());
    }

    @Test
    void isEmptyFolder_FlagPresent_True() throws InvalidArgsException {
        String[] args = {"-d", "folder"};

        rmArgsParser.parse(args);

        assertTrue(rmArgsParser.isEmptyFolder());
    }

    @Test
    void isEmptyFolder_FlagAbsent_False() throws InvalidArgsException {
        String[] args = {"folder"};

        rmArgsParser.parse(args);

        assertFalse(rmArgsParser.isEmptyFolder());
    }

    @Test
    void getFileNames_FileNamesInArgs_FileNames() throws InvalidArgsException {
        String[] args = {"file1.txt", "file2.txt"};

        rmArgsParser.parse(args);

        List<String> expectedFileNames = List.of("file1.txt", "file2.txt");
        assertEquals(expectedFileNames, rmArgsParser.getFileNames());
    }

    @Test
    void getFileNames_FileNamesNotInArgs_EmptyList() throws InvalidArgsException {
        String[] args = {"-r"};

        rmArgsParser.parse(args);

        List<String> expectedFileNames = List.of();
        assertEquals(expectedFileNames, rmArgsParser.getFileNames());
    }
}
