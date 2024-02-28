package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.app.SortInterface;
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
    static String path = TEST_DIRECTORY + file;
    static String path2 = TEST_DIRECTORY + file2;

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

    @BeforeEach
    void init() {
        // Clean up test folder
        sg.edu.nus.comp.cs4218.Environment.currentDirectory = TEST_DIRECTORY;
        File folder = new File(TEST_DIRECTORY);
        emptyFolder(folder, false);
    }

    @AfterAll
    static void tearDown() {
        sg.edu.nus.comp.cs4218.Environment.currentDirectory = ROOT_DIRECTORY;
        File folder = new File(TEST_DIRECTORY);
        emptyFolder(folder, true);
    }

    @Test
    void sortFromFiles_firstWordNumberNotReverseOrderNotCaseIndependent_returnsLines() throws Exception {
        String output = sortApplication.sortFromFiles(true, false, false, path);
        assertEquals("1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "10", output);

    }

    @Test
    void sortFromFiles_notFirstWordNumberReverseOrderNotCaseIndependent_returnsLines() throws Exception {
        String output = sortApplication.sortFromFiles(false, true, false, path);
        assertEquals("2" + STRING_NEWLINE + "10" + STRING_NEWLINE + "1", output);

    }

    @Test
    void testRunWithInvalidArgumentsShouldThrow() {
        String inputString = "ac" + STRING_NEWLINE + "a" + STRING_NEWLINE + "A" + STRING_NEWLINE + "AC";
        InputStream input = new ByteArrayInputStream(inputString.getBytes());
        String[] args = new String[]{"-z"};
        assertThrows(SortException.class, () -> sortApplication.run(args, input, System.out));
    }

    @Test
    void testRunWithEmptyArgumentsShouldPass() {
        String inputData = "ac" + STRING_NEWLINE + "a" + STRING_NEWLINE + "A" + STRING_NEWLINE + "AC";
        InputStream inputStream = new ByteArrayInputStream(inputData.getBytes());
        String[] arguments = new String[]{};
        assertDoesNotThrow(() -> sortApplication.run(arguments, inputStream, System.out));
    }


    @Test
    void testSortFromStdinWithNumericFirstWordAndNoReverseOrderAndNotCaseIndependent() throws Exception {
        String inputText = "10" + STRING_NEWLINE + "15" + STRING_NEWLINE + "13";
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        String actualOutput = sortApplication.sortFromStdin(true, false, false, inputStream);
        String expectedOutput = "10" + STRING_NEWLINE + "13" + STRING_NEWLINE + "15";
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void sortFromStdin_firstWordNumberReverseOrderCaseIndependent_returnsLines() throws Exception {
        String inputText = "10" + STRING_NEWLINE + "15" + STRING_NEWLINE + "13";
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        String actualOutput = sortApplication.sortFromStdin(true, true, true, inputStream);
        String expectedOutput = "15" + STRING_NEWLINE + "13" + STRING_NEWLINE + "10";
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void sortFromStdin_letters() {
        String inputText = "A" + STRING_NEWLINE + "b" + STRING_NEWLINE + "c" + STRING_NEWLINE + "D";
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        String expectedOutput = "A" + STRING_NEWLINE + "D" + STRING_NEWLINE + "b" + STRING_NEWLINE + "c";
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