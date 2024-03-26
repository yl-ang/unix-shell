package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static external_tests.integration_tests.sg.edu.nus.comp.cs4218.impl.app.CatApplicationPublicIT.ERR_NO_SUCH_FILE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("PMD.LongVariable") // Testing Purpose for clarity
public class PasteApplicationTest {
    private static final String ERR_NO_SUCH_FILE = "paste: %s: No such file or directory";
    private static final String STR_RESULT_1 = "A\nB\nC\nD\n";
    private static final String STR_RESULT_2 = "1\n2\n3\n4\n";
    private static final String FILE_FILE_A = "fileA.txt";
    private static final String FILE_FILE_B = "fileB.txt";
    @Mock
    private PasteArgsParser pasteArgsParser;

    @Spy
    @InjectMocks
    private PasteApplication pasteApplication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // Merge two files A.txt and B.txt (lines from the two files will be merged and separated by TAB)
    @Test
    void mergeFile_Parallel_ShouldBeSuccess() throws AbstractApplicationException {
        // Given
        String inputA = STR_RESULT_1;
        String inputB = STR_RESULT_2;

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
             MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
            // Mocking input streams for fileA and fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_FILE_A)).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_FILE_B)).thenReturn(inputStreamB);

            // Mocking lines obtained from input streams
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // Mocking Paths.get() to return mocked Path instances
            Path pathToFileMockA = mock(Path.class);
            Path pathToFileMockB = mock(Path.class);
            pathsMockedStatic.when(() -> Paths.get(FILE_FILE_A)).thenReturn(pathToFileMockA);
            pathsMockedStatic.when(() -> Paths.get(FILE_FILE_B)).thenReturn(pathToFileMockB);

            // Mocking behavior of Path.toFile() for each mocked Path instance
            File fileMockA = mock(File.class);
            File fileMockB = mock(File.class);
            when(pathToFileMockA.toFile()).thenReturn(fileMockA);
            when(pathToFileMockB.toFile()).thenReturn(fileMockB);

            // Mocking behavior of isAbsolute, node.exists(), node.isDirectory(), and node.canRead()
            when(pathToFileMockA.isAbsolute()).thenReturn(true);
            when(fileMockA.exists()).thenReturn(true);
            when(fileMockA.isDirectory()).thenReturn(false);
            when(fileMockA.canRead()).thenReturn(true);

            when(pathToFileMockB.isAbsolute()).thenReturn(true);
            when(fileMockB.exists()).thenReturn(true);
            when(fileMockB.isDirectory()).thenReturn(false);
            when(fileMockB.canRead()).thenReturn(true);

            // WHEN
            String result = pasteApplication.mergeFile(false, FILE_FILE_A, FILE_FILE_B);

            // THEN
            String expectedOutput = "A\t1\nB\t2\nC\t3\nD\t4";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge two files A.txt and B.txt with -s (serial) flag, Example 2 from 9.9
    @Test
    void mergeFile_Serial_ShouldBeSuccess() throws AbstractApplicationException {
        // GIVEN
        String inputA = STR_RESULT_1;
        String inputB = STR_RESULT_2;

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
             MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
            // Mocking input streams for fileA and fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_FILE_A)).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_FILE_B)).thenReturn(inputStreamB);

            // Mocking lines obtained from input streams
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // Mocking Paths.get() to return mocked Path instances
            Path pathToFileMockA = mock(Path.class);
            Path pathToFileMockB = mock(Path.class);
            pathsMockedStatic.when(() -> Paths.get(FILE_FILE_A)).thenReturn(pathToFileMockA);
            pathsMockedStatic.when(() -> Paths.get(FILE_FILE_B)).thenReturn(pathToFileMockB);

            // Mocking behavior of Path.toFile() for each mocked Path instance
            File fileMockA = mock(File.class);
            File fileMockB = mock(File.class);
            when(pathToFileMockA.toFile()).thenReturn(fileMockA);
            when(pathToFileMockB.toFile()).thenReturn(fileMockB);

            // Mocking behavior of isAbsolute, node.exists(), node.isDirectory(), and node.canRead()
            when(pathToFileMockA.isAbsolute()).thenReturn(true);
            when(fileMockA.exists()).thenReturn(true);
            when(fileMockA.isDirectory()).thenReturn(false);
            when(fileMockA.canRead()).thenReturn(true);

            when(pathToFileMockB.isAbsolute()).thenReturn(true);
            when(fileMockB.exists()).thenReturn(true);
            when(fileMockB.isDirectory()).thenReturn(false);
            when(fileMockB.canRead()).thenReturn(true);

            // WHEN
            String result = pasteApplication.mergeFile(true, FILE_FILE_A, FILE_FILE_B);

            // THEN
            String expectedOutput = "A\tB\tC\tD\n1\t2\t3\t4";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge stdin, A.txt, stdin (stdin is B.txt), Example 3 from 9.9
    @Test
    void mergeStdinFileStdin_Parallel_ShouldBeSuccess() throws Exception {
        // GIVEN
        String inputA = STR_RESULT_1;
        String inputB = STR_RESULT_2;

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
             MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
            // Mocking input streams for fileA, fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_FILE_A)).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // Mocking Paths.get() to return mocked Path instances
            Path pathToFileMockA = mock(Path.class);
            pathsMockedStatic.when(() -> Paths.get(FILE_FILE_A)).thenReturn(pathToFileMockA);

            // Mocking behavior of Path.toFile() for each mocked Path instance
            File fileMockA = mock(File.class);
            when(pathToFileMockA.toFile()).thenReturn(fileMockA);

            // Mocking behavior of isAbsolute, node.exists(), node.isDirectory(), and node.canRead()
            when(pathToFileMockA.isAbsolute()).thenReturn(true);
            when(fileMockA.exists()).thenReturn(true);
            when(fileMockA.isDirectory()).thenReturn(false);
            when(fileMockA.canRead()).thenReturn(true);

            // WHEN
            String result = pasteApplication.mergeFileAndStdin(false, inputStreamB, "-", FILE_FILE_A, "-");

            // THEN
            String expectedOutput = "1\tA\t2\n3\tB\t4\n\tC\t\n\tD\t";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge stdin, A.txt, stdin (stdin is B.txt), Example 3 from 9.9
    @Test
    void mergeFileAndStdin_Serial_ShouldBeSuccess() throws Exception {
        // GIVEN
        String inputA = STR_RESULT_1;
        String inputB = STR_RESULT_2;

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
            MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {

            // Mocking input streams for fileA, fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_FILE_A)).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // Mocking Paths.get() to return mocked Path instances
            Path pathToFileMockA = mock(Path.class);
            pathsMockedStatic.when(() -> Paths.get(FILE_FILE_A)).thenReturn(pathToFileMockA);

            // Mocking behavior of Path.toFile() for each mocked Path instance
            File fileMockA = mock(File.class);
            when(pathToFileMockA.toFile()).thenReturn(fileMockA);

            // Mocking behavior of isAbsolute, node.exists(), node.isDirectory(), and node.canRead()
            when(pathToFileMockA.isAbsolute()).thenReturn(true);
            when(fileMockA.exists()).thenReturn(true);
            when(fileMockA.isDirectory()).thenReturn(false);
            when(fileMockA.canRead()).thenReturn(true);

            // WHEN
            String result = pasteApplication.mergeFileAndStdin(true, inputStreamB, "-", FILE_FILE_A, "-");

            // THEN
            String expectedOutput = "1\t2\t3\t4\nA\tB\tC\tD";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge stdin input
    @Test
    void mergeStdin_Serial_ShouldBeSuccess() throws Exception {
        // GIVEN
        String inputA = STR_RESULT_1;
        InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));

            // WHEN
            String result = pasteApplication.mergeStdin(true, inputStreamA);

            // THEN
            String expectedOutput = "A\tB\tC\tD";
            assertEquals(expectedOutput, result);
        }
    }

    @Test
    void run_mergeFileAndStdin_Success() throws AbstractApplicationException {
        // GIVEN
        String[] args = {FILE_FILE_A, "-"};
        String inputB = STR_RESULT_2;
        InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Mock PasteArgsParser
        when(pasteArgsParser.getFileNames()).thenReturn(List.of(FILE_FILE_A));

        // Mock PasteApplication methods
        doReturn("A\t1\nB\t2\nC\t3\nD\t4").when(pasteApplication)
                .mergeFileAndStdin(anyBoolean(), any(InputStream.class), anyString(), anyString());

        // WHEN
        pasteApplication.run(args, inputStreamB, outputStream);

        // THEN
        String expectedOutput = "A\t1\nB\t2\nC\t3\nD\t4\n";
        assertEquals(expectedOutput, outputStream.toString());

        //VERIFY
        verify(pasteApplication, times(1)).mergeFileAndStdin(anyBoolean(), any(InputStream.class), anyString(), anyString());
    }

    @Test
    void run_mergeFile_Success() throws AbstractApplicationException {
        // GIVEN
        String[] args = {FILE_FILE_A, FILE_FILE_B};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Mock PasteArgsParser
        when(pasteArgsParser.getFileNames()).thenReturn(List.of(FILE_FILE_A, FILE_FILE_B));

        // Mock PasteApplication methods
        doReturn("A\t1\nB\t2\nC\t3\nD\t4").when(pasteApplication)
                .mergeFile(eq(false), eq(FILE_FILE_A), eq(FILE_FILE_B));

        // WHEN
        pasteApplication.run(args, null, outputStream);

        // THEN
        String expectedOutput = "A\t1\nB\t2\nC\t3\nD\t4\n";
        assertEquals(expectedOutput, outputStream.toString());

        //VERIFY
        verify(pasteApplication, times(1)).mergeFile(anyBoolean(),  anyString(), anyString());
    }

    @Test
    void run_mergeStdin_Success() throws AbstractApplicationException {
        // GIVEN
        String[] args = {};
        String input = STR_RESULT_1;
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Mock PasteArgsParser
        when(pasteArgsParser.getFileNames()).thenReturn(List.of("-"));

        // Mock PasteApplication methods
        doReturn("A\nB\nC\nD").when(pasteApplication)
                .mergeStdin(eq(false), any(InputStream.class));

        // WHEN
        pasteApplication.run(args, inputStream, outputStream);

        // THEN
        String expectedOutput = STR_RESULT_1;
        assertEquals(expectedOutput, outputStream.toString());

        //VERIFY
        verify(pasteApplication, times(1)).mergeStdin(anyBoolean(), any(InputStream.class));
    }

    @Test
    void mergeFileAndStdin_Serial_ShouldThrowException() throws Exception {
        // GIVEN
        String nonexistentFileName = "nonexistent_file.txt";
        String inputB = STR_RESULT_2;

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
             MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {

            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking input streamB
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // Mocking Paths.get() to return mocked Path instances
            Path pathToFileMockA = mock(Path.class);
            pathsMockedStatic.when(() -> Paths.get(nonexistentFileName)).thenReturn(pathToFileMockA);
            when(pathToFileMockA.getFileName()).thenReturn(Path.of(nonexistentFileName));

            // Mocking node.exists() to be false
            File fileMockA = mock(File.class);
            when(pathToFileMockA.toFile()).thenReturn(fileMockA);
            when(pathToFileMockA.isAbsolute()).thenReturn(true);
            when(fileMockA.exists()).thenReturn(false);

            // WHEN
            Exception exception = assertThrows(Exception.class, () ->
                    pasteApplication.mergeFileAndStdin(true, inputStreamB, "-", nonexistentFileName, "-"));

            // THEN
            assertInstanceOf(PasteException.class, exception);
            assertEquals(String.format(ERR_NO_SUCH_FILE, nonexistentFileName), exception.getMessage());
        }
    }
}
