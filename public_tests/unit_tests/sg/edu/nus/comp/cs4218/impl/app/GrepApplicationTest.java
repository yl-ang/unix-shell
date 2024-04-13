package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;

import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;

@SuppressWarnings("PMD.LongVariable") // Testing Purpose for clarity
public class GrepApplicationTest {
    private GrepApplication grepApplication;
    private Path path;
    private final InputStream inputStream = new ByteArrayInputStream(COMBINED_INPUT.getBytes());
    private static final String TEST_FILE = "file.txt";
    private static final String INVALID_PATTERN = "[[]]";
    private static final String VALID_PATTERN = "hello";
    private static final String HELLO_UPPERCASE = "Hello world";
    private static final String HELLO_UPPERCASE_LONG = "Hello world I am CS4218";
    private static final String HELLO_LOWERCASE = "hello world";
    private static final String HELLO_LOWERCASE_LONG = "hello world I am CS4218";
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
    public void grepFromFiles_FileNotFound_ShouldReturnErrorMessage() throws Exception {
        String nonExistentFile = "nonexistent.txt";
        String expectedErrorMessage = "grep: " + Environment.currentDirectory + CHAR_FILE_SEP + nonExistentFile + ": " + ERR_FILE_NOT_FOUND;

        String results = grepApplication.grepFromFiles(VALID_PATTERN, false, false, false, nonExistentFile);

        assertEquals(expectedErrorMessage + STRING_NEWLINE, results);
    }

