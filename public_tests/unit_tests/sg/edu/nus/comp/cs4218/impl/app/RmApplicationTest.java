package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RmApplicationTest {

    private RmApplication rmApplication;

    @TempDir
    public Path tempDir;

    private File file;
    private File directory;
    private File emptyDirectory;

    @BeforeEach
    void setUp() throws IOException {
        rmApplication = new RmApplication();

        file = new File(tempDir.toFile(), "file.txt");
        file.createNewFile();

        directory = new File(tempDir.toFile(), "directory");
        directory.mkdir();

        emptyDirectory = new File(tempDir.toFile(), "emptyDirectory");
        emptyDirectory.mkdir();
    }

    @AfterEach
    void tearDown() {
        file.delete();
        directory.delete();
        emptyDirectory.delete();
    }

    @Test
    void run_NullArgs_ExceptionThrown() {
        assertThrows(AbstractApplicationException.class, () -> rmApplication.run(null, System.in, System.out));
    }

    @Test
    void run_NullStdout_ExceptionThrown() {
        assertThrows(AbstractApplicationException.class, () -> rmApplication.run(new String[]{"file.txt"},
                System.in, null));
    }

    @Test
    void remove_NullArgs_ExceptionThrown() {
        assertThrows(AbstractApplicationException.class, () -> rmApplication.remove(null, false,
                "file.txt"));
    }

    @Test
    void remove_NonExistingFile_ExceptionThrown() {
        assertThrows(RmException.class, () -> rmApplication.remove(false, false,
                "non_existing_file.txt"));
    }

    @Test
    void remove_DeleteSingleFile_Success() throws AbstractApplicationException {
        rmApplication.remove(false, false, file.getPath());
    }

    @Test
    void remove_DeleteDirectoryWithRecursive_Success() throws AbstractApplicationException {
        rmApplication.remove(false, true, directory.getPath());
    }

    @Test
    void remove_EmptyDirectoryWithEmptyOption_Success() throws AbstractApplicationException {
        // GIVEN
        rmApplication.remove(true, false, emptyDirectory.getPath());

        // WHEN
        assert !emptyDirectory.exists();
    }

    @Test
    void run_CallsRemove_Success() throws AbstractApplicationException {

        // GIVEN
        String[] args = {file.getAbsolutePath()};
        InputStream inputStream = System.in;
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            // WHEN
            rmApplication.run(args, inputStream, outputStream);
        } catch (AbstractApplicationException e) {
            e.printStackTrace();
        }

        // THEN
        assert !file.exists();
    }

    @Test
    void remove_RecursivelyDeleteDirectory_Success() throws AbstractApplicationException {
        File subDir = new File(directory, "subDir");
        File file1 = new File(directory, "file1.txt");
        File file2 = new File(subDir, "file2.txt");

        subDir.mkdir();
        try {
            file1.createNewFile();
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // WHEN
        rmApplication.remove(true, true, directory.getPath());

        // THEN & VERIFY
        assertFalse(directory.exists());
        assertFalse(subDir.exists());
        assertFalse(file1.exists());
        assertFalse(file2.exists());
    }
}
