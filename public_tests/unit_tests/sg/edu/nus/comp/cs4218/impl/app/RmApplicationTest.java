package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class RmApplicationTest {

    private RmApplication rmApplication;

    @TempDir
    public Path tempDir;

    private File file;
    private File directory;

    @BeforeEach
    void setUp() throws IOException {
        rmApplication = new RmApplication();
        file = new File(tempDir.toFile(), "file.txt");
        file.createNewFile();
        directory = new File(tempDir.toFile(), "directory");
        directory.mkdir();
    }

    @AfterEach
    void tearDown() {
        // Clean up files and directories created during tests
        file.delete();
        directory.delete();
    }

    @Test
    void run_NullArgs_ExceptionThrown() {
        assertThrows(AbstractApplicationException.class, () -> rmApplication.run(null, System.in, System.out));
    }

    @Test
    void run_NullStdout_ExceptionThrown() {
        assertThrows(AbstractApplicationException.class, () -> rmApplication.run(new String[]{"file.txt"}, System.in, null));
    }

    @Test
    void remove_NullArgs_ExceptionThrown() {
        assertThrows(AbstractApplicationException.class, () -> rmApplication.remove(null, false, "file.txt"));
    }

    @Test
    void remove_NonExistingFile_ExceptionThrown() {
        assertThrows(RmException.class, () -> rmApplication.remove(false, false, "non_existing_file.txt"));
    }

    @Test
    void remove_DeleteSingleFile_Success() throws AbstractApplicationException {
        rmApplication.remove(false, false, file.getPath());
    }

    @Test
    void remove_DeleteDirectoryWithRecursive_Success() throws AbstractApplicationException {
        rmApplication.remove(false, true, directory.getPath());
    }
}
