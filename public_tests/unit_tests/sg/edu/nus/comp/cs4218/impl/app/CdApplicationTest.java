package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.app.CdApplication;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class CdApplicationTest {

    private static CdApplication cdApplication;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "unit_tests", "cd"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);
    private static final String TEST_DIRECTORY_PARENT = String.join(STR_FILE_SEP, Arrays.copyOf(TEST_DIRECTORY_ARR, TEST_DIRECTORY_ARR.length-1));
    private static final String[] EMPTY_STRING_ARRAY = {};
    private static final String PATH_FOLDER_EXISTS_IMMEDIATE = "FolderThatExist";
    private static final String PATH_FOLDER_EXISTS_IMMEDIATE_ABS = TEST_DIRECTORY + STR_FILE_SEP + PATH_FOLDER_EXISTS_IMMEDIATE;
    private static final String PATH_FOLDER_EXISTS_RELATIVE = STRING_CURR_DIR + STR_FILE_SEP + "FolderThatExist";
    private static final String PATH_FOLDER_EXISTS_SUB_FOLDER = PATH_FOLDER_EXISTS_IMMEDIATE + STR_FILE_SEP + "SubFolder";
    private static final String PATH_FOLDER_EXISTS_SUB_FOLDER_ABS = TEST_DIRECTORY + STR_FILE_SEP + PATH_FOLDER_EXISTS_SUB_FOLDER;
    private static final String PATH_FOLDER_DOES_NOT = "FolderThatDoesNotExists";
    private static final String PATH_EMPTY = "";
    private static final String PATH_WHITESPACE = " ";
    private static final String PATH_NOT_A_DIR = String.join(String.valueOf(CHAR_FILE_SEP), new String[]{"FolderThatExist","file.txt"});


    @BeforeEach
    void init() {
        Environment.currentDirectory = TEST_DIRECTORY;
        cdApplication = new CdApplication();
    }

    @AfterAll
    static void tearDown() {
        Environment.currentDirectory = ROOT_DIRECTORY;
    }

    @Test
    void run_NullArgs_ShouldThrowError() throws CdException {
        Exception exception = assertThrows(CdException.class,
                () -> cdApplication.run(null, null, null));
        assertEquals(new CdException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void run_EmptyArgs_ShouldThrowError() {
        assertThrows(CdException.class,
                () -> cdApplication.run(EMPTY_STRING_ARRAY, null, null));
    }

    @Test
    void changeToDirectory_BlankPath_ShouldThrowError() {
        Exception exception = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(PATH_EMPTY));
        assertEquals(new CdException(ERR_NO_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void changeToDirectory_WhitespacePath_ShouldThrowError() {
        Exception exception = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(PATH_WHITESPACE));
        assertEquals(new CdException(ERR_NO_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void changeToDirectory_NonExistentPath_ShouldThrowError() {
        Exception exception = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(PATH_FOLDER_DOES_NOT));
        assertEquals(new CdException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
    }

    @Test
    void changeToDirectory_NonDirectoryPath_ShouldThrowError() {
        Exception exception = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(PATH_NOT_A_DIR));
        assertEquals(new CdException(ERR_IS_NOT_DIR).getMessage(), exception.getMessage());
    }

    @Test
    void changeToDirectory_ImmediateFolder_ShouldChangeIntoDirectory() throws CdException {
        cdApplication.changeToDirectory(PATH_FOLDER_EXISTS_IMMEDIATE);
        String changedDir = Environment.currentDirectory;
        assertEquals(PATH_FOLDER_EXISTS_IMMEDIATE_ABS, changedDir);
    }

    @Test
    void changeToDirectory_ImmediateFolderAbsolute_ShouldChangeIntoDirectory() throws CdException {
        cdApplication.changeToDirectory(PATH_FOLDER_EXISTS_IMMEDIATE_ABS);
        String changedDir = Environment.currentDirectory;
        assertEquals(PATH_FOLDER_EXISTS_IMMEDIATE_ABS, changedDir);
    }

    @Test
    void changeToDirectory_ImmediateFolderRelative_ShouldChangeIntoDirectory() throws CdException {
        cdApplication.changeToDirectory(PATH_FOLDER_EXISTS_RELATIVE);
        String changedDir = Environment.currentDirectory;
        assertEquals(PATH_FOLDER_EXISTS_IMMEDIATE_ABS, changedDir);
    }

    @Test
    void changeToDirectory_ParentFolder_ShouldChangeIntoDirectory() throws CdException {
        cdApplication.changeToDirectory(STRING_PARENT_DIR);
        String changedDir = Environment.currentDirectory;
        assertEquals(TEST_DIRECTORY_PARENT, changedDir);
    }

    @Test
    void changeToDirectory_CurrentDirectory_ShouldStayInDirectory() throws CdException {
        String initialDir = Environment.currentDirectory;
        cdApplication.changeToDirectory(STRING_CURR_DIR);
        String changedDir = Environment.currentDirectory;
        assertEquals(initialDir, changedDir);
    }

    @Test
    void changeToDirectory_FoldWithOneSubFolder_ShouldStayInDirectory() throws CdException {
        cdApplication.changeToDirectory(PATH_FOLDER_EXISTS_SUB_FOLDER);
        String changedDir = Environment.currentDirectory;
        assertEquals(PATH_FOLDER_EXISTS_SUB_FOLDER_ABS, changedDir);
    }
}
