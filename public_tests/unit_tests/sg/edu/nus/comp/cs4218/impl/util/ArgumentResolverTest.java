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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;

public class ArgumentResolverTest {

    @Mock
    private ApplicationRunner applicationRunnerMock;

    @Mock
    private RegexArgument regexArgumentMock;

    @Mock
    private Command commandMock;

    @InjectMocks
    private ArgumentResolver argumentResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void resolveOneArgument_ShouldHandleCommandSubstitution() throws AbstractApplicationException, ShellException, FileNotFoundException {

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
            List<String> result = argumentResolver.resolveOneArgument(inputArg);

            // THEN
            assertEquals(List.of("test.txt"), result);

            // Verify interactions on the mocked static method
            mockedCommandBuilder.verify(() -> CommandBuilder.parseCommand(anyString(), any(ApplicationRunner.class)));
        }
    }
}
