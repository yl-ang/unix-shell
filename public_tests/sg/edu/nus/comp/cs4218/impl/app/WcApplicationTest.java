package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

class WcApplicationTest {
    static String testFileName = "wcTestFile.txt";
    static String emptyTestFileName = "wcEmptyTestFile.txt";
    static FileWriter myWriter;
    static FileWriter myWriter2;

    static WcApplication wcApplication;

    @BeforeAll
    static void setUp() throws IOException {
        try {
            myWriter = new FileWriter(testFileName);
            myWriter.write("Test txt file for wc\n");
            myWriter.write("Second line");
            myWriter.close();

            myWriter2 = new FileWriter(emptyTestFileName);
            myWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        wcApplication = new WcApplication();
    }

    /*
     * null stream
     * normal report
     * file empty
     * ioexception file too big?
     * */

    @Test
    void getCountReport_inputNull_throwException() throws AbstractApplicationException {
        InputStream input = null;

        WcException exception = assertThrows(WcException.class, () -> wcApplication.getCountReport(input));
        assertEquals(new WcException(ERR_NULL_STREAMS).getMessage(), exception.getMessage());
    }

    @Test
    void getCountReport_inputPresentButEmpty_countArrayWithZeroValues() throws AbstractApplicationException {
        InputStream input = null;
        try {
            input = IOUtils.openInputStream(emptyTestFileName);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
        long[] count = wcApplication.getCountReport(input); // lines words bytes
        try {
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
        long[] expectedCount = {0, 0, 0};
        assertArrayEquals(expectedCount, count);
    }

    @Test
    void getCountReport_inputPresent_countArray() throws AbstractApplicationException {
        InputStream input = null;
        try {
            input = IOUtils.openInputStream(testFileName);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
        long[] count = wcApplication.getCountReport(input); // lines words bytes
        try {
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
        long[] expectedCount = {1, 7, 32};
        assertArrayEquals(expectedCount, count);
    }

    /*one file no arg show all
    one file arg show in seq
    * no file ,exception
    file dont exist, exception
    dir, exception
    no perm, exception
    * many files, no arg show all, show count
    * many files, one arg, show count
    many files, arg show in seq, show count
    non text files
    *
    * */
    @Test
    void countFromFiles_nullFileName_ThrowException() throws WcException {
        try {
            WcException exception = assertThrows(WcException.class, () -> wcApplication.countFromFiles(false, false, false, null));
            assertEquals(new WcException(ERR_GENERAL).getMessage(), exception.getMessage());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_fileDoesNotExist_ThrowException() throws WcException {
        try {
            String fileName = "randomFileName1234.txt";
            WcException exception = assertThrows(WcException.class, () -> wcApplication.countFromFiles(false, false, false, fileName));
            assertEquals(new WcException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_fileNameIsDirectory_ThrowException() throws WcException {
        try {
            String fileName = "production";
            WcException exception = assertThrows(WcException.class, () -> wcApplication.countFromFiles(false, false, false, fileName));
            assertEquals(new WcException(ERR_IS_DIR).getMessage(), exception.getMessage());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_fileNoReadPermission_ThrowException() throws WcException {
        try {
            Set<PosixFilePermission> noReadPermission = PosixFilePermissions.fromString("-wx-wx-wx");
            FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(noReadPermission);
            Path filePath = Paths.get("fileWithNoReadPermissions.txt");
            Files.createFile(filePath, permissions);

            WcException exception = assertThrows(WcException.class, () -> wcApplication.countFromFiles(false, false, false, null));
            assertEquals(new WcException(ERR_NO_PERM).getMessage(), exception.getMessage());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_noFlags_showLinesWordsBytes() throws WcException {
        try {
            String output = wcApplication.countFromFiles(false, false, false, testFileName); // lines words bytes
            assertEquals("       1       7      32 wcTestFile.txt", output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_twoFlags_showFlagsInSequence() throws WcException {
        try {
            String output = wcApplication.countFromFiles(true, true, false, testFileName); // lines words bytes
            assertEquals("       1      32 wcTestFile.txt", output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    /*one file no arg show all
    one file 2 arg show in seq
    * no file ,exception
    * */
    @Test
    void countFromStdin_nullInput_ThrowException() throws WcException {
        InputStream input = null;
        String output;
        try {
            WcException exception = assertThrows(WcException.class, () -> wcApplication.countFromStdin(false, false, false, input));
            assertEquals(new WcException(ERR_NULL_STREAMS).getMessage(), exception.getMessage());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromStdin_noFlags_showLinesWordsBytes() throws WcException {
        InputStream input = null;
        String output;
        try {
            input = IOUtils.openInputStream(testFileName);
            output = wcApplication.countFromStdin(false, false, false, input); // lines words bytes
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
        try {
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
        assertEquals("       1       7      32", output);
    }

    @Test
    void countFromStdin_twoFlags_showFlagsInSequence() throws WcException {
        InputStream input = null;
        String output;
        try {
            input = IOUtils.openInputStream(testFileName);
            output = wcApplication.countFromStdin(false, true, true, input); // lines words bytes
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
        try {
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
        assertEquals("       1       7", output);
    }


    @Test
    @Disabled
    void countFromFileAndStdin() {
    }

    /*
     * no files, take from stdin
     * one file
     * many files, print cum with all
     * many files, 2 flag, print cum with count in sequence
     * */
    @Test
    void run() {
    }


}