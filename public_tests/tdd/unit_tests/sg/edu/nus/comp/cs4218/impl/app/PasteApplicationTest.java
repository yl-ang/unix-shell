package tdd.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.cmd.PasteCommand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PasteCommandTest {

    @Mock
    private InputStream mockFileA;

    @Mock
    private InputStream mockFileB;

    @InjectMocks
    private PasteCommand pasteCommand;

    private ByteArrayOutputStream stdout;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        stdout = new ByteArrayOutputStream();
    }

    // Merge two files A.txt and B.txt (lines from the two files will be merged and separated by
    // TAB), Example 1 from 9.9
    @Test
    void paste_MergeTwoFiles_Success() throws PasteException, IOException {
        // Given
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";
        when(mockFileA.read()).thenReturn(-1);
        when(mockFileB.read()).thenReturn(-1);
        when(mockFileA.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            byte[] bytes = inputA.getBytes(StandardCharsets.UTF_8);
            int len = Math.min(arg.length, bytes.length);
            System.arraycopy(bytes, 0, arg, 0, len);
            return len;
        });
        when(mockFileB.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            byte[] bytes = inputB.getBytes(StandardCharsets.UTF_8);
            int len = Math.min(arg.length, bytes.length);
            System.arraycopy(bytes, 0, arg, 0, len);
            return len;
        });

        // When
        pasteCommand.mergeFiles(mockFileA, mockFileB, stdout);

        // Then
        String expectedOutput = "A\t1\nB\t2\nC\t3\nD\t4\n";
        assertEquals(expectedOutput, stdout.toString());
    }

    // # Merge two files A.txt and B.txt with -s (serial) flag, Example 2 from 9.9
    @Test
    void paste_MergeTwoFilesSerial_Success() throws PasteException, IOException {
        // Given
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";
        when(mockFileA.read()).thenReturn(-1);
        when(mockFileB.read()).thenReturn(-1);
        when(mockFileA.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            byte[] bytes = inputA.getBytes(StandardCharsets.UTF_8);
            int len = Math.min(arg.length, bytes.length);
            System.arraycopy(bytes, 0, arg, 0, len);
            return len;
        });
        when(mockFileB.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            byte[] bytes = inputB.getBytes(StandardCharsets.UTF_8);
            int len = Math.min(arg.length, bytes.length);
            System.arraycopy(bytes, 0, arg, 0, len);
            return len;
        });

        // When
        pasteCommand.mergeFilesSerial(mockFileA, mockFileB, stdout);

        // Then
        String expectedOutput = "A\tB\tC\tD\t\n1\t2\t3\t4\t\n";
        assertEquals(expectedOutput, stdout.toString());
    }

    // # Merge stdin, A.txt, stdin (stdin is B.txt), Example 3 from 9.9
    @Test
    void paste_MergeStdinFileStdin_Success() throws PasteException, IOException {
        // Given
        String inputA = "A\nB\nC\nD\n";
        String inputB = "1\n2\n3\n4\n";
        String stdinInput = "20\nC\nD\n3\n4\n";

        when(mockFileA.read()).thenReturn(-1);
        when(mockFileB.read()).thenReturn(-1);
        when(mockFileA.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            byte[] bytes = inputA.getBytes(StandardCharsets.UTF_8);
            int len = Math.min(arg.length, bytes.length);
            System.arraycopy(bytes, 0, arg, 0, len);
            return len;
        });
        when(mockFileB.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            byte[] bytes = inputB.getBytes(StandardCharsets.UTF_8);
            int len = Math.min(arg.length, bytes.length);
            System.arraycopy(bytes, 0, arg, 0, len);
            return len;
        });

        ByteArrayInputStream stdinStream = new ByteArrayInputStream(stdinInput.getBytes(StandardCharsets.UTF_8));

        // When
        pasteCommand.mergeStdinFileStdin(mockFileA, stdinStream, mockFileB, stdout);

        // Then
        String expectedOutput = "A\tB\t20\t\n1\t2\tC\t\n\t\tD\t\n3\t4\t3\t\n4\t\t4\t\n";
        assertEquals(expectedOutput, stdout.toString());
    }
}
