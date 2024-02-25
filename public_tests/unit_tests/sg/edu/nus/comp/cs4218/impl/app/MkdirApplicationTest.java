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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MkdirApplicationTest {
    private static MkdirApplication mkdirApplication;

    private static final String rootDirectory = Environment.currentDirectory;
    private static final String testDirectory = rootDirectory + File.separator + "test_folder";
    private static final String folderWithoutParentDirectory = "testFolder1";
    private static final String folderWithParentDirectory = "testFolder2" + File.separator + "testSubFolder1";
    private static final String folderWithExistingParentDirectory = "testFolder2" + File.separator + "testSubFolder2";
    private static final String folderWithIllegalName = "\0";

    @BeforeAll
    static void setup() {
        mkdirApplication = new MkdirApplication();
        File testDirFile = new File(testDirectory);
        testDirFile.mkdirs();
    }

    @BeforeEach()
    void init() {
        Environment.currentDirectory = testDirectory;
    }

    @AfterAll()
    static void tearDown() {
        Environment.currentDirectory = rootDirectory;
        File folder = new File(testDirectory);
        deleteFolder(folder);
    }

    @Test()
    void createFolder_WithoutParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(folderWithoutParentDirectory);
        assertTrue(Files.exists(Paths.get(getFullPath(folderWithoutParentDirectory))), "Folder should created");
    }

    @Test()
    void createFolder_WithParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(folderWithParentDirectory);
        assertTrue(Files.exists(Paths.get(getFullPath(folderWithParentDirectory))), "Folder should created");
    }

    @Test()
    void createFolder_WithExistingParentFolder_ShouldCreateFolder() throws MkdirException {
        mkdirApplication.createFolder(folderWithExistingParentDirectory);
        assertTrue(Files.exists(Paths.get(getFullPath(folderWithExistingParentDirectory))), "Folder should be created");
    }

    @Test()
    void createFolder_FolderWithIllegalName_ShouldThrowError() throws MkdirException {
        assertThrows(MkdirException.class, () -> mkdirApplication.createFolder(folderWithIllegalName));
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
