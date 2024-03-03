package unit_tests.sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CAT;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_ECHO;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MULTIPLE_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class IORedirectionHandlerTest {

    private IORedirectionHandler ioRedirectionHandler;

    @Mock
    private InputStream inputStream;

    @Mock
    private OutputStream outputStream;
    @Mock
    private ArgumentResolver argumentResolver;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "unit_tests", "ioRedirectHandler"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);
    private static final List<String> INVALID_EMPTY_ARGS_LIST = new ArrayList<>();
    private static final String STR_CHAR_REDIR_OUTPUT = String.valueOf(CHAR_REDIR_OUTPUT);
    private static final String STR_CHAR_REDIR_INPUT = String.valueOf(CHAR_REDIR_INPUT);
    private static final String FILE_NAME_PLACEHOLDER = "out.txt";
    private static final String INVALID_FILE_NAME_PLACEHOLDER = "non-exists.txt";
    private static final String TEXT_INPUT = "'The mysterious old book on the dusty shelf seemed to whisper secrets of a bygone era.'";

    @BeforeEach
    public void setup() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Environment.currentDirectory = TEST_DIRECTORY;
        argumentResolver = mock(ArgumentResolver.class);
        outputStream = mock(OutputStream.class);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        Path path = Paths.get(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_PLACEHOLDER);
        if (Files.exists(path)) {
            // Delete the file
            Files.delete(path);
        }
    }

    @AfterAll
    static void teardown() {
        Environment.currentDirectory = ROOT_DIRECTORY;
    }

    @Test
    void extractRedirOptions_ValidSingleArgsInpput_ShouldExtractRedirOption() throws IOException, AbstractApplicationException, ShellException {
        when(argumentResolver.resolveOneArgument(anyString())).thenReturn(List.of(FILE_NAME_PLACEHOLDER));
        ioRedirectionHandler = new IORedirectionHandler(List.of(APP_ECHO, TEXT_INPUT, STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER), inputStream, outputStream, argumentResolver);
        ioRedirectionHandler.extractRedirOptions();
        assertEquals(2, ioRedirectionHandler.getNoRedirArgsList().size());
        assertEquals(APP_ECHO, ioRedirectionHandler.getNoRedirArgsList().get(0));
        assertEquals(TEXT_INPUT, ioRedirectionHandler.getNoRedirArgsList().get(1));
        Path path = Paths.get(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_PLACEHOLDER);
        assertTrue(Files.exists(path));
    }

    @Test
    void extractRedirOptions_ValidDoubleArgsInput_ShouldExtractRedirOption() throws IOException, AbstractApplicationException, ShellException {
        when(argumentResolver.resolveOneArgument(anyString())).thenReturn(List.of(FILE_NAME_PLACEHOLDER));
        ioRedirectionHandler = new IORedirectionHandler(List.of(APP_ECHO, TEXT_INPUT, STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER, STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER), inputStream, outputStream, argumentResolver);
        ioRedirectionHandler.extractRedirOptions();
        // ExtractRedirOption remain consistent with additional redirectHandler argsList
        assertEquals(2, ioRedirectionHandler.getNoRedirArgsList().size());
        assertEquals(APP_ECHO, ioRedirectionHandler.getNoRedirArgsList().get(0));
        assertEquals(TEXT_INPUT, ioRedirectionHandler.getNoRedirArgsList().get(1));
        Path path = Paths.get(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_PLACEHOLDER);
        assertTrue(Files.exists(path));
    }

    @Test
    void extractRedirOptions_ValidEmptyRedirOptionsInput_ShouldExtractRedirOption() throws IOException, AbstractApplicationException, ShellException {
        when(argumentResolver.resolveOneArgument(anyString())).thenReturn(List.of(FILE_NAME_PLACEHOLDER));
        ioRedirectionHandler = new IORedirectionHandler(List.of(STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER), inputStream, outputStream, argumentResolver);
        ioRedirectionHandler.extractRedirOptions();
        assertEquals(0, ioRedirectionHandler.getNoRedirArgsList().size());
    }

    @Test
    void extractRedirOptions_NullArgsList_ShouldThrowException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        ioRedirectionHandler = new IORedirectionHandler(null, inputStream, outputStream, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());
        assertEquals(new ShellException(ERR_SYNTAX).getMessage(), exception.getMessage());
    }

    @Test
    void extractRedirOptions_EmptyArgsList_ShouldThrowException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        ioRedirectionHandler = new IORedirectionHandler(INVALID_EMPTY_ARGS_LIST, inputStream, outputStream, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());
        assertEquals( new ShellException(ERR_SYNTAX).getMessage(), exception.getMessage());
    }

    @Test
    void extractRedirOptions_InvalidMultipleRedirTargetAfterArgumentResolver_ShouldThrowException() throws IOException, AbstractApplicationException, ShellException {
        when(argumentResolver.resolveOneArgument(anyString())).thenReturn(List.of(FILE_NAME_PLACEHOLDER, FILE_NAME_PLACEHOLDER));
        ioRedirectionHandler = new IORedirectionHandler(List.of(APP_ECHO, STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER), inputStream, outputStream, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());
        assertEquals(new ShellException(ERR_SYNTAX).getMessage(), exception.getMessage());
    }
    @Test
    void extractRedirOptions_InvalidInputFileDoesNotExists_ShouldThrowException() throws IOException, AbstractApplicationException, ShellException {
        when(argumentResolver.resolveOneArgument(anyString())).thenReturn(List.of(INVALID_FILE_NAME_PLACEHOLDER));
        ioRedirectionHandler = new IORedirectionHandler(List.of(APP_CAT, STR_CHAR_REDIR_INPUT, INVALID_FILE_NAME_PLACEHOLDER), inputStream, outputStream, argumentResolver);
        Exception exception = assertThrows(FileNotFoundException.class, () -> ioRedirectionHandler.extractRedirOptions());
        String errorMessage = String.format("%s %s %s", ERR_FILE_NOT_FOUND, CHAR_COLON, TEST_DIRECTORY + STR_FILE_SEP + INVALID_FILE_NAME_PLACEHOLDER);
        assertEquals(new FileNotFoundException(errorMessage).getMessage(), exception.getMessage());
    }

    @Test
    void extractRedirOptions_InvalidMultipleDifferentRedirect_ShouldThrowException() throws IOException, AbstractApplicationException, ShellException {
        ioRedirectionHandler = new IORedirectionHandler(List.of(STR_CHAR_REDIR_OUTPUT, STR_CHAR_REDIR_INPUT), inputStream, outputStream, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());
        assertEquals(new ShellException(ERR_SYNTAX).getMessage(), exception.getMessage());
    }

    @Test
    void extractRedirOptions_InvalidMultipleStreamInput_ShouldThrowException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        // Arrange
        List<String> argsList = Arrays.asList(APP_CAT, STR_CHAR_REDIR_INPUT, FILE_NAME_PLACEHOLDER);
        InputStream originalInputStream = System.in;
        OutputStream originalOutputStream = System.out;
        when(argumentResolver.resolveOneArgument(anyString())).thenReturn(List.of(FILE_NAME_PLACEHOLDER));
        ioRedirectionHandler = new IORedirectionHandler(List.of(APP_ECHO, TEXT_INPUT, STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER), originalInputStream, originalOutputStream, argumentResolver);
        ioRedirectionHandler.extractRedirOptions();
        Exception exception = assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());
        assertEquals(new ShellException(ERR_MULTIPLE_STREAMS).getMessage(), exception.getMessage());
    }

    @Test
    void extractRedirOptions_InvalidMultipleStreamOut_ShouldThrowException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        // Arrange
        List<String> argsList = Arrays.asList(APP_CAT, STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER);
        InputStream originalInputStream = System.in;
        OutputStream originalOutputStream = System.out;
        when(argumentResolver.resolveOneArgument(anyString())).thenReturn(List.of(FILE_NAME_PLACEHOLDER));
        ioRedirectionHandler = new IORedirectionHandler(List.of(APP_ECHO, TEXT_INPUT, STR_CHAR_REDIR_OUTPUT, FILE_NAME_PLACEHOLDER), originalInputStream, originalOutputStream, argumentResolver);
        ioRedirectionHandler.extractRedirOptions();
        Exception exception = assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());
        assertEquals(new ShellException(ERR_MULTIPLE_STREAMS).getMessage(), exception.getMessage());
    }
}
