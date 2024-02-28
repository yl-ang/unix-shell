package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import static sg.edu.nus.comp.cs4218.impl.util.TestUtils.deleteDir;

class LsApplicationTest {

    private static LsApplication lsApplication;
    private static final String NEW_LINE = System.lineSeparator();
    private static final String ROOT_PATH = Environment.currentDirectory;
    private static final String TEST_FOLDER_NAME = "tmpLsTestFolder";
    private static final String TEST_PATH = ROOT_PATH + CHAR_FILE_SEP + TEST_FOLDER_NAME + CHAR_FILE_SEP;
    private static final String FOLDER_ONE = "folder1";
    private static final String FOLDER_TWO = "folder2";
    private static final String FILE_ONE = "file1.iml";
    private static final String FILE_TWO = "file2.xml";
    private static final String FILE_THREE = "file3.txt";
    private static final String FILE_FOUR = "file4.txt";

    /*
    Folder Structure for test
    -folder1
      -folder2
        -file4.txt
      -file1.iml
      -file2.xml
    -file3.txt
    */

    @BeforeAll
    static void setUp() throws IOException {
        lsApplication = new LsApplication();
        deleteDir(new File(TEST_PATH));
        Files.createDirectories(Paths.get(TEST_PATH + FOLDER_ONE + CHAR_FILE_SEP + FOLDER_TWO));
        Files.createFile(Paths.get(TEST_PATH + FOLDER_ONE + CHAR_FILE_SEP + FILE_ONE));
        Files.createFile(Paths.get(TEST_PATH + FOLDER_ONE + CHAR_FILE_SEP + FILE_TWO));
        Files.createFile(Paths.get(TEST_PATH + FILE_THREE));
        Files.createFile(Paths.get(TEST_PATH + FOLDER_ONE + CHAR_FILE_SEP + FOLDER_TWO + CHAR_FILE_SEP + FILE_FOUR));
    }

    @BeforeEach
    void setUpEach() {
        Environment.currentDirectory = ROOT_PATH;
    }

    @AfterAll
    static void tearDown() {
        Environment.currentDirectory = ROOT_PATH;
        deleteDir(new File(TEST_PATH));
    }

    @Test
    void listFolderContent_GivenPathNoOptions_ShouldPrintAllFilesAndDirectories() throws AbstractApplicationException {
        String lsOutput = lsApplication.listFolderContent(false, false, TEST_PATH);
        String expectedOutput = TEST_FOLDER_NAME + ":" + STRING_NEWLINE + FILE_THREE + STRING_NEWLINE + FOLDER_ONE;
        assertEquals(expectedOutput, lsOutput);
    }

    @Test
    void listFolderContent_GivenAbsolutePathNoOptions_ShouldPrintAllFilesAndDirectories() throws AbstractApplicationException {
        Environment.currentDirectory = TEST_PATH;
        String lsOutput = lsApplication.listFolderContent(false, false, ".");
        String parentFolder = "./:";
        String expectedOutput = parentFolder + STRING_NEWLINE + FILE_THREE + STRING_NEWLINE + FOLDER_ONE;
        assertEquals(expectedOutput, lsOutput);
    }

    @Test
    void listFolderContent_GivenRelativePathNoOptions_ShouldPrintAllFilesAndDirectories() throws AbstractApplicationException {
        String path = "." + CHAR_FILE_SEP + TEST_FOLDER_NAME + CHAR_FILE_SEP + FOLDER_ONE + CHAR_FILE_SEP + ".."
                + CHAR_FILE_SEP + FOLDER_ONE + CHAR_FILE_SEP + ".";
        String lsOutput = lsApplication.listFolderContent(false, false, path);
        String expectedOutput = TEST_FOLDER_NAME + CHAR_FILE_SEP + FOLDER_ONE + ":" + STRING_NEWLINE + FILE_ONE +
                STRING_NEWLINE + FILE_TWO + STRING_NEWLINE + FOLDER_TWO;
        assertEquals(expectedOutput, lsOutput);
    }

    @Test
    void listFolderContent_GivenInvalidPathNoOptions_ShouldReturnInvalidPathStringError() throws AbstractApplicationException {
        String path = TEST_FOLDER_NAME + "thisFileIsWrong.xml";
        String lsOutput = lsApplication.listFolderContent(false, false, path);
        String expectedOutput = "ls: cannot access '" + TEST_FOLDER_NAME
                + "thisFileIsWrong.xml': No such file or directory";
        assertEquals(expectedOutput, lsOutput);
    }

    @Test
    void listFolderContent_GivenValidMultiplePathsNoOptions_ShouldReturnValidPathString() throws AbstractApplicationException {
        String validPath1 = TEST_FOLDER_NAME;
        String validPath2 = TEST_FOLDER_NAME + CHAR_FILE_SEP + FOLDER_ONE + CHAR_FILE_SEP + FOLDER_TWO;
        String lsOutput = lsApplication.listFolderContent(false, false, validPath1, validPath2);
        String expectedOutput = TEST_FOLDER_NAME + ":" + NEW_LINE + FILE_THREE + STRING_NEWLINE + FOLDER_ONE +
                STRING_NEWLINE + STRING_NEWLINE + TEST_FOLDER_NAME + CHAR_FILE_SEP + FOLDER_ONE + CHAR_FILE_SEP +
                FOLDER_TWO + ":" + STRING_NEWLINE + FILE_FOUR;
        assertEquals(expectedOutput, lsOutput);
    }
}