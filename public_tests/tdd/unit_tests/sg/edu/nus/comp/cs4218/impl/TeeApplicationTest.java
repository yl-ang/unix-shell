package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class TeeApplicationTest {

    private TeeInterface teeApplication;
    private InputStream inputStdin;
    private OutputStream output;

    private static final String TEST_OUTPUT_FILENAME = "teeOutputTestFile.txt";

    private static final String TEST_FILENAME = "cutTestFile.txt";
    private static Path testFilePath;

    @BeforeAll
    static void setUp() throws IOException {
        // add contents into test file
        Path path = Paths.get(TEST_FILENAME);
        String content = """
                test tee
                123456789
                """;
        Files.writeString(path, content);

        // create output file
        testFilePath = Paths.get(TEST_OUTPUT_FILENAME);
        Files.createFile(testFilePath);
    }

    @BeforeEach
    void init() throws IOException, ShellException {
        teeApplication = mock(TeeInterface.class);

        inputStdin = IOUtils.openInputStream(TEST_FILENAME);

        output = new ByteArrayOutputStream();
    }

    @AfterEach
    void done() throws IOException, ShellException {
        IOUtils.closeInputStream(inputStdin);
    }

    @AfterAll
    static void teardown() throws IOException {
        Files.deleteIfExists(testFilePath);

        Path path = Paths.get(TEST_FILENAME);
        Files.deleteIfExists(path);
    }

    @Test
    void teeFromStdin_nullFileNameGiven_exception() {
        String[] fileNames = {null};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, inputStdin, fileNames);
        assertEquals(new TeeException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void teeFromStdin_fileNotFound_exception() {
        String[] fileNames = {"randomfilename.txt"};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, inputStdin, fileNames);
        assertEquals(new TeeException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
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
        String[] fileNames = {TEST_OUTPUT_FILENAME};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, null, fileNames);
        assertEquals(new TeeException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }


    @Test
    void teeFromStdin_isAppendFalse_fileWithContentGetsOverridden() throws AbstractApplicationException, IOException {
        Path path = Paths.get(TEST_OUTPUT_FILENAME);
        String content = """
                123456789
                123456789
                """;
        Files.writeString(path, content);

        String[] fileNames = {TEST_OUTPUT_FILENAME};

        teeApplication.teeFromStdin(false, inputStdin, fileNames);
        assertEquals("test tee\n123456789\n", output.toString());
    }

    @Test
    void teeFromStdin_isAppendFTrue_fileWithContentGetsAppended() throws AbstractApplicationException, IOException {
        Path path = Paths.get(TEST_OUTPUT_FILENAME);
        String content = """
                123456789
                123456789
                """;
        Files.writeString(path, content);

        String[] fileNames = {TEST_OUTPUT_FILENAME};

        teeApplication.teeFromStdin(false, inputStdin, fileNames);
        assertEquals("123456789\n123456789\ntest tee\n123456789\n", output.toString());
    }



    @Test
    void run_bothCharAndByteFlagGiven_tooManyArgsException() throws TeeException {
        String[] args = {"-c", "-b"};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStdin, output));
        assertEquals(new TeeException(ERR_BOTH_CHAR_AND_BYTE_FLAGS_PRESENT).getMessage(), exception.getMessage());
    }


    @Test
    void run_zeroGivenAsPositionArg_exception() {
        String[] args = {"-c", "-b", "0", TEST_OUTPUT_FILENAME};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStdin, output));
        assertEquals(new TeeException(ERR_ZERO_POSITION_ARG).getMessage(), exception.getMessage());
    }


}