package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.app.MkdirApplication;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.exception.MkdirException.ERR_FOLDER_EXISTS;
import static sg.edu.nus.comp.cs4218.exception.MkdirException.INVALID_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

public class MkdirApplicationTest {
    private static MkdirApplication mkdirApplication;

    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String TEST_DIRECTORY = ROOT_DIRECTORY + File.separator + "test_folder";
    private static final String FOLDER_WITHOUT_PARENT_DIRECTORY = "testFolder1";
    private static final String FOLDER_WITH_PARENT_DIRECTORY = "testFolder2" + File.separator + "testSubFolder1";
    private static final String FOLDER_WITH_EXISTING_PARENT_DIRECTORY = "testFolder2" + File.separator + "testSubFolder2";
    private static final String FOLDER_CREATE_EXISTS_FOR_TEST = TEST_DIRECTORY + File.separator + "testFolder4" + File.separator + "testSubFolder3";
    private static final String[] INVALID_ARGS_ILLEGAL_FLAG = {"-l", "folder1"};
    private static final String[] VALID_ARGS_WITH_SINGLE_FOLDER = {"testFolder3"};
    private static final String[] VALID_ARGS_WITH_PARENT_FLAG = {"-p", "testFolder4/testSubFolder3"};
    private static final String[] VALID_ARGS_WITH_MULTIPLE_FOLDER_WITH_PARENT_FLAG = {"-p", "testFolder5/testSubFolder4",
            "testFolder6"};
    private static final String[] INVALID_ARGS_PARENT_NO_EXISTS = {"NonExistParent/testFolder"};
    private static final String[] INVALID_ARGS_FOLDER_EXISTS = {"testFolder1"};
    private static final String[] INVALID_ARGS_FOLDER_EXISTS_WITH_PARENT = {"testFolder4/testSubFolder3"};

    @BeforeAll
    static void setup() {
        // Setup test folder
        mkdirApplication = new MkdirApplication();
        File testDirFile = new File(TEST_DIRECTORY);
        testDirFile.mkdirs();
    }

    @BeforeEach
    void init() {
        // Clean up test folder
        Environment.currentDirectory = TEST_DIRECTORY;
        File folder = new File(TEST_DIRECTORY);
        emptyFolder(folder, false);
    }

    @AfterAll
    static void tearDown() {
        Environment.currentDirectory = ROOT_DIRECTORY;
        File folder = new File(TEST_DIRECTORY);
        emptyFolder(folder, true);
    }

    @Test
    void createFolder_WithoutParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(FOLDER_WITHOUT_PARENT_DIRECTORY);
        assertTrue(Files.exists(Paths.get(getFullPath(FOLDER_WITHOUT_PARENT_DIRECTORY))), "Folder should created");
    }

    @Test
    void createFolder_WithParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(FOLDER_WITH_PARENT_DIRECTORY);
        assertTrue(Files.exists(Paths.get(getFullPath(FOLDER_WITH_PARENT_DIRECTORY))), "Folder should created");
    }

    @Test
    void createFolder_WithExistingParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(FOLDER_WITH_EXISTING_PARENT_DIRECTORY);
        assertTrue(Files.exists(Paths.get(getFullPath(FOLDER_WITH_EXISTING_PARENT_DIRECTORY))),
                "Folder should be created");
    }

    @Test
    void run_validArgsSingleFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.run(VALID_ARGS_WITH_SINGLE_FOLDER, System.in, System.out);
        assertTrue(Files.exists(Paths.get(getFullPath(VALID_ARGS_WITH_SINGLE_FOLDER[0]))),
                "Folder should created");
    }

    @Test
    void run_validArgsWithParentFolder_ShouldCreateFolders() throws MkdirException {
        mkdirApplication.run(VALID_ARGS_WITH_PARENT_FLAG, System.in, System.out);
        assertTrue(Files.exists(Paths.get(getFullPath(VALID_ARGS_WITH_PARENT_FLAG[1]))),
                "Folder should created");
        assertDoesNotThrow(() -> mkdirApplication.run(VALID_ARGS_WITH_PARENT_FLAG, System.in, System.out));
    }

    @Test
    void run_validArgsWithMultipleFolderWithParentFlag_ShouldCreateFolders() throws MkdirException {
        mkdirApplication.run(VALID_ARGS_WITH_MULTIPLE_FOLDER_WITH_PARENT_FLAG, System.in, System.out);
        assertTrue(Files.exists(Paths.get(getFullPath(VALID_ARGS_WITH_MULTIPLE_FOLDER_WITH_PARENT_FLAG[1]))),
                "Folder should created");
        assertTrue(Files.exists(Paths.get(getFullPath(VALID_ARGS_WITH_MULTIPLE_FOLDER_WITH_PARENT_FLAG[2]))),
                "Folder should created");
    }

    @Test
    void run_NullArgs_ShouldThrowError() throws MkdirException {
        Exception exception = assertThrows(MkdirException.class,
                () -> mkdirApplication.run(null, System.in, System.out));
        assertEquals(new MkdirException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void run_IllegalArgs_ShouldThrowError() throws MkdirException {
        assertThrows(MkdirException.class,
                () -> mkdirApplication.run(INVALID_ARGS_ILLEGAL_FLAG, System.in, System.out));
    }

    @Test
    void run_invalidArgsParentNoExists_ShowThrowError() throws MkdirException {

        Exception exception = assertThrows(MkdirException.class,
                () -> mkdirApplication.run(INVALID_ARGS_PARENT_NO_EXISTS, System.in, System.out));
        assertEquals(new MkdirException(INVALID_DIR, INVALID_ARGS_PARENT_NO_EXISTS[0]).getMessage(), exception.getMessage());
    }

    @Test
    void run_invalidArgsFolderExists_ShowThrowError() throws MkdirException {
        File file = new File(TEST_DIRECTORY + File.separator + FOLDER_WITHOUT_PARENT_DIRECTORY);
        file.mkdirs();
        Exception exception = assertThrows(MkdirException.class,
                () -> mkdirApplication.run(INVALID_ARGS_FOLDER_EXISTS, System.in, System.out));
        assertEquals(new MkdirException(ERR_FOLDER_EXISTS, INVALID_ARGS_FOLDER_EXISTS[0]).getMessage(), exception.getMessage());
    }

    @Test
    void run_invalidArgsFolderExistsWithParent_ShowThrowError() throws MkdirException {
        File file = new File(FOLDER_CREATE_EXISTS_FOR_TEST);
        file.mkdirs();
        Exception exception = assertThrows(MkdirException.class,
                () -> mkdirApplication.run(INVALID_ARGS_FOLDER_EXISTS_WITH_PARENT, System.in, System.out));
        assertEquals(new MkdirException(ERR_FOLDER_EXISTS, INVALID_ARGS_FOLDER_EXISTS_WITH_PARENT[0]).getMessage(),
                exception.getMessage());
    }

    private static String getFullPath(String path) {
        return TEST_DIRECTORY + File.separator + path;
    }
    private static void emptyFolder(File folder, boolean deleteFolder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        emptyFolder(file, true);
                    } else {
                        file.delete();
                    }
                }
            }
            if (deleteFolder) {
                folder.delete();
            }
        }
    }
}
