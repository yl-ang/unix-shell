package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MvApplicationTest {
    @InjectMocks
    private MvInterface mvApplication;

    private final Path tempDir = Paths.get("tempDir");
    private final Path sourceFile = tempDir.resolve("source.txt");
    private final Path targetFile = tempDir.resolve("target.txt");

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        Files.createDirectory(tempDir);
        Files.createFile(sourceFile);
        Files.createFile(targetFile);
    }

    @Test
    void mvSrcFileToDestFile_Success() throws AbstractApplicationException, IOException {
        // GIVEN
        String content = "Hello World!";
        Files.write(sourceFile, content.getBytes());

        // WHEN
        mvApplication.mvSrcFileToDestFile(true, sourceFile.toString(), targetFile.toString());

        // THEN
        assertTrue(Files.exists(targetFile));
        assertFalse(Files.exists(sourceFile));
        assertEquals(content, Files.readString(targetFile));
    }

    @Test
    void mvSrcFileToDestFile_Overwrite_Success() throws AbstractApplicationException, IOException {
        // GIVEN
        String content = "Hello World!";
        Files.write(sourceFile, content.getBytes());
        Files.write(targetFile, "Overwrite Message".getBytes());

        // WHEN
        mvApplication.mvSrcFileToDestFile(true, sourceFile.toString(), targetFile.toString());

        // THEN
        assertTrue(Files.exists(targetFile));
        assertFalse(Files.exists(sourceFile));
        assertEquals(content, Files.readString(targetFile));
    }

    @Test
    void mvSrcFileToDestFile_NoOverwrite_Success() throws AbstractApplicationException, IOException {
        // GIVEN
        String content = "Hello, World!";
        Files.write(sourceFile, content.getBytes());
        Files.write(targetFile, "Never Overwrite Message".getBytes());

        // WHEN
        assertThrows(IOException.class, () -> mvApplication.mvSrcFileToDestFile(false, sourceFile.toString(), targetFile.toString()));

        // THEN
        assertTrue(Files.exists(targetFile));
        assertTrue(Files.exists(sourceFile));
        assertEquals("Never Overwrite Message", Files.readString(targetFile));
    }

    @Test
    void mvFilesToFolder_Success() throws AbstractApplicationException, IOException {
        // GIVEN
        Path sourceDir = tempDir.resolve("sourceDir");
        Path targetDir = tempDir.resolve("targetDir");
        Files.createDirectory(sourceDir);
        Files.createDirectory(targetDir);
        Files.createFile(sourceDir.resolve("file1.txt"));
        Files.createFile(sourceDir.resolve("file2.txt"));

        // WHEN
        mvApplication.mvFilesToFolder(true, targetDir.toString(), "file1.txt", "file2.txt");

        // THEN
        assertFalse(Files.exists(sourceDir));
        assertTrue(Files.exists(targetDir.resolve("file1.txt")));
        assertTrue(Files.exists(targetDir.resolve("file2.txt")));
    }
}
