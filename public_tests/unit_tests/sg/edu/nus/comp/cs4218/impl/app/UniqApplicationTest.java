package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class UniqApplicationTest {

    private UniqInterface uniqApplication;
    private static InputStream inputStdin;
    private OutputStream outputStdout;


    private static final String TEST_FILENAME = "uniqTestFile.txt";
    private static final String OUTPUTTEST_FILENAME = "uniqTestFile2.txt";

    private static final String EMPTY_FILE = "emptyFile.txt";
    private static final String TEST_INPUT = "HELLO_WORLD" + STRING_NEWLINE +
            "HELLO_WORLD" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE +
            "ALICE" +  STRING_NEWLINE +
            "BOB" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE + "BOB";
    private static final String EXPECTEDOUTPUT_UNIQ = "HELLO_WORLD" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE +
            "BOB" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE + "BOB" + STRING_NEWLINE;
    private static final String EXPECTEDOUTPUT_C = "2 " + "HELLO_WORLD" + STRING_NEWLINE +
            "2 " + "ALICE" + STRING_NEWLINE +
            "1 " + "BOB" + STRING_NEWLINE +
            "1 " + "ALICE" + STRING_NEWLINE +
            "1 " + "BOB" + STRING_NEWLINE;
    private static final String EXPECTEDOUTPUT_SMALLD = "HELLO_WORLD" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE;
    private static final String EXPECTEDOUTPUT_D = "HELLO_WORLD" + STRING_NEWLINE +
            "HELLO_WORLD" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE;

    @BeforeAll
    static void setUp() throws IOException {
        Path path1 = Paths.get(TEST_FILENAME);
        Path path2 = Paths.get(OUTPUTTEST_FILENAME);
        Path path3 = Paths.get(EMPTY_FILE);

        Files.deleteIfExists(path1);
        Files.deleteIfExists(path2);
        Files.deleteIfExists(path3);
        Files.writeString(path1, TEST_INPUT);
        Files.writeString(path3, "");
    }

    @BeforeEach
    void init() throws ShellException {
        uniqApplication = new UniqApplication();
        inputStdin = new ByteArrayInputStream(new byte[]{});
        outputStdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void done() throws ShellException {
        IOUtils.closeInputStream(inputStdin);
        IOUtils.closeOutputStream(outputStdout);
    }

    @AfterAll
    static void teardown() throws IOException {
        Path path = Paths.get(TEST_FILENAME);
        Files.deleteIfExists(path);

        path = Paths.get(OUTPUTTEST_FILENAME);
        Files.deleteIfExists(path);

        path = Paths.get(EMPTY_FILE);
        Files.deleteIfExists(path);
    }

    @Test
    void uniqFromFile_EmptyFile_ReturnsEmptyOutput() throws Exception {
        String output = uniqApplication.uniqFromFile(false, false, false, EMPTY_FILE, OUTPUTTEST_FILENAME);
        Path path = Paths.get(OUTPUTTEST_FILENAME);
        Files.write(path, output.getBytes());
        String actualOutput = new String(Files.readAllBytes(path));
        assertEquals(STRING_NEWLINE, actualOutput);
    }

    @Test
    public void uniqFromFile_NoOption_returnsExpectedOutputToFile() throws Exception {
        String output = uniqApplication.uniqFromFile(false, false, false, TEST_FILENAME, OUTPUTTEST_FILENAME);
        Path path = Paths.get(OUTPUTTEST_FILENAME);
        Files.write(path, output.getBytes());

        // Read the content of the output test file
        String actualOutput = new String(Files.readAllBytes(path));
        assertEquals(EXPECTEDOUTPUT_UNIQ, actualOutput);
    }

    @Test
    public void uniqFromFile_AllTrue_returnsExpectedOutputToFile() throws Exception {
        String expected = "2 " + "HELLO_WORLD" + STRING_NEWLINE +
                "2 " + "HELLO_WORLD" + STRING_NEWLINE +
                "2 " + "ALICE" + STRING_NEWLINE +
                "2 " + "ALICE" + STRING_NEWLINE;
        String output = uniqApplication.uniqFromFile(true, true, true, TEST_FILENAME, OUTPUTTEST_FILENAME);
        Path path = Paths.get(OUTPUTTEST_FILENAME);
        Files.write(path, output.getBytes());
        String content = new String(Files.readAllBytes(path));
        assertEquals(expected, content);
    }

    @Test
    public void uniqFromFile_OptionC_returnsExpectedOutputToFile() throws Exception {
        String output = uniqApplication.uniqFromFile(true, false, false, TEST_FILENAME, OUTPUTTEST_FILENAME);
        Path path = Paths.get(OUTPUTTEST_FILENAME);
        Files.write(path, output.getBytes());

        // Read the content of the output test file
        String actualOutput = new String(Files.readAllBytes(path));
        assertEquals(EXPECTEDOUTPUT_C, actualOutput);
    }

    @Test
    public void uniqFromFile_OptionSmallD_returnsExpectedOutputToFile() throws Exception {
        String output = uniqApplication.uniqFromFile(false, true, false, TEST_FILENAME, OUTPUTTEST_FILENAME);
        Path path = Paths.get(OUTPUTTEST_FILENAME);
        Files.write(path, output.getBytes());

        // Read the content of the output test file
        String actualOutput = new String(Files.readAllBytes(path));
        assertEquals(EXPECTEDOUTPUT_SMALLD, actualOutput);
    }

    @Test
    public void uniqFromFile_OptionD_returnsExpectedOutputToFile() throws Exception {
        String output = uniqApplication.uniqFromFile(false, false, true, TEST_FILENAME, OUTPUTTEST_FILENAME);
        Path path = Paths.get(OUTPUTTEST_FILENAME);
        Files.write(path, output.getBytes());

        // Read the content of the output test file
        String actualOutput = new String(Files.readAllBytes(path));
        assertEquals(EXPECTEDOUTPUT_D, actualOutput);
    }

    @Test
    void uniqFromStdin_NoOption_returnsExpectedOutput() throws Exception {
        inputStdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        String output = uniqApplication.uniqFromStdin(false, false, false, inputStdin, null);
        assertEquals(EXPECTEDOUTPUT_UNIQ, output);
    }

    @Test
    void uniqFromStdin_OptionC_returnsExpectedOutput() throws Exception {
        inputStdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        String output = uniqApplication.uniqFromStdin(true, false, false, inputStdin, null);
        assertEquals(EXPECTEDOUTPUT_C, output);
    }

    @Test
    void uniqFromStdin_OptionSmallD_returnsExpectedOutput() throws Exception {
        inputStdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        String output = uniqApplication.uniqFromStdin(false, true, false, inputStdin, null);
        assertEquals(EXPECTEDOUTPUT_SMALLD, output);
    }

    @Test
    void uniqFromStdin_OptionD_returnsExpectedOutput() throws Exception {
        inputStdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        String output = uniqApplication.uniqFromStdin(false, false, true, inputStdin, null);
        assertEquals(EXPECTEDOUTPUT_D, output);
    }

    @Test
    public void uniqFromStdin_AllTrue_returnsExpectedOutput() throws Exception {
        inputStdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        String expected = "2 " + "HELLO_WORLD" + STRING_NEWLINE +
                "2 " + "HELLO_WORLD" + STRING_NEWLINE +
                "2 " + "ALICE" + STRING_NEWLINE +
                "2 " + "ALICE" + STRING_NEWLINE;
        String output = uniqApplication.uniqFromStdin(true, true, true, inputStdin, null);
        assertEquals(expected, output);
    }

}
