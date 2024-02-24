package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
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

    @AfterAll
    static void tearDown() throws IOException {
        try {
            Files.deleteIfExists(Paths.get(testFileName));
            Files.deleteIfExists(Paths.get(emptyTestFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * null stream
     * normal report
     * file empty
     * ioexception file too big?
     * */

    @Test
    void getCountReport_inputNull_throwException() throws AbstractApplicationException {
        WcException exception = assertThrows(WcException.class, () -> wcApplication.getCountReport(null));
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
//            String fileName = null;
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
            String output = wcApplication.countFromFiles(false, false, false, fileName); // lines words bytes
            assertEquals("wc: " + ERR_FILE_NOT_FOUND, output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_fileNameIsDirectory_ThrowException() throws WcException {
        try {
            String fileName = "production";
            String output = wcApplication.countFromFiles(false, false, false, fileName); // lines words bytes
            assertEquals("wc: " + ERR_IS_DIR, output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_fileNoReadPermission_ThrowException() throws WcException {
        Path filePath = null;
        try {
            Set<PosixFilePermission> noReadPermission = PosixFilePermissions.fromString("--x--x--x");
            FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(noReadPermission);
            String fileName = "fileWithNoReadPermissions.txt";
            filePath = Paths.get(fileName);
            Files.createFile(filePath, permissions);

            String output = wcApplication.countFromFiles(false, false, false, filePath.toString()); // lines words bytes
            assertEquals("wc: " + ERR_NO_PERM, output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        } finally {
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    void countFromFiles_noFlags_showLinesWordsBytesSeperatedByTab() throws WcException {
        try {
            String output = wcApplication.countFromFiles(false, false, false, testFileName); // lines words bytes
            assertEquals("\t1\t7\t32 wcTestFile.txt", output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_twoFlags_showFlagsInSequenceSeperatedByTab() throws WcException {
        try {
            String output = wcApplication.countFromFiles(true, true, false, testFileName); // lines words bytes
            assertEquals("\t1\t32 wcTestFile.txt", output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_multipleFileNamesNoFlags_showLinesWordsBytesAndTotalSeperatedByTab() throws WcException {
        try {
            String[] fileNames = {testFileName, testFileName};
            String output = wcApplication.countFromFiles(false, false, false, fileNames); // lines words bytes
            assertEquals("\t1\t7\t32 wcTestFile.txt\n\t1\t7\t32 wcTestFile.txt\n\t2\t14\t64 total", output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_multipleFileNamesTwoFlags_showFlagsInSequenceAndTotalSeperatedByTab() throws WcException {
        try {
            String[] fileNames = {testFileName, testFileName};
            String output = wcApplication.countFromFiles(false, true, true, fileNames); // lines words bytes
            assertEquals("\t1\t7 wcTestFile.txt\n\t1\t7 wcTestFile.txt\n\t2\t14 total", output);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_jsonFileNoFlags_showLinesWordsBytesSeperatedByTab() throws WcException {
        String jsonFileName = "wcTestFile.json";
        Path path = Paths.get(jsonFileName);
        String jsonContent = """
                {
                  "name": "John Doe",
                  "age": 30,
                  "city": "New York"
                }""";
        try {
            Files.writeString(path, jsonContent);
            String output = wcApplication.countFromFiles(false, false, false, jsonFileName); // lines words bytes
            assertEquals("\t4\t10\t59 wcTestFile.json", output);
            Files.deleteIfExists(path);
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void countFromFiles_xmlFileNoFlags_showLinesWordsBytesSeperatedByTab() throws WcException {
        String xmlFileName = "wcTestFile.xml";
        Path path = Paths.get(xmlFileName);
        String xmlContent = """
                <?xml version="1.0"?>
                <user>
                  <name>John Doe</name>
                  <age>30</age>
                  <city>New York</city>
                </user>""";
        try {
            Files.writeString(path, xmlContent);
            String output = wcApplication.countFromFiles(false, false, false, xmlFileName); // lines words bytes
            assertEquals("\t5\t9\t100 wcTestFile.xml", output);
            Files.deleteIfExists(path);
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
    void countFromStdin_noFlags_showLinesWordsBytesSeperatedByTab() throws WcException {
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
        assertEquals("\t1\t7\t32", output);
    }

    @Test
    void countFromStdin_twoFlags_showFlagsInSequenceSeperatedByTab() throws WcException {
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
        assertEquals("\t1\t7", output);
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
    void run_noArgsGiven_countFromStdin() throws WcException {
        InputStream input = null;
        OutputStream output = new ByteArrayOutputStream();
        try {
            input = IOUtils.openInputStream(testFileName);
            String[] args = {};
            wcApplication.run(args, input, output); // lines words bytes
            assertEquals("\t1\t7\t32\n", output.toString());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }

        try {
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void run_fileNameGivenWithArgsTogether_countFromFileWithFlagsInSequence() throws WcException {
        String inputData = "Hello World\nThis is a test\n";
        InputStream input = new ByteArrayInputStream(inputData.getBytes());
        OutputStream output = new ByteArrayOutputStream();
        try {
            String[] args = {"-cl", testFileName};
            wcApplication.run(args, input, output); // lines words bytes
            assertEquals("\t1\t32 wcTestFile.txt\n", output.toString());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }

        try {
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
    }

    @Test
    void run_multipleFileNamesGiven_countFromFilesWithTotal() throws WcException {
        InputStream input = null;
        OutputStream output = new ByteArrayOutputStream();
        try {
            input = IOUtils.openInputStream(testFileName);
            String[] args = {testFileName, testFileName};
            wcApplication.run(args, input, output); // lines words bytes
            assertEquals("\t1\t7\t32 wcTestFile.txt\n\t1\t7\t32 wcTestFile.txt\n\t2\t14\t64 total\n", output.toString());
        } catch (Exception e) {
            throw new WcException(e.getMessage());
        }

        try {
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new WcException(e.getMessage());
        }
    }


}