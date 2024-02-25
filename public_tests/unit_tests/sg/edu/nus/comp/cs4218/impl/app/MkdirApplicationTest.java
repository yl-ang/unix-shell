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

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.exception.MkdirException.ERR_FOLDER_EXISTS;
import static sg.edu.nus.comp.cs4218.exception.MkdirException.INVALID_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

public class MkdirApplicationTest {
    private static MkdirApplication mkdirApplication;

    private static final String rootDirectory = Environment.currentDirectory;
    private static final String testDirectory = rootDirectory + File.separator + "test_folder";
    private static final String folderWithoutParentDirectory = "testFolder1";
    private static final String folderWithParentDirectory = "testFolder2" + File.separator + "testSubFolder1";
    private static final String folderWithExistingParentDirectory = "testFolder2" + File.separator + "testSubFolder2";
    private static final String[] invalidArgsIllegalFlag = {"-l", "folder1"};
    private static final String[] validArgsWithSingleFolder = {"testFolder3"};
    private static final String[] validArgsWithParentFlag = {"-p", "testFolder4/testSubFolder3"};
    private static final String[] validArgsWithMultipleFolderWithParentFlag = {"-p", "testFolder5/testSubFolder4",
            "testFolder6"};
    private static final String[] invalidArgsParentNoExists = {"NonExistParent/testFolder"};
    private static final String[] invalidArgsFolderExists = {"testFolder1"};
    private static final String[] invalidArgsFolderExistsWithParent = {"testFolder4/testSubFolder3"};

    @BeforeAll
    static void setup() {
        mkdirApplication = new MkdirApplication();
        File testDirFile = new File(testDirectory);
        testDirFile.mkdirs();
    }

    @BeforeEach
    void init() {
        Environment.currentDirectory = testDirectory;
    }

    @AfterAll
    static void tearDown() {
        Environment.currentDirectory = rootDirectory;
        File folder = new File(testDirectory);
        deleteFolder(folder);
    }

    @Test
    void createFolder_WithoutParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(folderWithoutParentDirectory);
        assertTrue(Files.exists(Paths.get(getFullPath(folderWithoutParentDirectory))), "Folder should created");
    }

    @Test
    void createFolder_WithParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(folderWithParentDirectory);
        assertTrue(Files.exists(Paths.get(getFullPath(folderWithParentDirectory))), "Folder should created");
    }

    @Test
    void createFolder_WithExistingParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(folderWithExistingParentDirectory);
        assertTrue(Files.exists(Paths.get(getFullPath(folderWithExistingParentDirectory))),
                "Folder should be created");
    }

    @Test
    void run_validArgsSingleFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.run(validArgsWithSingleFolder, System.in, System.out);
        assertTrue(Files.exists(Paths.get(getFullPath(validArgsWithSingleFolder[0]))),
                "Folder should created");
    }

    @Test
    void run_validArgsWithParentFolder_ShouldCreateFolders() throws MkdirException {
        mkdirApplication.run(validArgsWithParentFlag, System.in, System.out);
        assertTrue(Files.exists(Paths.get(getFullPath(validArgsWithParentFlag[1]))),
                "Folder should created");
        assertDoesNotThrow(() -> mkdirApplication.run(validArgsWithParentFlag, System.in, System.out));
    }

    @Test
    void run_validArgsWithMultipleFolderWithParentFlag_ShouldCreateFolders() throws MkdirException {
        mkdirApplication.run(validArgsWithMultipleFolderWithParentFlag, System.in, System.out);
        assertTrue(Files.exists(Paths.get(getFullPath(validArgsWithMultipleFolderWithParentFlag[1]))),
                "Folder should created");
        assertTrue(Files.exists(Paths.get(getFullPath(validArgsWithMultipleFolderWithParentFlag[2]))),
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
                () -> mkdirApplication.run(invalidArgsIllegalFlag, System.in, System.out));
    }

    @Test
    void run_invalidArgsParentNoExists_ShowThrowError() throws MkdirException {
        Exception exception = assertThrows(MkdirException.class,
                () -> mkdirApplication.run(invalidArgsParentNoExists, System.in, System.out));
        assertEquals(new MkdirException(INVALID_DIR, invalidArgsParentNoExists[0]).getMessage(), exception.getMessage());
    }

    @Test
    void run_invalidArgsFolderExists_ShowThrowError() throws MkdirException {
        Exception exception = assertThrows(MkdirException.class,
                () -> mkdirApplication.run(invalidArgsFolderExists, System.in, System.out));
        assertEquals(new MkdirException(ERR_FOLDER_EXISTS, invalidArgsFolderExists[0]).getMessage(), exception.getMessage());
    }

    @Test
    void run_invalidArgsFolderExistsWithParent_ShowThrowError() throws MkdirException {
        Exception exception = assertThrows(MkdirException.class,
                () -> mkdirApplication.run(invalidArgsFolderExistsWithParent, System.in, System.out));
        assertEquals(new MkdirException(ERR_FOLDER_EXISTS, invalidArgsFolderExistsWithParent[0]).getMessage(),
                exception.getMessage());
    }

    private static String getFullPath(String path) {
        return testDirectory + File.separator + path;
    }
    private static void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }
    }

}
