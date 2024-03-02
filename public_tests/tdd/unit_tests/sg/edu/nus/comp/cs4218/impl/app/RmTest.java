package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

public class RmTest {

    private static RmInterface rmApplication;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "tdd", "unit_tests", "rm"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);
    private static final String CREATE_TEST_FILE_PATH = TEST_DIRECTORY + STR_FILE_SEP + "example.txt";

    @BeforeEach
    void init() {
        Environment.currentDirectory = TEST_DIRECTORY;
        rmApplication = mock(RmInterface.class);
    }

    @Test
    public void remove_RemoveSingleFileNoFlag_ShouldRemoveFile() throws AbstractApplicationException, IOException {
        File file = new File(CREATE_TEST_FILE_PATH);
        boolean createFile = file.createNewFile();
        assertTrue(createFile, "Create test file to removed by 'rm' has to be created");
        rmApplication.remove(false, false, CREATE_TEST_FILE_PATH);
        assertFalse(file.exists(), "File should not exist after removal");
    }

}
