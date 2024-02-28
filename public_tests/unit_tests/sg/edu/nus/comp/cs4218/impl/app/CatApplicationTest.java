package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.impl.app.CatApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

import java.io.*;
import java.nio.charset.StandardCharsets;

class CatApplicationTest {
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "unit_tests", "cat"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);
    private static final String FILE1_NAME = "file1.txt";
    private static final String FILE2_NAME = "file2.txt";
    private static final String FILE1_PATH = TEST_DIRECTORY + STR_FILE_SEP + FILE1_NAME;
    private static final String FILE2_PATH = TEST_DIRECTORY + STR_FILE_SEP + FILE2_NAME;

    private CatApplication catApplication;
    private InputStream originalStdin;
    private OutputStream originalStdout;
    private ByteArrayOutputStream stdoutContent;

    @BeforeEach
    void setUp() throws Exception {
        catApplication = new CatApplication();
        originalStdin = System.in;
        originalStdout = System.out;
        stdoutContent = new ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(stdoutContent));

        createFile(FILE1_PATH, "Hello");
        createFile(FILE2_PATH, "World");
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalStdin);
        System.setOut((java.io.PrintStream) originalStdout);

        deleteFile(FILE1_PATH);
        deleteFile(FILE2_PATH);
    }

    @Test
    void testCatFiles() throws Exception {
        // GIVEN
        String[] args = {FILE1_PATH, FILE2_PATH};

        // WHEN
        catApplication.run(args, null, System.out);

        // THEN
        String expectedOutput = String.format("Hello%sWorld%s", System.lineSeparator(), System.lineSeparator());
        assertEquals(expectedOutput, stdoutContent.toString());
    }


    @Test
    void testCatStdin() throws Exception {
        // GIVEN
        String[] args = {};
        String inputContent = "Hello\r\nWorld\r\n";
        ByteArrayInputStream stdinContent = new ByteArrayInputStream(inputContent.getBytes());
        System.setIn(stdinContent);

        // WHEN
        catApplication.run(args, System.in, System.out);

        // THEN
        String expectedOutput = String.format("Hello%sWorld%s", System.lineSeparator(), System.lineSeparator());
        assertEquals(expectedOutput, stdoutContent.toString());
    }

    @Test
    void testCatFileAndStdin() throws Exception {
        // GIVEN
        String[] args = {FILE1_PATH, "-"};
        ByteArrayInputStream stdinContent = new ByteArrayInputStream("World".getBytes());
        System.setIn(stdinContent);

        // WHEN
        catApplication.run(args, System.in, System.out);

        // THEN
        String expectedOutput = String.format("Hello%sWorld%s", System.lineSeparator(), System.lineSeparator());
        assertEquals(expectedOutput, stdoutContent.toString());
    }

    private void createFile(String filePath, String content) throws IOException {
        File file = new File(filePath);

        file.getParentFile().mkdirs();

        OutputStream os = new FileOutputStream(file, false);
        os.write(content.getBytes(StandardCharsets.UTF_8));
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testCatNullArguments() {
        // GIVEN
        String[] args = null;

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, System.out));
    }

    @Test
    void testCatNullOutputStream() {
        // GIVEN
        String[] args = {FILE1_PATH};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, null));
    }

    @Test
    void testCatInvalidArguments() {
        // GIVEN
        String[] args = {"-invalidFlag"};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, System.out));
    }

    @Test
    void testCatNonexistentFile() {
        // GIVEN
        String[] args = {"nonexistent.txt"};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, System.out));
    }

    @Test
    void testCatFileIOException() {
        // GIVEN
        String[] args = {ROOT_DIRECTORY}; // Assuming ROOT_DIRECTORY is a directory, not a file

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, System.out));
    }

    @Test
    void testCatNullStdin() {
        // GIVEN
        String[] args = {};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, System.out));
    }
}
