package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.impl.app.CatApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
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
        // GIVEN
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
        // AFTER
        System.setIn(originalStdin);
        System.setOut((java.io.PrintStream) originalStdout);

        deleteFile(FILE1_PATH);
        deleteFile(FILE2_PATH);
    }

    private void createFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try (OutputStream os = new FileOutputStream(file, false)) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void run_CatFiles_ShouldOutputCombinedContent() throws Exception {
        // GIVEN
        String[] args = {FILE1_PATH, FILE2_PATH};

        // WHEN
        catApplication.run(args, null, System.out);

        // THEN
        String expectedOutput = String.format("Hello%sWorld%s", System.lineSeparator(), System.lineSeparator());
        assertEquals(expectedOutput, stdoutContent.toString());
    }

    @Test
    void run_CatStdin_ShouldOutputCombinedContent() throws Exception {
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
    void run_CatFileAndStdin_ShouldOutputCombinedContent() throws Exception {
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

    @Test
    void run_NullArgs_ShouldThrowError() throws CatException {
        // WHEN / THEN
        Exception exception = assertThrows(CatException.class,
                () -> catApplication.run(null, null, null));
        assertEquals(new CatException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void run_NullOutputStream_ShouldThrowError() {
        // GIVEN
        String[] args = {FILE1_PATH};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, null));
    }

    @Test
    void run_InvalidArguments_ShouldThrowError() {
        // GIVEN
        String[] args = {"-invalidFlag"};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, System.out));
    }

    @Test
    void run_NullStdin_ShouldThrowError() {
        // GIVEN
        String[] args = {};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, null, System.out));
    }

    @Test
    void runCatFiles_NullIsLineNumber_ShouldThrowCatException() {
        // GIVEN / WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.catFiles(null));
    }

    @Test
    void runCatFiles_NoFileNames_ShouldThrowCatException() {
        // GIVEN / WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.catFiles(true));
    }

    @Test
    void run_WriteStreamException_ShouldThrowCatException() throws CatException, IOException {
        // GIVEN
        String[] args = {"valid-argument"};
        InputStream stdin = new ByteArrayInputStream("Hello".getBytes());

        // Creating a custom OutputStream that throws IOException on write
        OutputStream throwingOutputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Simulated write failure");
            }
        };

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.run(args, stdin, throwingOutputStream));
    }

    @Test
    void run_CatFileAndStdinNullInputStream_ShouldThrowCatException() throws CatException {
        // GIVEN
        CatApplication catApplication = new CatApplication();
        Boolean isLineNumber = true;
        String[] fileName = {"file1.txt"};

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.catFileAndStdin(isLineNumber, null, fileName),
                "Expected ERR_NO_ISTREAM exception");
    }

    @Test
    void run_CatFileAndStdinNoFileArguments_ShouldThrowCatException() throws CatException {
        // GIVEN
        CatApplication catApplication = new CatApplication();
        Boolean isLineNumber = true;
        InputStream stdin = new ByteArrayInputStream("Hello".getBytes());

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.catFileAndStdin(isLineNumber, stdin),
                "Expected ERR_NO_FILE_ARGS exception");
    }

    @Test
    void run_CatStdinNullArgs_ShouldThrowCatException() throws CatException {
        // GIVEN
        CatApplication catApplication = new CatApplication();
        Boolean isLineNumber = true;

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.catStdin(null, null),
                "Expected ERR_NULL_ARGS exception");
    }

    @Test
    void run_CatStdinNullInputStream_ShouldThrowCatException() throws CatException {
        // GIVEN
        CatApplication catApplication = new CatApplication();
        Boolean isLineNumber = true;

        // WHEN / THEN
        assertThrows(CatException.class, () -> catApplication.catStdin(isLineNumber, null),
                "Expected ERR_NO_ISTREAM exception");
    }

    @Test
    void run_catFilesAddLineNumbers_ShouldNumberLines() throws Exception {
        // GIVEN
        String[] args = {FILE1_PATH, FILE2_PATH};
        Boolean isLineNumber = true;

        // WHEN
        String output = catApplication.catFiles(isLineNumber, args);

        // THEN
        String expectedOutput = String.format("1 Hello%s1 World%s", System.lineSeparator(), System.lineSeparator());
        assertEquals(expectedOutput, output + StringUtils.STRING_NEWLINE);
    }

    @Test
    void run_CatStdinWithLineNumbers_ShouldOutputNumberedLines() throws Exception {
        // GIVEN
        String[] args = {"-n"};
        String inputContent = "Hello\r\nWorld\r\n";
        ByteArrayInputStream stdinContent = new ByteArrayInputStream(inputContent.getBytes());
        System.setIn(stdinContent);
        Boolean isLineNumber = true;

        // WHEN
        String output = catApplication.catStdin(isLineNumber, stdinContent);

        // THEN
        String expectedOutput = String.format("1 Hello%s2 World%s", System.lineSeparator(), System.lineSeparator());
        assertEquals(expectedOutput, output + StringUtils.STRING_NEWLINE);
    }
}
