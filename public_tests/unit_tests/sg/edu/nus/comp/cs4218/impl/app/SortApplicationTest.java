package public_tests.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.exception.SortException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
    private static final String TEST_FOLDER_NAME = "tmpSortTestFolder" + File.separator;
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
        Files.write(Paths.get(path), ("10" + STRING_NEWLINE + "1" + STRING_NEWLINE + "2").getBytes(), APPEND);
        Files.write(Paths.get(path2), ("a" + STRING_NEWLINE + "A" + STRING_NEWLINE + "ab" + STRING_NEWLINE + "AB").getBytes(), APPEND);
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

    @org.junit.jupiter.api.Test
    void run() {
    }

    @org.junit.jupiter.api.Test
    void sortFromFiles() {
    }

    @org.junit.jupiter.api.Test
    void sortFromStdin() {
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