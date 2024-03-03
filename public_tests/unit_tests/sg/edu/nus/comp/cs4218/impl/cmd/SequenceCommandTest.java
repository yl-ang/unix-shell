package unit_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class SequenceCommandTest {
    private ArrayList<Command> listOfCommands;
    private OutputStream outputStream;

    @Mock
    private Command mockCommandOne;
    @Mock
    private Command mockCommandTwo;
    @Mock
    private Command mockCommandThree;
    @Mock
    private Command mockCommandFour;
    @Mock
    private Command mockCommandFive;

    @InjectMocks
    SequenceCommand sequenceCommand;

    @BeforeEach
    void setUp() {
        listOfCommands = new ArrayList<>();
        outputStream = new ByteArrayOutputStream();
        mockCommandOne = mock(CallCommand.class);
        mockCommandTwo = mock(CallCommand.class);
        mockCommandThree = mock(CallCommand.class);
        mockCommandFour = mock(CallCommand.class);
        mockCommandFive = mock(CallCommand.class);
    }

    @Test
    void evaluate_GivenSingleValidCommand_ShouldEvaluateCommand() {
        //GIVEN / WHEN
        listOfCommands.add(mockCommandOne);
        sequenceCommand = new SequenceCommand(listOfCommands);

        //THEN
        assertDoesNotThrow(() -> {
            sequenceCommand.evaluate(System.in, System.out);
            verify(mockCommandOne).evaluate(any(InputStream.class), any(OutputStream.class));
        });
    }

    @Test
    void evaluate_GivenMultipleValidCommands_ShouldEvaluateAllCommands() {
        //GIVEN / WHEN
        listOfCommands.addAll(Arrays.asList(mockCommandOne, mockCommandFive, mockCommandThree));
        sequenceCommand = new SequenceCommand(listOfCommands);

        //THEN
        assertDoesNotThrow(() -> {
            sequenceCommand.evaluate(System.in, System.out);
            verify(mockCommandOne).evaluate(any(InputStream.class), any(OutputStream.class));
            verify(mockCommandFive).evaluate(any(InputStream.class), any(OutputStream.class));
            verify(mockCommandThree).evaluate(any(InputStream.class), any(OutputStream.class));
        });
    }

    @Test
    void evaluate_GivenInvalidStartCommand_ShouldThrowExceptionStartCommandAndContinue() throws ShellException, AbstractApplicationException, FileNotFoundException {
        //GIVEN / WHEN
        doThrow(ShellException.class).when(mockCommandOne).evaluate(any(InputStream.class), any(OutputStream.class));
        listOfCommands.addAll(Arrays.asList(mockCommandOne, mockCommandTwo, mockCommandFour));
        sequenceCommand = new SequenceCommand(listOfCommands);
        sequenceCommand.evaluate(System.in, outputStream);

        //THEN
        String expectedOutput = "null" + STRING_NEWLINE;
        assertEquals(outputStream.toString(), expectedOutput);
        assertDoesNotThrow(() -> verify(mockCommandTwo).evaluate(any(InputStream.class), any(OutputStream.class)));
        assertDoesNotThrow(() -> verify(mockCommandFour).evaluate(any(InputStream.class), any(OutputStream.class)));
    }

    @Test
    void evaluate_GivenInvalidCenterCommand_ShouldRunAllThrowExceptionCenterCommand() throws ShellException, AbstractApplicationException, FileNotFoundException {
        //GIVEN / WHEN
        doThrow(ShellException.class).when(mockCommandTwo).evaluate(any(InputStream.class), any(OutputStream.class));
        listOfCommands.addAll(Arrays.asList(mockCommandOne, mockCommandTwo, mockCommandFive));
        sequenceCommand = new SequenceCommand(listOfCommands);
        sequenceCommand.evaluate(System.in, outputStream);

        //THEN
        String expectedOutput = "null" + STRING_NEWLINE;
        assertEquals(outputStream.toString(), expectedOutput);
        assertDoesNotThrow(() -> verify(mockCommandOne).evaluate(any(InputStream.class), any(OutputStream.class)));
        assertDoesNotThrow(() -> verify(mockCommandFive).evaluate(any(InputStream.class), any(OutputStream.class)));
    }

    @Test
    void evaluate_GivenInvalidEndCommand_ShouldRunAllThrowExceptionEndCommand() throws ShellException, AbstractApplicationException, FileNotFoundException {
        //GIVEN / WHEN
        doThrow(ShellException.class).when(mockCommandFive).evaluate(any(InputStream.class), any(OutputStream.class));
        listOfCommands.addAll(Arrays.asList(mockCommandThree, mockCommandFour, mockCommandFive));
        sequenceCommand = new SequenceCommand(listOfCommands);
        sequenceCommand.evaluate(System.in, outputStream);

        //THEN
        String expectedOutput = "null" + STRING_NEWLINE;
        assertEquals(outputStream.toString(), expectedOutput);
        assertDoesNotThrow(() -> {
            verify(mockCommandThree).evaluate(any(InputStream.class), any(OutputStream.class));
            verify(mockCommandFour).evaluate(any(InputStream.class), any(OutputStream.class));
        });
    }
}
