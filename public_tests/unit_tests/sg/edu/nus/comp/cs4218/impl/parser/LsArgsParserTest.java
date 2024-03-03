package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LsArgsParserTest {
    private LsArgsParser lsArgsParser;

    @BeforeEach
    void setUp() {
        lsArgsParser = new LsArgsParser();
    }

    @Test
    void isRecursive_FlagPresent_True() throws InvalidArgsException {
        String[] args = {"-R"};
        lsArgsParser.parse(args);
        assertTrue(lsArgsParser.isRecursive());
    }

    @Test
    void isRecursive_FlagAbsent_False() throws InvalidArgsException {
        String[] args = {"-X"};
        lsArgsParser.parse(args);
        assertFalse(lsArgsParser.isRecursive());
    }

    @Test
    void isSortByExt_FlagPresent_True() throws InvalidArgsException {
        String[] args = {"-X"};
        lsArgsParser.parse(args);
        assertTrue(lsArgsParser.isSortByExt());
    }

    @Test
    void isSortByExt_FlagAbsent_False() throws InvalidArgsException {
        String[] args = {"-R"};
        lsArgsParser.parse(args);
        assertFalse(lsArgsParser.isSortByExt());
    }

    @Test
    void getDirectories_DirectoriesPresent_DirectoriesList() throws InvalidArgsException {
        String[] args = {"dir1", "dir2"};
        lsArgsParser.parse(args);
        List<String> directories = lsArgsParser.getDirectories();
        assertEquals(List.of("dir1", "dir2"), directories);
    }

    @Test
    void getDirectories_NoDirectoriesPresent_EmptyList() throws InvalidArgsException {
        String[] args = {"-R"};
        lsArgsParser.parse(args);
        List<String> directories = lsArgsParser.getDirectories();
        assertTrue(directories.isEmpty());
    }
}
