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
    private InputStream inputTestFile;
    private OutputStream output;

    private static final String TEST_FILENAME = "teeTestFile.txt";
    private Path testFilePath;

    @BeforeAll
    static void setUp() throws IOException {

    }

    @BeforeEach
    void init() throws IOException {
        teeApplication = mock(TeeInterface.class);

        testFilePath = Paths.get(TEST_FILENAME);
        Files.createFile(testFilePath);

        output = new ByteArrayOutputStream();
    }

    @AfterEach
    void done() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @AfterAll
    static void teardown() throws IOException {
    }

    @Test
    void teeFromStdin_bothIsCharPoAndIsBytePoIsFalse_exception() {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {TEST_FILENAME};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, false, ranges, fileNames));
        assertEquals(new TeeException(ERR_ISCHARPO_AND_ISBYTEPO_FALSE).getMessage(), exception.getMessage());
    }

    @Test
    void teeFromStdin_nullFileNameGiven_exception() {
        List<int[]> ranges = List.of(new int[]{3, 7});

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(true, false, ranges, null));
        assertEquals(new TeeException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }


    @Test
    void teeFromStdin_fileNotFound_exception() {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {"randomfilename.txt"};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(true, false, ranges, fileNames));
        assertEquals(new TeeException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
    }

    @Test
    void teeFromStdin_fileNameIsDirectory_exception() {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {"src"};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(true, false, ranges, fileNames));
        assertEquals(new TeeException(ERR_IS_DIR).getMessage(), exception.getMessage());
    }

    @Test
    void teeFromStdin_fileNoReadPermission_exception() throws IOException {
        // create file without read permissions
        Set<PosixFilePermission> noReadPermission = PosixFilePermissions.fromString("--x--x--x");
        FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(noReadPermission);
        String fileName = "fileWithNoReadPermissions.txt";
        Path filePath = Paths.get(fileName);
        Files.createFile(filePath, permissions);

        // input args
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {fileName};

        // then
        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(true, false, ranges, fileNames));
        assertEquals(new TeeException(ERR_NO_PERM).getMessage(), exception.getMessage());

        // remove file
        Files.deleteIfExists(filePath);
    }


    @Test
    void teeFromStdin_fileIsEmpty_noOutput() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {EMPTY_TEST_FILENAME};

        teeApplication.teeFromStdin(true, false, ranges, fileNames);
        assertEquals("", output.toString());
    }

    @Test
    void teeFromStdin_isCharPoAndFileNameGiven_cutFromFile() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {TEST_FILENAME};

        teeApplication.teeFromStdin(true, false, ranges, fileNames);
        assertEquals("34567\n34567\n", output.toString());
    }

    @Test
    void teeFromStdin_isBytePoSameStartAndEndIndexInRange_cutFromFile() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 3});
        String[] fileNames = {TEST_FILENAME};

        teeApplication.teeFromStdin(false, true, ranges, fileNames);
        assertEquals("3\n3\n", output.toString());
    }

    @Test
    void teeFromStdin_isBytePoAndFileNamesGiven_cutFromAllFiles() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {TEST_FILENAME, TEST_FILENAME};

        teeApplication.teeFromStdin(false, true, ranges, fileNames);
        assertEquals("34567\n34567\n34567\n34567\n", output.toString());
    }

    @Test
    void teeFromStdin_listOfRangeGiven_outputCutInSequenceOfIndex() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{5, 7}, new int[]{1, 3});
        String[] fileNames = {TEST_FILENAME};

        teeApplication.teeFromStdin(false, true, ranges, fileNames);
        assertEquals("123567\n123567\n", output.toString());
    }


    @Test
    void run_bothCharAndByteFlagGiven_tooManyArgsException() throws TeeException {
        String[] args = {"-c", "-b"};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStdin, output));
        assertEquals(new TeeException(ERR_BOTH_CHAR_AND_BYTE_FLAGS_PRESENT).getMessage(), exception.getMessage());
    }


    @Test
    void run_zeroGivenAsPositionArg_exception() {
        String[] args = {"-c", "-b", "0", TEST_FILENAME};

        TeeException exception = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStdin, output));
        assertEquals(new TeeException(ERR_ZERO_POSITION_ARG).getMessage(), exception.getMessage());
    }


}