package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class TeeApplicationTest {

    private TeeInterface teeApplication;
    private InputStream inputStdin;
    private OutputStream outputStream;

    private static final String OUTPUT_FILENAME = "teeOutputTestFile.txt";
    private static final String INPUT_FILENAME = "cutTestFile.txt";
    private static Path outputFilePath;

    @BeforeAll
    static void setUp() throws IOException {
        // add contents into test file
        Path path = Paths.get(INPUT_FILENAME);
        String content = """
                test tee
                123456789
                """;
        Files.writeString(path, content);

        // create output file
        outputFilePath = Paths.get(OUTPUT_FILENAME);
        Files.createFile(outputFilePath);
    }

    @BeforeEach
    void init() throws IOException, ShellException {
        teeApplication = mock(TeeInterface.class);

        inputStdin = IOUtils.openInputStream(INPUT_FILENAME);

        Files.createFile(outputFilePath);

        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void done() throws IOException, ShellException {
        IOUtils.closeInputStream(inputStdin);
        Files.deleteIfExists(outputFilePath);
    }

    @AfterAll
    static void teardown() throws IOException {
        Path path = Paths.get(INPUT_FILENAME);
        Files.deleteIfExists(path);
    }

    @Test
    void teeFromStdin_nullFileNameGiven_exception() {
        String[] fileNames = {null};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, inputStdin, fileNames));
        assertEquals(new TeeException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void teeFromStdin_fileNameIsDirectory_exception() {
        String[] fileNames = {"src"};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, inputStdin, fileNames));
        assertEquals(new TeeException(ERR_IS_DIR).getMessage(), exception.getMessage());
    }

    @Test
    void teeFromStdin_fileNoWritePermission_exception() throws IOException {
        // create file without write permissions
        Set<PosixFilePermission> noWritePermission = PosixFilePermissions.fromString("--x--x--x");
        FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(noWritePermission);
        String fileName = "fileWithNoWritePermissions.txt";
        Path filePath = Paths.get(fileName);
        Files.createFile(filePath, permissions);

        // input args
        String[] fileNames = {fileName};

        // then
        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, inputStdin, fileNames));
        assertEquals(new TeeException(ERR_NO_PERM_WRITE_FILE).getMessage(), exception.getMessage());

        // remove file
        Files.deleteIfExists(filePath);
    }

    @Test
    void teeFromStdin_stdinIsNull_exception() {
        String[] fileNames = {OUTPUT_FILENAME};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, null, fileNames));
        assertEquals(new TeeException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void teeFromStdin_isAppendIsNull_exception() {
        String[] fileNames = {OUTPUT_FILENAME};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(null, inputStdin, fileNames));
        assertEquals(new TeeException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }


    @Test
    void teeFromStdin_isAppendFalse_inputIsReturnedAndFileWithContentGetsOverridden() throws AbstractApplicationException, IOException {
        Path path = Paths.get(OUTPUT_FILENAME);
        String content = """
                123456789
                123456789
                """;
        Files.writeString(path, content);

        String[] fileNames = {OUTPUT_FILENAME};

        String output = teeApplication.teeFromStdin(false, inputStdin, fileNames);

        // check output
        assertEquals("test tee\n123456789\n", output); //NOPMD - suppressed AvoidDuplicateLiterals - Clarity

        // check file changed
        String result = new String(Files.readAllBytes(outputFilePath));
        assertEquals("test tee\n123456789\n", result);
    }

    @Test
    void teeFromStdin_isAppendTrue_inputIsReturnedAndFileWithContentGetsAppended() throws AbstractApplicationException, IOException {
        Path path = Paths.get(OUTPUT_FILENAME);
        String content = """
                123456789
                123456789
                """;
        Files.writeString(path, content);

        String[] fileNames = {OUTPUT_FILENAME};

        String output = teeApplication.teeFromStdin(true, inputStdin, fileNames);

        // check output
        assertEquals("test tee\n123456789\n", output);

        // check file changed
        String result = new String(Files.readAllBytes(outputFilePath));
        assertEquals("123456789\n123456789\ntest tee\n123456789\n", result);
    }

    @Test
    void teeFromStdin_multipleFileNames_allFilesUpdated() throws AbstractApplicationException, IOException {
        String secondFile = "secondFile.txt";
        String[] fileNames = {OUTPUT_FILENAME, secondFile};

        String output = teeApplication.teeFromStdin(false, inputStdin, fileNames);

        // check output
        assertEquals("test tee\n123456789\n", output);

        // check file changed
        String result = new String(Files.readAllBytes(outputFilePath));
        assertEquals("test tee\n123456789\n", result);

        Path path = Paths.get(secondFile);
        String result2 = new String(Files.readAllBytes(path));
        assertEquals("test tee\n123456789\n", result2);
    }

    @Test
    void teeFromStdin_noFilesGiven_noOutput() throws AbstractApplicationException, IOException {
        String[] fileNames = {};

        String output = teeApplication.teeFromStdin(false, inputStdin, fileNames);

        assertEquals("", output);
    }

    @Test
    void run_isAppendTrue_inputIsShownOnStdoutAndFileWithContentGetsAppended() throws IOException, AbstractApplicationException {
        Path path = Paths.get(OUTPUT_FILENAME);
        String content = """
                123456789
                123456789
                """;
        Files.writeString(path, content);

        String[] args = {"-a", OUTPUT_FILENAME};

        teeApplication.run(args, inputStdin, outputStream);

        // check output
        assertEquals("test tee\n123456789\n", outputStream.toString());

        // check file changed
        String result = new String(Files.readAllBytes(outputFilePath));
        assertEquals("123456789\n123456789\ntest tee\n123456789\n", result);
    }

}