package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class PasteApplicationTest {
    @Mock
    private InputStream mockFileA;

    @Mock
    private InputStream mockFileB;

    @Mock
    private IOUtils ioUtils;

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
    void paste_MergeTwoFilesParallel_Success() throws AbstractApplicationException, IOException {
        // Given
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";

        try (MockedStatic<IOUtils> mockedStatic = Mockito.mockStatic(IOUtils.class)) {
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
            String expectedOutput = "A\t1\nB\t2\nC\t3\nD\t4\n";
            assertEquals(expectedOutput, result);
        }
    }


    // # Merge two files A.txt and B.txt with -s (serial) flag, Example 2 from 9.9
    @Test
    void paste_MergeTwoFilesSerial_Success() throws AbstractApplicationException, IOException {
        // GIVEN
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";
        when(pasteApplication.mergeFile(anyBoolean(), anyString())).thenReturn("");
        when(pasteApplication.mergeFile(anyBoolean(), anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            InputStream fileA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
            InputStream fileB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));
            return pasteApplication.mergeFile((Boolean) args[0], "fileA.txt", "fileB.txt");
        });

        // WHEN
        pasteApplication.run(new String[]{"-s", "fileA.txt", "fileB.txt"}, null, stdout);

        // THEN
        String expectedOutput = "A\tB\tC\tD\t\n1\t2\t3\t4\t\n";
        assertEquals(expectedOutput, stdout.toString());
    }

    // # Merge stdin, A.txt, stdin (stdin is B.txt), Example 3 from 9.9
    @Test
    void paste_MergeStdinFileStdin_Success() throws Exception {
        // GIVEN
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";
        String stdinInput = "20\nC\nD\n3\n4\n";

        when(pasteApplication.mergeFileAndStdin(anyBoolean(), any(InputStream.class), anyString()))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    InputStream fileA = new ByteArrayInputStream(inputA.getBytes(StandardCharsets.UTF_8));
                    InputStream fileB = new ByteArrayInputStream(inputB.getBytes(StandardCharsets.UTF_8));
                    InputStream stdinStream = new ByteArrayInputStream(stdinInput.getBytes(StandardCharsets.UTF_8));
                    return pasteApplication.mergeFileAndStdin((Boolean) args[0], stdinStream, "fileA.txt",
                            "fileB.txt");
                });

        // WHEN
        pasteApplication.run(new String[]{"-", "fileA.txt", "-"}, System.in, stdout);

        // THEN
        String expectedOutput = "A\tB\t20\t\n1\t2\tC\t\n\t\tD\t\n3\t4\t3\t\n4\t\t4\t\n";
        assertEquals(expectedOutput, stdout.toString());
    }
}
