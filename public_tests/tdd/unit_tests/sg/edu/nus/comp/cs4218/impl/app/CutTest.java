package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CutTest {

    private CutInterface cutApplication;
    private static InputStream inputStdin;
    private InputStream inputTestFile;
    private InputStream inputEmptyTestFile;
    private OutputStream output;

    private static final String TEST_FILENAME = "cutTestFile.txt";
    private static final String EMPTY_TEST_FILENAME = "cutEmptyTestFile.txt";

    @BeforeEach
    void init() throws ShellException {
        cutApplication = mock(CutInterface.class);

        inputTestFile = IOUtils.openInputStream(TEST_FILENAME);
        inputEmptyTestFile = IOUtils.openInputStream(EMPTY_TEST_FILENAME);

        output = new ByteArrayOutputStream();
    }

    @AfterEach
    void done() throws ShellException {
        IOUtils.closeInputStream(inputTestFile);
        IOUtils.closeInputStream(inputEmptyTestFile);
    }

    @BeforeAll
    static void setUp() throws IOException {
        inputStdin = System.in;

        Path path = Paths.get(TEST_FILENAME);
        String content = """
                123456789
                123456789
                """;
        Files.writeString(path, content);
    }

    @AfterAll
    static void teardown() throws IOException {
        Path path = Paths.get(TEST_FILENAME);
        Files.deleteIfExists(path);

        path = Paths.get(EMPTY_TEST_FILENAME);
        Files.deleteIfExists(path);
    }

    @Test
    void cutFromFiles_bothIsCharPoAndIsBytePoIsFalse_exception() {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {TEST_FILENAME};

        CutException exception = assertThrows(CutException.class, () -> cutApplication.cutFromFiles(false, false, ranges, fileNames));
        assertEquals(new CutException(ERR_ISCHARPO_AND_ISBYTEPO_FALSE).getMessage(), exception.getMessage());
    }

    @Test
    void cutFromFiles_nullFileNameGiven_exception() {
        List<int[]> ranges = List.of(new int[]{3, 7});

        CutException exception = assertThrows(CutException.class, () -> cutApplication.cutFromFiles(true, false, ranges, null));
        assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    void cutFromFiles_rangesEmpty_exception() {
        List<int[]> ranges = List.of();
        String[] fileNames = {TEST_FILENAME};

        CutException exception = assertThrows(CutException.class, () -> cutApplication.cutFromFiles(true, false, ranges, fileNames));
        assertEquals(new CutException(ERR_RANGE_EMPTY).getMessage(), exception.getMessage());
    }

    @Test
    void cutFromFiles_fileIsEmpty_noOutput() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {EMPTY_TEST_FILENAME};

        cutApplication.cutFromFiles(true, false, ranges, fileNames);
        assertEquals("", output.toString());
    }

    @Test
    void cutFromFiles_isCharPoAndFileNameGiven_cutFromFile() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {TEST_FILENAME};

        cutApplication.cutFromFiles(true, false, ranges, fileNames);
        assertEquals("34567\n34567\n", output.toString());
    }

    @Test
    void cutFromFiles_isBytePoSameStartAndEndIndexInRange_cutFromFile() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 3});
        String[] fileNames = {TEST_FILENAME};

        cutApplication.cutFromFiles(false, true, ranges, fileNames);
        assertEquals("3\n3\n", output.toString());
    }

    @Test
    void cutFromFiles_isBytePoAndFileNamesGiven_cutFromAllFiles() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 7});
        String[] fileNames = {TEST_FILENAME, TEST_FILENAME};

        cutApplication.cutFromFiles(false, true, ranges, fileNames);
        assertEquals("34567\n34567\n34567\n34567\n", output.toString());
    }

    @Test
    void cutFromFiles_listOfRangeGiven_outputCutInSequenceOfIndex() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{5, 7}, new int[]{1, 3});
        String[] fileNames = {TEST_FILENAME};

        cutApplication.cutFromFiles(false, true, ranges, fileNames);
        assertEquals("123567\n123567\n", output.toString());
    }


    @Test
    void cutFromStdin_bothIsCharPoAndIsBytePoIsFalse_exception() {
        List<int[]> ranges = List.of(new int[]{3, 7});

        CutException exception = assertThrows(CutException.class, () -> cutApplication.cutFromStdin(false, false, ranges, inputTestFile));
        assertEquals(new CutException(ERR_ISCHARPO_AND_ISBYTEPO_FALSE).getMessage(), exception.getMessage());
    }


    @Test
    void cutFromStdin_rangesEmpty_exception() {
        List<int[]> ranges = List.of();

        CutException exception = assertThrows(CutException.class, () -> cutApplication.cutFromStdin(true, false, ranges, inputTestFile));
        assertEquals(new CutException(ERR_RANGE_EMPTY).getMessage(), exception.getMessage());
    }

 
    @Test
    void cutFromStdin_isBytePo_cutFromStdin() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 7});

        cutApplication.cutFromStdin(false, true, ranges, inputTestFile);
        assertEquals("34567\n34567\n", output.toString());
    }

    @Test
    void cutFromStdin_isBytePoSameStartAndEndIndexInRange_cutFromStdin() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{3, 3});

        cutApplication.cutFromStdin(false, true, ranges, inputTestFile);
        assertEquals("3\n3\n", output.toString());
    }

    @Test
    void cutFromStdin_isCharPoAndListOfRangeGiven_outputCutInSequenceOfIndex() throws AbstractApplicationException {
        List<int[]> ranges = List.of(new int[]{5, 7}, new int[]{1, 3});

        cutApplication.cutFromStdin(true, false, ranges, inputTestFile);
        assertEquals("123567\n123567\n", output.toString());
    }


    @Test
    void run_bothCharAndByteFlagGiven_tooManyArgsException() throws CutException {
        String[] args = {"-c", "-b"};

        CutException exception = assertThrows(CutException.class, () -> cutApplication.run(args, inputStdin, output));
        assertEquals(new CutException(ERR_BOTH_CHAR_AND_BYTE_FLAGS_PRESENT).getMessage(), exception.getMessage());
    }


    @Test
    void run_zeroGivenAsPositionArg_exception() {
        String[] args = {"-c", "-b", "0", TEST_FILENAME};

        CutException exception = assertThrows(CutException.class, () -> cutApplication.run(args, inputStdin, output));
        assertEquals(new CutException(ERR_ZERO_POSITION_ARG).getMessage(), exception.getMessage());
    }

    @Test
    void run_charFlagGivenWithOneFilename_cutByCharFromFile() throws AbstractApplicationException {
        String[] args = {"-c", "1-5", TEST_FILENAME};

        cutApplication.run(args, inputStdin, output);
        assertEquals("12345\n12345\n", output.toString());
    }


    @Test
    void run_charFlagGivenWithFilenames_cutByCharFromFiles() throws AbstractApplicationException {
        String[] args = {"-c", "1-5", TEST_FILENAME, TEST_FILENAME};

        cutApplication.run(args, inputStdin, output);
        assertEquals("12345\n12345\n12345\n12345\n", output.toString());
    }

    @Test
    void run_charFlagNoFilenames_cutByCharFromStdin() throws AbstractApplicationException {
        String[] args = {"-c", "1-5"};

        cutApplication.run(args, inputTestFile, output);
        assertEquals("12345\n12345\n", output.toString());
    }

    @Test
    void run_byteFlagWithFilenamesAndDash_cutByByteFromFilesAndStdin() throws AbstractApplicationException {
        String[] args = {"-b", "1-5", TEST_FILENAME, "-"};

        cutApplication.run(args, inputTestFile, output);
        assertEquals("12345\n12345\n12345\n12345\n", output.toString());
    }

    @Test
    void run_byteFlagWithTwoDash_cutByByteFromStdinOnlyOnce() throws AbstractApplicationException {
        String[] args = {"-b", "1-5", "-", "-"};

        cutApplication.run(args, inputTestFile, output);
        assertEquals("12345\n12345\n", output.toString());
    }


    @Test
    void run_rangeEndIndexLongerThanFileContent_cutUntilEndOfContent() throws AbstractApplicationException {
        String[] args = {"-b", "1-100", TEST_FILENAME};

        cutApplication.run(args, inputTestFile, output);
        assertEquals("123456789\n123456789\n", output.toString());
    }


    @Test
    void run_rangeNoStartIndex_cutFromStartOfFileUntilEndIndex() throws AbstractApplicationException {
        String[] args = {"-b", "-5", TEST_FILENAME};

        cutApplication.run(args, inputTestFile, output);
        assertEquals("12345\n12345\n", output.toString());
    }

    @Test
    void run_commaSeparatedNumber_cutAllInvidiualNumbers() throws AbstractApplicationException {
        String[] args = {"-b", "1,3,5", TEST_FILENAME};

        cutApplication.run(args, inputTestFile, output);
        assertEquals("135\n135\n", output.toString());
    }

    @Test
    void run_twoRanges_cutAllInvidiualNumbers() throws AbstractApplicationException {
        String[] args = {"-b", "1-5", "7-9", TEST_FILENAME};

        cutApplication.run(args, inputTestFile, output);
        assertEquals("12345789\n12345789\n", output.toString());
    }


}