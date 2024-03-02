package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirParser;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MkdirParserTest {

    private static MkdirParser mkdirParser;
    private static final String FOLDER_NAME = "folder";
    private static final String FOLDER_NAME_ADDITIONAL = "folder2";
    private static final String FOLDER_WITH_PARENT = "folder" + File.separator + "parent";
    private static final String FLAG_IS_CREATE_PARENT_STRING = "-" + MkdirParser.FLAG_IS_CREATE_PARENT;
    private static final String FLAG_IS_ILLGEGAL= "-k";

    @BeforeEach
    void init() {
        mkdirParser = new MkdirParser();
    }

    @Test
    public void parse_IllegalFlagPresent_ShouldThrowInvalidArgsException() {
        assertThrows(InvalidArgsException.class, () -> mkdirParser.parse(FLAG_IS_ILLGEGAL, FOLDER_NAME));
    }

    @Test
    public void isCreateParent_TestIsCreateParentFlagPresent_FlagShouldBePresent() throws InvalidArgsException {
        mkdirParser.parse(FLAG_IS_CREATE_PARENT_STRING, FOLDER_NAME);
        assertTrue(mkdirParser.isCreateParent());
    }

    @Test
    public void isCreateParent_TestIsCreateParentFlagPresent_FlagShouldNotBePresent() throws InvalidArgsException {
        mkdirParser.parse(FOLDER_NAME);
        assertFalse(mkdirParser.isCreateParent());
    }

    @Test
    public void getFolderPath_TestGetFolderPath_ShouldBeAbleToObtainFolder() throws InvalidArgsException {
        mkdirParser.parse(FLAG_IS_CREATE_PARENT_STRING, FOLDER_WITH_PARENT);
        assertEquals(FOLDER_WITH_PARENT, mkdirParser.getFolderPath().get(0));
    }

    @Test
    public void getFolderPath_TestGetFolderPathWithoutFlag_ShouldBeAbleToObtainFolder() throws InvalidArgsException {
        mkdirParser.parse(FOLDER_NAME);
        assertEquals(FOLDER_NAME, mkdirParser.getFolderPath().get(0));
    }

    @Test
    public void getFolderPath_TestGetFolderPathMultiplePaths_ShouldBeAbleToObtainFolders() throws InvalidArgsException {
        mkdirParser.parse(FLAG_IS_CREATE_PARENT_STRING, FOLDER_WITH_PARENT, FOLDER_NAME_ADDITIONAL);
        assertEquals(FOLDER_WITH_PARENT, mkdirParser.getFolderPath().get(0));
        assertEquals(FOLDER_NAME_ADDITIONAL, mkdirParser.getFolderPath().get(1));
    }

    @Test
    public void getFolderPath_TestGetFolderPathEmpty_ShouldBeEmpty() throws InvalidArgsException {
        mkdirParser.parse();
        assertTrue(mkdirParser.getFolderPath().isEmpty());
    }
}
