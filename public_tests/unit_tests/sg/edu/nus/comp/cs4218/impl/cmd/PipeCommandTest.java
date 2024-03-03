package unit_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PipeCommandTest {

    @Mock
    private CallCommand callCommand1;

    @Mock
    private CallCommand callCommand2;

    private PipeCommand pipeCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        pipeCommand = new PipeCommand(Arrays.asList(callCommand1, callCommand2));
    }

    @Test
    void evaluate_ShouldEvaluateCommandsInPipeline() throws Exception {
        // GIVEN
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream stdout = new ByteArrayOutputStream();

        // Call Command's evaluate (void method mocking)
        doAnswer(invocation -> {
            OutputStream outputStream = (OutputStream) invocation.getArguments()[1];
            outputStream.write("test.txt".getBytes());
            return null;
        }).when(callCommand1).evaluate(any(InputStream.class), any(OutputStream.class));

        doAnswer(invocation -> {
            OutputStream outputStream = (OutputStream) invocation.getArguments()[1];
            outputStream.write("test.txt".getBytes());
            return null;
        }).when(callCommand2).evaluate(any(InputStream.class), any(OutputStream.class));

        // WHEN
        pipeCommand.evaluate(stdin, stdout);

        // THEN
        verify(callCommand1).evaluate(eq(stdin), any(ByteArrayOutputStream.class));
        verify(callCommand2).evaluate(any(ByteArrayInputStream.class), eq(stdout));
    }

    @Test
    void evaluate_ShouldThrowShellExceptionIfAnyCommandThrowsShellException() throws Exception {
        // GIVEN
        InputStream stdin = new ByteArrayInputStream("input".getBytes());
        OutputStream stdout = new ByteArrayOutputStream();

        // WHEN
        doThrow(new ShellException("ShellException")).when(callCommand1).evaluate(any(), any());

        // THEN
        assertThrows(ShellException.class, () -> pipeCommand.evaluate(stdin, stdout));
        verify(callCommand2, never()).evaluate(any(), any());
    }
}
