package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

public class CutTest {

    private CutInterface cutApplication;
    private InputStream inputStdin;
    private OutputStream output;

    private static final String testFileName = "cutTestFile.txt";

    @BeforeEach
    void init() {
        cutApplication = mock(CutInterface.class);

        inputStdin = System.in;
        output = new ByteArrayOutputStream();
    }
    @AfterEach
    void done() throws ShellException {
        IOUtils.closeInputStream(inputStdin);
    }

    @BeforeAll
    static void setUp() throws IOException {
        Path path = Paths.get(testFileName);
        String content = """
                123456789
                123456789
                """;
        Files.writeString(path, content);

    }

    @AfterAll
    static void teardown() throws IOException {
        Path path = Paths.get(testFileName);
        Files.deleteIfExists(path);
    }

    @Test
    void cutFromFiles_fileNamesGiven_cutFromAllFiles() {

    }


    @Test
    void () {

    }
    @Test
    void () {

    }
    @Test
    void () {

    }


    @Test
    void run_bothCharAndByteFlagGiven_tooManyArgsException() throws CutException {
        String[] args = {"-c", "-b"};
        CutException exception = assertThrows(CutException.class, () -> cutApplication.run(args, inputStdin, output));
        assertEquals(new CutException(ERR_BOTH_CHAR_AND_BYTE_FLAGS).getMessage(), exception.getMessage());
    }


    @Test
    void run_zeroGivenAsPositionArg_exception() throws CutException {
        String[] args = {"-c", "-b", "0", testFileName};
        CutException exception = assertThrows(CutException.class, () -> cutApplication.run(args, inputStdin, output));
        assertEquals(new CutException(ERR_ZERO_POSITION_ARG).getMessage(), exception.getMessage());
    }

    @Test
    void run_charFlagGivenWithOneFilename_cutByCharFromFile() throws AbstractApplicationException {
        String[] args = {"-c", "1-5", testFileName};
        cutApplication.run(args, inputStdin, output);
        assertEquals("12345\n", output.toString());
    }


    @Test
    void run_charFlagGivenWithFilenames_cutByCharFromFiles() throws AbstractApplicationException {
        String[] args = {"-c", "1-5", testFileName, testFileName};
        cutApplication.run(args, inputStdin, output);
        assertEquals("12345\n12345\n", output.toString());
    }

    @Test
    void run_charFlagNoFilenames_cutByCharFromStdin() throws AbstractApplicationException {
        String[] args = {"-c", "1-5"};
        cutApplication.run(args, inputStdin, output);
        assertEquals("12345\n", output.toString());
    }

    @Test
    void run_charFlagWithFilenamesAndDash_cutByCharFromFilesAndStdin() throws AbstractApplicationException {
        String[] args = {"-c", "1-5", testFileName, "-"};
        cutApplication.run(args, inputStdin, output);
        assertEquals("12345\n12345\n", output.toString());
    }



}