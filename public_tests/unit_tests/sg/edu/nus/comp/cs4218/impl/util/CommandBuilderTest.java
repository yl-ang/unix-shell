package unit_tests.sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.LongVariable") // Testing Purpose for clarity
public class CommandBuilderTest {

    @Mock
    ApplicationRunner applicationRunner;

    @Mock
    ArgumentResolver argumentResolver;

    @InjectMocks
    CommandBuilder commandBuilder;

    public static final String ERR_SYNTAX = "Invalid syntax";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parseCommand_NullCommandString_ShouldThrowShellException() {
        // WHEN / THEN
        ShellException exception = assertThrows(ShellException.class, () -> commandBuilder.parseCommand(null, applicationRunner));
        assertEquals("shell: " + ERR_SYNTAX, exception.getMessage());
    }

    @Test
    void parseCommand_BlankCommandString_ShouldThrowShellException() {
        // WHEN / THEN
        ShellException exception = assertThrows(ShellException.class, () -> commandBuilder.parseCommand("", applicationRunner));
        assertEquals("shell: " + ERR_SYNTAX, exception.getMessage());
    }

    @Test
    void parseCommand_CommandStringWithBlankSpaces_ShouldThrowShellException() {
        // WHEN / THEN
        ShellException exception = assertThrows(ShellException.class, () -> commandBuilder.parseCommand(
                "   ", applicationRunner));
        assertEquals("shell: " + ERR_SYNTAX, exception.getMessage());
    }

    @Test
    void parseCommand_InvalidCommandString_ShouldThrowShellException() {
        String invalidCommandString = ";";
        assertThrows(ShellException.class, () -> commandBuilder.parseCommand(invalidCommandString,
                applicationRunner));
    }

    @Test
    void parseCommand_validCommandStringWithoutSequenceAndPipe_ShouldReturnCallCommand() throws ShellException {
        // GIVEN
        String commandString = "echo Hello";

        // WHEN
        Command finalCommand = CommandBuilder.parseCommand(commandString, applicationRunner);

        // THEN
        assertTrue(finalCommand instanceof CallCommand);
    }

    @Test
    void parseCommand_validCommandStringWithRedirection_ShouldReturnCallCommandWithRedirection() throws ShellException {
        // GIVEN
        String commandString = "echo Hello > output.txt";

        // WHEN
        Command finalCommand = CommandBuilder.parseCommand(commandString, applicationRunner);

        // THEN
        assertTrue(finalCommand instanceof CallCommand);
        List<String> tokens = ((CallCommand) finalCommand).getArgsList();
        assertEquals(4, tokens.size());
        assertEquals("echo", tokens.get(0));
        assertEquals("Hello", tokens.get(1));
        assertEquals(">", tokens.get(2));
        assertEquals("output.txt", tokens.get(3));
    }

    @Test
    void parseCommand_validCommandStringWithPipe_ShouldReturnPipeCommand() throws ShellException {
        // GIVEN
        String commandString = "echo Hello | grep Hello";

        // WHEN
        Command finalCommand = CommandBuilder.parseCommand(commandString, applicationRunner);

        // THEN
        assertInstanceOf(PipeCommand.class, finalCommand);
        List<CallCommand> pipeCommands = ((PipeCommand) finalCommand).getCallCommands();
        assertEquals(2, pipeCommands.size());
    }

    @Test
    void parseCommand_validCommandStringWithSemicolon_ShouldReturnSequenceCommand() throws ShellException {
        // GIVEN
        String commandString = "echo Hello ; ls";

        // WHEN
        Command finalCommand = CommandBuilder.parseCommand(commandString, applicationRunner);

        // THEN
        assertInstanceOf(SequenceCommand.class, finalCommand);
        List<Command> sequenceCommands = ((SequenceCommand) finalCommand).getCommands();
        assertEquals(2, sequenceCommands.size());
    }

    @Test
    void parseCommand_invalidCommandStringWithMismatchedQuote_ShouldThrowShellException() {
        // GIVEN
        String commandString = "echo 'Hello World";

        // THEN
        assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(commandString, applicationRunner));
    }

    @Test
    void parseCommand_invalidCommandStringWithMismatchedSemiColon_ShouldThrowShellException() {

        // GIVEN
        String commandString = ";echo Hello World";

        // THEN
        assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(commandString, applicationRunner));
    }

}
