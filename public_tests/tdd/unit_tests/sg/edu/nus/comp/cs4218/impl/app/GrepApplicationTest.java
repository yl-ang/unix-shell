package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;

public class GrepApplicationTest {
    private GrepApplication grepApplication;
    private Path path;
    private static final String TEST_FILE = "file.txt";
    private static final String TEST_FILE2 = "file2.txt";
    private static final String NE_FILE = "nonExistent.txt";
    private static final String TEST_FOLDER = "testFolder";
    private static final String INVALID_PATTERN = "[[]]";
    private static final String VALID_PATTERN = "hello";
    private static final String HELLO_UPPERCASE = "Hello world";
    private static final String HELLO_LOWERCASE = "hello world";
    private static final String HELLO_INSENSITIVE = "heLlO WorLD";
    private static final String[] LINES1 = {HELLO_UPPERCASE, HELLO_LOWERCASE, HELLO_INSENSITIVE};
    private static final String[] LINES2 = {"hello", HELLO_LOWERCASE, "5"};

    @BeforeEach
    public void setUp() throws Exception {
        grepApplication = new GrepApplication();
        path = Paths.get(TEST_FILE);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    @AfterEach
    public void tearDown() {
        grepApplication = null;
        path = Path.of(TEST_FILE);
        deletePath(path);
    }

    private static void deletePath(Path path) {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void grepFromStdIn_InvalidPattern_ShouldThrowGrepException() {
        GrepException grepException = assertThrows(GrepException.class, () -> {
            grepApplication.grepFromStdin(INVALID_PATTERN, false, false, false, System.in);
        });

        assertEquals(new GrepException(ERR_INVALID_REGEX).getMessage(), grepException.getMessage());
    }

    @Test
    public void grepFromFiles_NullPattern_ShouldThrowGrepException() {
        GrepException grepException = assertThrows(GrepException.class, () -> {
            grepApplication.grepFromFiles(null, false, false, false, TEST_FILE);
        });

        assertEquals(new GrepException(GrepApplication.NULL_POINTER).getMessage(), grepException.getMessage());
    }

    @Test
    public void grepFromStdIn_NoFlagsValidInputUppercase_ShouldReturnEmpty() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_UPPERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, false, false, false, inputStream);
            assertEquals("", results);
        });


    }
}
