package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

public class RmTest {

    private static RmInterface rmApplication;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "tdd", "unit_tests", "rm"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);
    private static final String CREATE_TEST_FILE_PATH_1 = TEST_DIRECTORY + STR_FILE_SEP + "file1.txt";
    private static final String CREATE_TEST_FILE_PATH_2 = TEST_DIRECTORY + STR_FILE_SEP + "file2.txt";
    private static final String CREATE_EMPTY_FOLDER = TEST_DIRECTORY + STR_FILE_SEP + "emptyFolder";
    private static final String CREATE_NON_EMPTY_FOLDER = TEST_DIRECTORY + STR_FILE_SEP + "nonEmptyFolder";
    private static final String CREATE_TEST_FILE_PATH_3 = CREATE_NON_EMPTY_FOLDER + STR_FILE_SEP + "file3.txt";
    private static final String CREATE_NON_EMPTY_SUBFOLDER = CREATE_NON_EMPTY_FOLDER + STR_FILE_SEP + "subfolder";
    private static final String CREATE_TEST_FILE_PATH_4 = CREATE_NON_EMPTY_SUBFOLDER + STR_FILE_SEP + "file4.txt";

    @BeforeEach
    void init() {
        Environment.currentDirectory = TEST_DIRECTORY;
        rmApplication = mock(RmInterface.class);
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.walk(Paths.get(TEST_DIRECTORY))
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    @Test
    public void remove_RemoveSingleFileNoFlag_ShouldRemoveFile() throws AbstractApplicationException, IOException {
        File file = new File(CREATE_TEST_FILE_PATH_1);
        boolean createFile = file.createNewFile();
        assertTrue(createFile, "Create test file to removed by 'rm' has to be created"); //NOPMD - suppressed AvoidDuplicateLiterals - Clarity
        rmApplication.remove(false, false, CREATE_TEST_FILE_PATH_1);
        assertFalse(file.exists(), "File should not exist after removal");
    }

    @Test
    public void remove_RemoveMultipleFilesNoFlag_ShouldRemoveFiles() throws AbstractApplicationException, IOException {
        File file1 = new File(CREATE_TEST_FILE_PATH_1);
        File file2 = new File(CREATE_TEST_FILE_PATH_2);
        assertTrue(file1.createNewFile(), "Create test file 1 to be removed by 'rm'");
        assertTrue(file2.createNewFile(), "Create test file 2 to be removed by 'rm'");
        rmApplication.remove(false, false, CREATE_TEST_FILE_PATH_1, CREATE_TEST_FILE_PATH_2);
        assertFalse(file1.exists(), "File 1 should not exist after removal");
        assertFalse(file2.exists(), "File 2 should not exist after removal");
    }

    @Test
    public void remove_RemoveEmptyFolderNoFlag_ThrowsExpcetion() throws AbstractApplicationException, IOException {
        File emptyFolder = new File(CREATE_EMPTY_FOLDER);
        assertTrue(emptyFolder.mkdir(), "Create empty test folder to be removed by 'rm'");
        // Try to remove the empty folder without the '-d' flag, should throw an exception
        assertThrows(AbstractApplicationException.class, () -> rmApplication.remove(false, false, CREATE_EMPTY_FOLDER));
    }
    @Test
    public void remove_RemoveEmptyFolderEmptyFlag_ShouldRemoveFolder() throws AbstractApplicationException, IOException {
        File emptyFolder = new File(CREATE_EMPTY_FOLDER);
        assertTrue(emptyFolder.mkdir(), "Create empty test folder to be removed by 'rm'");
        // Try to remove the empty folder without the '-d' flag, should throw an exception
        rmApplication.remove(true, false, CREATE_EMPTY_FOLDER);
        // Assert that the folder does not exist after removal
        assertFalse(emptyFolder.exists(), "Empty folder should not exist after removal");
    }

    @Test
    public void remove_RemoveNonEmptyFolderNoFlag_ThrowsExpcetion() throws AbstractApplicationException, IOException {
        File nonEmptyFolder = new File(CREATE_NON_EMPTY_FOLDER);
        assertTrue(nonEmptyFolder.mkdir(), "Create test folder to be removed by 'rm'"); //NOPMD - suppressed AvoidDuplicateLiterals - Clarity
        File file = new File(CREATE_TEST_FILE_PATH_3);
        boolean createFile = file.createNewFile();
        assertTrue(createFile, "Create test file to removed by 'rm' has to be created");
        assertThrows(AbstractApplicationException.class, () -> rmApplication.remove(false, false, CREATE_NON_EMPTY_FOLDER));
    }
    @Test
    public void remove_RemoveNonEmptyFolderEmptyFlag_ShouldRemoveFolder() throws AbstractApplicationException, IOException {
        File nonEmptyFolder = new File(CREATE_NON_EMPTY_FOLDER);
        assertTrue(nonEmptyFolder.mkdir(), "Create test folder to be removed by 'rm'");
        File file = new File(CREATE_TEST_FILE_PATH_3);
        boolean createFile = file.createNewFile();
        assertTrue(createFile, "Create test file to removed by 'rm' has to be created");
        assertThrows(AbstractApplicationException.class, () -> rmApplication.remove(true, false, CREATE_NON_EMPTY_FOLDER));
    }

    @Test
    public void remove_RemoveFolderRecurseFlag_ShouldRemoveFoldersAndFile() throws AbstractApplicationException, IOException {
        File nonEmptyFolder = new File(CREATE_NON_EMPTY_FOLDER);
        assertTrue(nonEmptyFolder.mkdir(), "Create test folder to be removed by 'rm'");
        File file = new File(CREATE_TEST_FILE_PATH_3);
        boolean createFile = file.createNewFile();
        assertTrue(createFile, "Create test file to removed by 'rm' has to be created");
        rmApplication.remove(false, true, CREATE_NON_EMPTY_FOLDER);
        assertFalse(nonEmptyFolder.exists(), "Folder should be delete");
        assertFalse(file.exists(), "File should be delete");
    }
    @Test
    public void remove_RemoveFolderRecurseFlag_ShouldRemoveFoldersAndFileRecursively() throws AbstractApplicationException, IOException {
        File nonEmptyFolder1 = new File(CREATE_NON_EMPTY_FOLDER);
        assertTrue(nonEmptyFolder1.mkdir(), "Create test folder to be removed by 'rm'");

        File file1 = new File(CREATE_TEST_FILE_PATH_3);
        boolean createFile1 = file1.createNewFile();
        assertTrue(createFile1, "Create test file to removed by 'rm' has to be created");

        File nonEmptyFolder2 = new File(CREATE_NON_EMPTY_SUBFOLDER);
        assertTrue(nonEmptyFolder2.mkdir(), "Create empty test folder to be removed by 'rm'");

        File file2 = new File(CREATE_TEST_FILE_PATH_4);
        boolean createFile2 = file2.createNewFile();
        assertTrue(createFile2, "Create test file to removed by 'rm' has to be created");

        rmApplication.remove(false, true, CREATE_NON_EMPTY_FOLDER);
        assertFalse(nonEmptyFolder1.exists(), "Folder should be delete");
        assertFalse(file1.exists(), "File should be delete");
        assertFalse(nonEmptyFolder2.exists(), "Folder should be delete");
        assertFalse(file2.exists(), "File should be delete");
    }
}
