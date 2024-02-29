package unit_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandBuilderTest {

    @Mock
    ApplicationRunner applicationRunner;

    @Test
    void parseCommand_NullCommandString_ShouldThrowShellException() {
        // WHEN / THEN
        assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(null, applicationRunner));
    }

    @Test
    void parseCommand_BlankCommandString_ShouldThrowShellException() {
        // WHEN / THEN
        assertThrows(ShellException.class, () -> CommandBuilder.parseCommand("", applicationRunner));
    }
}