    @Test
    public void grepFromFiles_IsPrefixFileName_ShouldReturnLinesResults() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path); //NOPMD - suppressed CloseResource - Already Close
        writer.write(HELLO_LOWERCASE_LONG);
        writer.close();

        String results  = grepApplication.grepFromFiles(HELLO_LOWERCASE, false, false, true, TEST_FILE);

        assertEquals(TEST_FILE + ":" + HELLO_LOWERCASE_LONG + STRING_NEWLINE, results);
    }

    @Test
    public void grepFromFiles_IsPrefixFileNameCaseInsensitive_ShouldReturnLinesResults() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path); //NOPMD - suppressed CloseResource - Already Close
        writer.write(HELLO_UPPERCASE_LONG);
        writer.close();

        String results  = grepApplication.grepFromFiles(HELLO_LOWERCASE, true, false, true, TEST_FILE);

        assertEquals(TEST_FILE + ":" + HELLO_UPPERCASE_LONG + STRING_NEWLINE, results);
    }

    @Test
    public void grepFromFiles_CaseInsensitive_ShouldReturnLinesResultsWithoutFileName() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path); //NOPMD - suppressed CloseResource - Already Close
        writer.write(HELLO_UPPERCASE);
        writer.close();

        String results  = grepApplication.grepFromFiles(HELLO_LOWERCASE, true, false, false, TEST_FILE);

        assertEquals(HELLO_UPPERCASE + STRING_NEWLINE, results);
    }

    @Test
    public void grepFromFiles_CountLinesCaseInsensitive_ShouldReturnZero() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(path); //NOPMD - suppressed CloseResource - Already Close
        writer.write(COMBINED_INPUT);
        writer.close();

        String results  = grepApplication.grepFromFiles(VALID_PATTERN, true, true, false, TEST_FILE);

        assertEquals(3 + STRING_NEWLINE, results);
    }

    @Test
    public void grepFromFiles_MultipleFiles_ShouldReturnCombinedResults() throws Exception {
        String[] files = {"file1.txt", "file2.txt"};
        String[] lines1 = {"line1", "line2"};
        String[] lines2 = {"line2", "line3"};
        BufferedWriter writer1 = Files.newBufferedWriter(Path.of(files[0])); //NOPMD - suppressed CloseResource - Already Close
        BufferedWriter writer2 = Files.newBufferedWriter(Path.of(files[1])); //NOPMD - suppressed CloseResource - Already Close
        for (String line : lines1) {
            writer1.write(line + STRING_NEWLINE);
        }
        for (String line : lines2) {
            writer2.write(line + STRING_NEWLINE);
        }
        writer1.close();
        writer2.close();

        String results = grepApplication.grepFromFiles("line2", false, false, false, files);

        assertEquals("file1.txt:line2" + STRING_NEWLINE + "file2.txt:line2" + STRING_NEWLINE, results);

        for (String file : files) {
            Files.deleteIfExists(Path.of(file));
        }
    }

    @Test
    public void grepFromStdIn_EmptyInputStream_ShouldReturnEmpty() throws Exception {
        InputStream emptyInputStream = new ByteArrayInputStream("".getBytes());
        String results = grepApplication.grepFromStdin(VALID_PATTERN, false, false, false, emptyInputStream);
        assertEquals("", results);
    }

    @Test
    public void grepFromStdIn_PatternNotFound_ShouldReturnEmpty() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_LOWERCASE_LONG.getBytes());
        String results = grepApplication.grepFromStdin("xyz", false, false, false, inputStream);
        assertEquals("", results);
    }

    @Test
    public void grepFromStdIn_AllFlagsFalseValidInputLowercase_ShouldReturnLowercase() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_LOWERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, false, false, false, inputStream);
            assertEquals(HELLO_LOWERCASE + STRING_NEWLINE, results);
        });
    }

    @Test
    public void grepFromStdIn_AllFlagsFalseValidInputUppercase_ShouldReturnEmpty() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_UPPERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, false, false, false, inputStream);
            assertEquals("", results);
        });
    }

    @Test
    public void grepFromStdIn_CaseInsensitiveValidInputUppercase_ShouldReturnUppercase() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_UPPERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(HELLO_LOWERCASE, true, false, false, inputStream);
            assertEquals(HELLO_UPPERCASE + STRING_NEWLINE, results);
        });
    }

    @Test
    public void grepFromStdIn_AllFlagsTrue_ShouldReturnStdIn() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(HELLO_LOWERCASE.getBytes());
        assertDoesNotThrow(() -> {
            String results = grepApplication.grepFromStdin(VALID_PATTERN, true, true, true, inputStream);
            assertEquals("(standard input):1" + STRING_NEWLINE, results);
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
    void run_nullStdin_shouldThrowGrepException() {
        assertThrows(GrepException.class, () -> grepApplication.run(new String[]{"hello world"}, null, System.out));
    }

    @Test
    void run_validGrepFromStdin_shouldReturnGrepOutput() throws Exception {
        grepApplication.run(new String[]{"hello world", "-"}, inputStream, stdout);
        assertEquals(HELLO_LOWERCASE + STRING_NEWLINE, stdout.toString());
    }

    @Test
    public void grepFromFileAndStdin_ReadFromStdin_ShouldReturnResultsFromStdin() throws Exception {
        ByteArrayInputStream mockedInputStream = new ByteArrayInputStream(HELLO_LOWERCASE.getBytes());
        GrepApplication mockedGrepApplication = Mockito.spy(grepApplication);
        when(mockedGrepApplication.grepFromStdin(HELLO_LOWERCASE, false, false, false, mockedInputStream))
                .thenReturn(HELLO_LOWERCASE + STRING_NEWLINE);
        String results = mockedGrepApplication.grepFromFileAndStdin(HELLO_LOWERCASE, false, false, false, mockedInputStream, "-");
        assertEquals(HELLO_LOWERCASE + STRING_NEWLINE, results);
        Mockito.verify(mockedGrepApplication).grepFromStdin(HELLO_LOWERCASE, false, false, false, mockedInputStream);
    }

    @Test
    public void grepFromFileAndStdin_ReadFromFile_ShouldReturnResultsFromFile() throws Exception {
        ByteArrayInputStream mockedInputStream = new ByteArrayInputStream(HELLO_LOWERCASE.getBytes());
        GrepApplication mockedGrepApplication = Mockito.spy(grepApplication);
        when(mockedGrepApplication.grepFromFiles(HELLO_LOWERCASE, false, false, false, TEST_FILE))
                .thenReturn(TEST_FILE + ": " + HELLO_LOWERCASE + STRING_NEWLINE);
        String results = mockedGrepApplication.grepFromFileAndStdin(HELLO_LOWERCASE, false, false, false, mockedInputStream, TEST_FILE);
        assertEquals(TEST_FILE + ": " + HELLO_LOWERCASE + STRING_NEWLINE, results);
        Mockito.verify(mockedGrepApplication).grepFromFiles(HELLO_LOWERCASE, false, false, false, TEST_FILE);
    }

}
