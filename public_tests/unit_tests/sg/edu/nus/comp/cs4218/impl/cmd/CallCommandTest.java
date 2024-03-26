package unit_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

@SuppressWarnings("PMD.LongVariable") // Testing Purpose for clarity
public class CallCommandTest {

    private static CallCommand callCommand;

    @Mock
    private ArgumentResolver argumentResolver;

    @Mock
    private ApplicationRunner applicationRunner;

    @Mock
    private InputStream inputStream;

    @Mock
    OutputStream outputStream;

    private static final List<String> INVALID_EMPTY_ARGS_LIST = new ArrayList<>();
    private static final String VALID_COMMAND_ECHO_STR = "echo";
    private static final String VALID_COMMAND_ECHO_INPUT_STR = "test";
    private static final List<String> VALID_ARGS_LIST = Arrays.asList(VALID_COMMAND_ECHO_STR, VALID_COMMAND_ECHO_INPUT_STR);
    private static final String[] VALID_COMMAND_INPUT_ARR = new String[]{VALID_COMMAND_ECHO_INPUT_STR};

    @BeforeEach
    void setupTest() {
        argumentResolver = mock(ArgumentResolver.class);
        applicationRunner = mock(ApplicationRunner.class);
        inputStream = mock(InputStream.class);
        outputStream = mock(OutputStream.class);
    }

    @Test
    public void evaluate_NullArgsList_ShouldThrowException() throws ShellException {
        callCommand = new CallCommand(null, applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () ->  callCommand.evaluate(inputStream, outputStream));
        assertEquals( new ShellException(ERR_SYNTAX).getMessage(), exception.getMessage());
    }

    @Test
    public void evaluate_EmptyArgsList_ShouldThrowException() throws ShellException {
        callCommand = new CallCommand(INVALID_EMPTY_ARGS_LIST, applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () ->  callCommand.evaluate(inputStream, outputStream));
        assertEquals( new ShellException(ERR_SYNTAX).getMessage(), exception.getMessage());
    }

    @Test
    public void evaluate_ValidArgsList_ShouldRunAppRunner() throws FileNotFoundException, AbstractApplicationException, ShellException {
        callCommand = new CallCommand(VALID_ARGS_LIST, applicationRunner, argumentResolver);
        when(argumentResolver.parseArguments(VALID_ARGS_LIST)).thenReturn(new ArrayList<>(VALID_ARGS_LIST));
        callCommand.evaluate(inputStream, outputStream);
        verify(applicationRunner).runApp(VALID_COMMAND_ECHO_STR, VALID_COMMAND_INPUT_ARR, inputStream, outputStream);
    }
    @Test
    public void evaluate_ValidArgsList_ShouldNotRunAppRunner() throws FileNotFoundException, AbstractApplicationException, ShellException {
        callCommand = new CallCommand(VALID_ARGS_LIST, applicationRunner, argumentResolver);
        when(argumentResolver.parseArguments(VALID_ARGS_LIST)).thenReturn(new ArrayList<>());
        callCommand.evaluate(inputStream, outputStream);
        verify(applicationRunner, never()).runApp(VALID_COMMAND_ECHO_STR, VALID_COMMAND_INPUT_ARR, inputStream, outputStream);
    }
}