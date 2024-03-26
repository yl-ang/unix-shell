package unit_tests.sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.RegexArgument;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ArgumentResolverTest {

    @Mock
    private Command commandMock;

    @InjectMocks
    @Spy
    private ArgumentResolver argumentResolverSpy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parseArguments_GivenMultipleValidArgInput_ShouldParseMultipleArguments() throws AbstractApplicationException, ShellException, FileNotFoundException {

        // GIVEN
        List<String> inputArgsList = Arrays.asList("arg1", "'arg 2'", "`ls`");

        // Mocking resolveOneArgument method to isolate the test
        doReturn(List.of("arg1")).when(argumentResolverSpy).resolveOneArgument("arg1");
        doReturn(List.of("arg 2")).when(argumentResolverSpy).resolveOneArgument("'arg 2'");
        doReturn(Arrays.asList("file1", "file2")).when(argumentResolverSpy).resolveOneArgument("`ls`");

        // WHEN
        List<String> result = argumentResolverSpy.parseArguments(inputArgsList);

        // THEN
        assertEquals(Arrays.asList("arg1", "arg 2", "file1", "file2"), result);
    }

    @Test
    void parseArguments_GivenEmptyArgList_ShouldHandleEmptyArgumentList() throws AbstractApplicationException, ShellException, FileNotFoundException {
        // GIVEN
        List<String> inputArgsList = List.of();

        // WHEN
        List<String> result = argumentResolverSpy.parseArguments(inputArgsList);

        // THEN
        assertEquals(List.of(), result);
    }

    @Test
    void resolveOneArgument_GivenCommandSubstitution_ShouldHandleCorrectly() throws AbstractApplicationException, ShellException, FileNotFoundException {

        // Mocking a static method using Mockito's MockedStatic (static method mocking)
        try (MockedStatic<CommandBuilder> mockedCommandBuilder = mockStatic(CommandBuilder.class)) {
            mockedCommandBuilder.when(() -> CommandBuilder.parseCommand(anyString(), any(ApplicationRunner.class)))
                    .thenReturn(commandMock);

            // Mock the behavior of the evaluate method (void method mocking)
            doAnswer(invocation -> {
                OutputStream outputStream = (OutputStream) invocation.getArguments()[1];
                outputStream.write("test.txt".getBytes());
                return null;  // evaluate is void
            }).when(commandMock).evaluate(any(InputStream.class), any(OutputStream.class));

            // GIVEN
            String inputArg = "`ls`";

            // WHEN
            List<String> result = argumentResolverSpy.resolveOneArgument(inputArg);

            // THEN
            assertEquals(List.of("test.txt"), result);

            // Verify interactions on the mocked static method
            mockedCommandBuilder.verify(() -> CommandBuilder.parseCommand(anyString(), any(ApplicationRunner.class)));
        }
    }

    @Test
    void resolveOneArgument_GivenGlobbing_ShouldHandleCorrectly() throws AbstractApplicationException, ShellException, FileNotFoundException {
        // GIVEN
        String inputArg = "file*.txt";

        // WHEN
        List<String> result = argumentResolverSpy.resolveOneArgument(inputArg);

        // THEN
        assertEquals(List.of("file*.txt"), result);
    }

    @Test
    void resolveOneArgument_GivenQuoting_ShouldCorrectly() throws AbstractApplicationException, ShellException, FileNotFoundException {
        // GIVEN
        String inputArg = "\"double-quoted text\"";

        // WHEN
        List<String> result = argumentResolverSpy.resolveOneArgument(inputArg);

        // THEN
        assertEquals(List.of("double-quoted text"), result);
    }

    @Test
    void resolveOneArgument_GivenSingleQuote_ShouldHandleCorrectly() throws AbstractApplicationException, ShellException, FileNotFoundException {
        // GIVEN
        String inputArg = "'single-quoted text'";

        // WHEN
        List<String> result = argumentResolverSpy.resolveOneArgument(inputArg);

        // THEN
        assertEquals(List.of("single-quoted text"), result);
    }
}
