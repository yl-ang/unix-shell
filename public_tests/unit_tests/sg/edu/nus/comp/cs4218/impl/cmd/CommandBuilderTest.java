package unit_tests.sg.edu.nus.comp.cs4218.impl.cmd;

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
import static org.mockito.Mockito.*;

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
        ShellException exception = assertThrows(ShellException.class, () -> commandBuilder.parseCommand("   ", applicationRunner));
        assertEquals("shell: " + ERR_SYNTAX, exception.getMessage());
    }
}
