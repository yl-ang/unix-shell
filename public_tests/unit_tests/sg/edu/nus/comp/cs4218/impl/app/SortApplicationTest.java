package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.SortException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


class SortApplicationTest {

    private static sg.edu.nus.comp.cs4218.impl.app.SortApplication sortApplication;

    private static final String ROOT_DIRECTORY = sg.edu.nus.comp.cs4218.Environment.currentDirectory;
    private static final String TEST_DIRECTORY = ROOT_DIRECTORY + File.separator + "test_folder";
    static String file = "file.txt";
    static String file2 = "file2.txt";
    static String path = TEST_DIRECTORY + File.separator + file;
    static String path2 = TEST_DIRECTORY + File.separator + file2;

    @BeforeAll
    static void setup() throws IOException {
        // Setup test folder
        sortApplication = new sg.edu.nus.comp.cs4218.impl.app.SortApplication();
        File testDirFile = new File(TEST_DIRECTORY);
        testDirFile.mkdirs();
        Files.createDirectories(Paths.get(TEST_DIRECTORY));

        Files.createFile(Paths.get(path));
        Files.createFile(Paths.get(path2));
        Files.write(Paths.get(path), ("10" + STRING_NEWLINE + "2" + STRING_NEWLINE + "1").getBytes(), APPEND);
        Files.write(Paths.get(path2), ("a" + STRING_NEWLINE + "A" + STRING_NEWLINE + "ac" + STRING_NEWLINE + "AC").getBytes(), APPEND);
    }

    @AfterAll
    static void tearDown() {
        sg.edu.nus.comp.cs4218.Environment.currentDirectory = ROOT_DIRECTORY;
        File folder = new File(TEST_DIRECTORY);
        emptyFolder(folder, true);
    }

    @Test
    void sortFromFiles_notFirstWordNumberReverseOrderNotCaseIndependent_ShouldPass() throws Exception {
        String output = sortApplication.sortFromFiles(false, true, false, path);
        assertEquals("2" + STRING_NEWLINE + "10" + STRING_NEWLINE + "1" + STRING_NEWLINE, output);

    }

    @Test
    void sortFromFiles_notFirstWordNumberNotReverseOrderNotCaseIndependent_ShouldPass() throws Exception {
        String output = sortApplication.sortFromFiles(false, false, false, path);
        assertEquals("1" + STRING_NEWLINE + "10" + STRING_NEWLINE + "2" + STRING_NEWLINE, output);

    }

    @Test
    void sortFromFiles_firstWordNumberNotReverseOrder_ShouldPass() throws Exception {
        String output = sortApplication.sortFromFiles(true, false, false, path);
        assertEquals("1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "10" + STRING_NEWLINE, output);

    }

    @Test
    void sortFromFiles_notFirstWordNumberReverseOrderNotCaseIndependentLetter_ShouldPass() throws Exception {
        String output = sortApplication.sortFromFiles(false, true, false, path2);
        assertEquals("ac" + STRING_NEWLINE + "a" + STRING_NEWLINE + "AC" + STRING_NEWLINE + "A" + STRING_NEWLINE, output);

    }

    @Test
    void sortFromFiles_FirstWordNumberReverseOrderCaseIndependentLetter_ShouldPass() throws Exception {
        String output = sortApplication.sortFromFiles(true, true, true, path2);
        assertEquals("AC" + STRING_NEWLINE + "ac" + STRING_NEWLINE + "A" + STRING_NEWLINE + "a" + STRING_NEWLINE, output);

    }

    @Test
    void run_NullStdout_ThrowsSortException() {
        assertThrows(SortException.class, () -> sortApplication.run(new String[]{}, System.in, null));
    }

    @Test
    void sortFromFiles_NonExistentFile_ThrowsSortException() {
        String[] fileNames = {"nonexistent.txt"};
        assertThrows(SortException.class, () -> sortApplication.sortFromFiles(true, false, false, fileNames));
    }

    @Test
    void run_WithInvalidArguments_ShouldThrow() {
        String inputString = "ac" + STRING_NEWLINE + "a" + STRING_NEWLINE + "A" + STRING_NEWLINE + "AC" + STRING_NEWLINE;
        InputStream input = new ByteArrayInputStream(inputString.getBytes());
        String[] args = new String[]{"-z"};
        assertThrows(SortException.class, () -> sortApplication.run(args, input, System.out));
    }

    @Test
    void run_WithValidArgsSortsFromFiles_ShouldPass() {
        String inputString = "ac\na\nA\nAC";

        try {
            InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String[] args = new String[]{"-r"};
            sortApplication.run(args, inputStream, outputStream);
            String actualOutput = outputStream.toString();
            String expectedOutput = "ac\na\nAC\nA\n";
            assertEquals(expectedOutput, actualOutput);

            inputStream = new ByteArrayInputStream(inputString.getBytes());
            outputStream.reset();
            String[] args2 = new String[]{"-n"};
            sortApplication.run(args2, inputStream, outputStream);
            String actualOutput2 = outputStream.toString();
            String expectedOutput2 = "A\nAC\na\nac\n";
            assertEquals(expectedOutput2, actualOutput2);

            inputStream = new ByteArrayInputStream(inputString.getBytes());
            outputStream.reset();
            String[] args3 = new String[]{"-f"};
            sortApplication.run(args3, inputStream, outputStream);
            String actualOutput3 = outputStream.toString();
            String expectedOutput3 = "a\nA\nac\nAC\n";
            assertEquals(expectedOutput3, actualOutput3);

            inputStream = new ByteArrayInputStream(inputString.getBytes());
            outputStream.reset();
            String[] args4 = new String[]{"-r", "-f"};
            sortApplication.run(args4, inputStream, outputStream);
            String actualOutput4 = outputStream.toString();
            String expectedOutput4 = "AC\nac\nA\na\n";
            assertEquals(expectedOutput4, actualOutput4);
        } catch (AbstractApplicationException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    @Test
    void sortFromStdin_FirstWordNumberAndNoReverseOrder_ShouldPass() throws Exception {
        String inputText = "10" + STRING_NEWLINE + "15" + STRING_NEWLINE + "13";
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        String actualOutput = sortApplication.sortFromStdin(true, false, false, inputStream);
        String expectedOutput = "10" + STRING_NEWLINE + "13" + STRING_NEWLINE + "15" + STRING_NEWLINE;
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void sortFromStdin_firstWordNumberReverseOrderCaseIndependent_ShouldPass() throws Exception {
        String inputText = "10" + STRING_NEWLINE + "15" + STRING_NEWLINE + "13";
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        String actualOutput = sortApplication.sortFromStdin(true, true, true, inputStream);
        String expectedOutput = "15" + STRING_NEWLINE + "13" + STRING_NEWLINE + "10" + STRING_NEWLINE;
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void sortFromStdin_letters_ShouldPass() {
        String inputText = "A" + STRING_NEWLINE + "b" + STRING_NEWLINE + "c" + STRING_NEWLINE + "D";
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        String expectedOutput = "A" + STRING_NEWLINE + "D" + STRING_NEWLINE + "b" + STRING_NEWLINE + "c" + STRING_NEWLINE;
        assertDoesNotThrow(() -> {
            String actualOutput = sortApplication.sortFromStdin(false, false, false, inputStream);
            assertEquals(expectedOutput, actualOutput);
        });
    }

    private static void emptyFolder(File folder, boolean deleteFolder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        emptyFolder(file, true);
                    } else {
                        file.delete();
                    }
                }
            }
            if (deleteFolder) {
                folder.delete();
            }
        }
    }
}