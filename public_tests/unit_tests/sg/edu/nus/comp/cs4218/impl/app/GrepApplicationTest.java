package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;

public class GrepApplicationTest {
    private GrepApplication grepApplication;
    private Path path;
    private final InputStream inputStream = new ByteArrayInputStream(COMBINED_INPUT.getBytes());
    private static final String TEST_FILE = "file.txt";
    private static final String INVALID_PATTERN = "[[]]";
    private static final String VALID_PATTERN = "hello";
    private static final String HELLO_UPPERCASE = "Hello world";
    private static final String HELLO_LOWERCASE = "hello world";
    private static final String HELLO_LOWERCASE_LONG = "hello world my name is cs4218";
    private static final String HELLO_INSENSITIVE = "heLlO WorLD";
    private static final String[] LINES1 = {HELLO_UPPERCASE, HELLO_LOWERCASE, HELLO_INSENSITIVE};
    private static final String[] LINES2 = {"hello", HELLO_LOWERCASE, "5"};
    private static final String COMBINED_INPUT = HELLO_LOWERCASE + STRING_NEWLINE +
            HELLO_UPPERCASE + STRING_NEWLINE +
            HELLO_INSENSITIVE + STRING_NEWLINE;
    private OutputStream stdout;

    @BeforeEach
    public void setUp() throws Exception {
        grepApplication = new GrepApplication();
        stdout = new ByteArrayOutputStream();
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
    public void grepFromStdIn_NoOptionsValidInputLowercase_ShouldReturnEmpty() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_LOWERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, false, false, false, inputStream);
            assertEquals(HELLO_LOWERCASE + STRING_NEWLINE, results);
        });
    }

    @Test
    public void grepFromStdIn_InvalidPattern_ShouldThrowGrepException() {
        GrepException grepException = assertThrows(GrepException.class, () -> {
            grepApplication.grepFromStdin(INVALID_PATTERN, false, false, false, System.in);
        });

        assertEquals(new GrepException(ERR_INVALID_REGEX).getMessage(), grepException.getMessage());
    }

    @Test
    public void grepFromStdIn_NoOptionsValidInputUppercase_ShouldReturnEmpty() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_UPPERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, false, false, false, inputStream);
            assertEquals("", results);
        });
    }

    @Test
    public void grepFromStdIn_NotCaseInsensitiveValidInputUppercase_ShouldReturnEmpty() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_UPPERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, false, false, false, inputStream);
            assertEquals(HELLO_LOWERCASE + STRING_NEWLINE, results);
        });
    }

    @Test
    public void grepFromStdIn_CaseInsensitiveCountLines_ShouldReturnEmpty() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_UPPERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, true, true, false, inputStream);
            assertEquals("1" + STRING_NEWLINE, results);
        });
    }

    @Test
    public void grepFromFiles_NullPattern_ShouldThrowGrepException() {
        GrepException grepException = assertThrows(GrepException.class, () -> {
            grepApplication.grepFromFiles(null, false, false, false, TEST_FILE);
        });

        assertEquals(new GrepException(GrepApplication.NULL_POINTER).getMessage(), grepException.getMessage());
    }

    @Test
    public void grepFromFiles_IsPrefixFileName_ShouldReturnLinesResults() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path);
        writer.write(HELLO_LOWERCASE);
        writer.close();

        String results  = grepApplication.grepFromFiles(VALID_PATTERN, false, false, true, TEST_FILE);

        assertEquals(TEST_FILE + ": " + HELLO_LOWERCASE_LONG + STRING_NEWLINE, results);
    }

//    @Test
//    public void grepFromFiles_IsPrefixFileName_ShouldReturnLinesResults() throws Exception {
//        BufferedWriter writer = Files.newBufferedWriter(path);
//        writer.write(HELLO_LOWERCASE);
//        writer.close();
//
//        String results  = grepApplication.grepFromFiles(VALID_PATTERN, false, false, true, TEST_FILE);
//
//        assertEquals(TEST_FILE + ": " + HELLO_LOWERCASE + STRING_NEWLINE, results);
//    }

    @Test
    public void grepFromFiles_IsPrefixFileNameCaseInsensitive_ShouldReturnLinesResults() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path);
        writer.write(HELLO_LOWERCASE_LONG);
        writer.close();

        String results  = grepApplication.grepFromFiles(HELLO_UPPERCASE, true, false, true, TEST_FILE);

        assertEquals(TEST_FILE + ": " + HELLO_LOWERCASE_LONG + STRING_NEWLINE, results);
    }

    @Test
    public void grepFromFiles_CaseInsensitive_ShouldReturnLinesResults() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path);
        writer.write(HELLO_UPPERCASE);
        writer.close();

        String results  = grepApplication.grepFromFiles(VALID_PATTERN, true, false, true, TEST_FILE);

        assertEquals(TEST_FILE + ": " + HELLO_UPPERCASE + STRING_NEWLINE, results);
    }

    @Test
    public void grepFromFiles_CountLinesUppercase_ShouldReturnZero() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path);
        writer.write(HELLO_UPPERCASE);
        writer.close();

        String results  = grepApplication.grepFromFiles(VALID_PATTERN, false, true, false, TEST_FILE);

        assertEquals(0 + STRING_NEWLINE, results);
    }

    @Test
    void run_nullStdin_shouldThrowGrepException() {
        assertThrows(GrepException.class, () -> grepApplication.run(new String[]{"hello world"}, null, System.out));
    }

    @Test
    void run_validGrepFromStdin_shouldReturnGrepOutput() throws Exception {
        grepApplication.run(new String[]{"hello world", "-"}, inputStream, stdout);
        assertEquals(HELLO_LOWERCASE + STRING_NEWLINE, stdout.toString());
    }

}
