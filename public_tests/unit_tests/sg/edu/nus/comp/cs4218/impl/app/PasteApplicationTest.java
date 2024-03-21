package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplicationTest {
    @Mock
    private InputStream mockFileA;

    @Mock
    private InputStream mockFileB;

    @Mock
    private IOUtils ioUtils;

    @Mock
    private PasteArgsParser pasteArgsParser;

    @Spy
    @InjectMocks
    private PasteApplication pasteApplication;

    private ByteArrayOutputStream stdout;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        stdout = new ByteArrayOutputStream();
    }

    // Merge two files A.txt and B.txt (lines from the two files will be merged and separated by TAB)
    @Test
    void mergeFile_Parallel_ShouldBeSuccess() throws AbstractApplicationException, IOException {
        // Given
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            // Mocking input streams for fileA and fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream("fileA.txt")).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.openInputStream("fileB.txt")).thenReturn(inputStreamB);

            // Mocking lines obtained from input streams
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // WHEN
            String result = pasteApplication.mergeFile(false, "fileA.txt", "fileB.txt");

            // THEN
            String expectedOutput = "A\t1\nB\t2\nC\t3\nD\t4";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge two files A.txt and B.txt with -s (serial) flag, Example 2 from 9.9
    @Test
    void mergeFile_Serial_ShouldBeSuccess() throws AbstractApplicationException, IOException {
        // GIVEN
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            // Mocking input streams for fileA and fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream("fileA.txt")).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.openInputStream("fileB.txt")).thenReturn(inputStreamB);

            // Mocking lines obtained from input streams
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // WHEN
            String result = pasteApplication.mergeFile(true, "fileA.txt", "fileB.txt");

            // THEN
            String expectedOutput = "A\tB\tC\tD\n1\t2\t3\t4";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge stdin, A.txt, stdin (stdin is B.txt), Example 3 from 9.9
    @Test
    void mergeStdinFileStdin_Parallel_ShouldBeSuccess() throws Exception {
        // GIVEN
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            // Mocking input streams for fileA, fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream("fileA.txt")).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // WHEN
            String result = pasteApplication.mergeFileAndStdin(false, inputStreamB, "-", "fileA.txt", "-");

            // THEN
            String expectedOutput = "1\tA\t2\n3\tB\t4\n \tC\t \n \tD\t ";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge stdin, A.txt, stdin (stdin is B.txt), Example 3 from 9.9
    @Test
    void mergeFileAndStdin_Serial_ShouldBeSuccess() throws Exception {
        // GIVEN
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";

        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            // Mocking input streams for fileA, fileB
            InputStream inputStreamA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));

            // Mocking opening of files: A.txt and B.txt
            mockedStatic.when(() -> IOUtils.openInputStream("fileA.txt")).thenReturn(inputStreamA);
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamA)).thenReturn(Arrays.asList("A", "B", "C", "D"));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStreamB)).thenReturn(Arrays.asList("1", "2", "3", "4"));

            // WHEN
            String result = pasteApplication.mergeFileAndStdin(true, inputStreamB, "-", "fileA.txt", "-");

            // THEN
            String expectedOutput = "1\t2\t3\t4\nA\tB\tC\tD";
            assertEquals(expectedOutput, result);
        }
    }

    // # Merge stdin input
    @Test
    void mergeStdin_Serial_ShouldBeSuccess() throws Exception {
        // GIVEN
        String inputA = "A\nB\nC\nD\n";
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
        String[] args = {"fileA.txt", "-"};
        String inputB = "1\n2\n3\n4\n";
        InputStream inputStreamB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Mock PasteArgsParser
        when(pasteArgsParser.getFileNames()).thenReturn(List.of("fileA.txt"));

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
        String[] args = {"fileA.txt", "fileB.txt"};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Mock PasteArgsParser
        when(pasteArgsParser.getFileNames()).thenReturn(List.of("fileA.txt", "fileB.txt"));

        // Mock PasteApplication methods
        doReturn("A\t1\nB\t2\nC\t3\nD\t4").when(pasteApplication)
                .mergeFile(eq(false), eq("fileA.txt"), eq("fileB.txt"));

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
        String input = "A\nB\nC\nD\n";
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
        String expectedOutput = "A\nB\nC\nD\n";
        assertEquals(expectedOutput, outputStream.toString());

        //VERIFY
        verify(pasteApplication, times(1)).mergeStdin(anyBoolean(), any(InputStream.class));
    }
}
