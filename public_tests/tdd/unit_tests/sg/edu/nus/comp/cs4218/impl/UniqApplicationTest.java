package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class UniqApplicationTest {

    private UniqInterface uniqApplication;
    private static InputStream inputStdin;
    private InputStream inputTestFile;
    private InputStream inputEmptyTestFile;
    private OutputStream output;

    private static final String TEST_FILENAME = "uniqTestFile.txt";
    private static final String EMPTY_TEST_FILENAME = "uniqEmptyTestFile.txt";
    private static final String TEST_INPUT = "HELLO_WORLD" + STRING_NEWLINE +
            "HELLO_WORLD" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE +
            "ALICE" +  STRING_NEWLINE +
            "BOB" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE + "BOB";
    private static final String EXPECTEDOUTPUT_UNIQ = "HELLO_WORLD" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE +
            "BOB" + STRING_NEWLINE +
            "ALICE" + STRING_NEWLINE + "BOB";
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
        inputStdin = System.in;

        Path path = Paths.get(TEST_FILENAME);
        Files.writeString(path, TEST_INPUT);
    }

    @BeforeEach
    void init() throws ShellException {
        uniqApplication = mock(UniqInterface.class);

        inputTestFile = IOUtils.openInputStream(TEST_FILENAME);
        inputEmptyTestFile = IOUtils.openInputStream(EMPTY_TEST_FILENAME);

        output = new ByteArrayOutputStream();
    }

    @AfterEach
    void done() throws ShellException {
        IOUtils.closeInputStream(inputTestFile);
        IOUtils.closeInputStream(inputEmptyTestFile);
    }

    @AfterAll
    static void teardown() throws IOException {
        Path path = Paths.get(TEST_FILENAME);
        Files.deleteIfExists(path);

        path = Paths.get(EMPTY_TEST_FILENAME);
        Files.deleteIfExists(path);
    }

    @Test
    public void uniqFromFile_OptionC_returnsExpectedOutput() throws Exception {
        String output = uniqApplication.uniqFromFile(true, false, false, TEST_INPUT, null);
        assertEquals(EXPECTEDOUTPUT_C, output);
    }


}
