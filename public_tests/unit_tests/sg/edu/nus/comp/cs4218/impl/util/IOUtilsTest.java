package unit_tests.sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.*;

class IOUtilsTest {

    private final static String TEST_TXT = "test.txt";
    private final static String NON_EXISTENT_TXT = "null/non_existent.txt";

    private String originCurrentDirectory;

    @BeforeEach
    void setUp() throws IOException, ShellException {
        originCurrentDirectory = Environment.currentDirectory;
        String testFilePath = resolveFilePath(TEST_TXT).toString();
        try (OutputStream fileOutputStream = openOutputStream(TEST_TXT);
             Writer writer = new OutputStreamWriter(fileOutputStream)) {
            writer.write("hello\ncs4218");
        }
    }

    @Test
    void testOpenInputStream_FromExistingFile() {
        assertDoesNotThrow(() -> openInputStream(TEST_TXT));
    }

    @Test
    void testOpenInputStream_FromNonExistentFile() {
        assertThrows(ShellException.class, () -> openInputStream(NON_EXISTENT_TXT));
    }

    @Test
    void testOpenOutputStream_FromExistingFile() {
        assertDoesNotThrow(() -> openOutputStream(TEST_TXT));
    }

    @Test
    void testOpenOutputStream_FromNonExistentFile() {
        assertThrows(FileNotFoundException.class, () -> openOutputStream(NON_EXISTENT_TXT));
    }

    @Test
    void testCloseInputStream_Success() throws ShellException {
        InputStream inputStream = openInputStream(TEST_TXT);
        assertDoesNotThrow(() -> closeInputStream(inputStream));
    }

    @Test
    void testCloseOutputStream_Success() throws FileNotFoundException, ShellException {
        OutputStream outputStream = openOutputStream(TEST_TXT);
        assertDoesNotThrow(() -> closeOutputStream(outputStream));
    }

    @Test
    void testCloseInputStream_NullInputStream() {
        assertDoesNotThrow(() -> closeInputStream(null));
    }

    @Test
    void testCloseOutputStream_NullOutputStream() {
        assertDoesNotThrow(() -> closeOutputStream(null));
    }

    @Test
    void testGetLinesFromInputStream_Success() throws IOException, ShellException {
        InputStream inputStream = openInputStream(TEST_TXT);
        List<String> lines = getLinesFromInputStream(inputStream);
        assertNotNull(lines);
        assertEquals(2, lines.size());
        assertEquals("hello", lines.get(0));
        assertEquals("cs4218", lines.get(1));
    }

    @AfterEach
    void tearDown() {
        new File(TEST_TXT).delete();
        System.setProperty("user.dir", originCurrentDirectory);
    }
}
