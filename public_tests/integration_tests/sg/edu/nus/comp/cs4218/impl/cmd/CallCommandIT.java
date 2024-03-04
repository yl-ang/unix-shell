package integration_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_ECHO;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class CallCommandIT {

    private  CallCommand callCommand;
    private ArgumentResolver argumentResolver;
    private ApplicationRunner applicationRunner;
    private ByteArrayOutputStream outputStream;

    private InputStream systemInputStream = System.in;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "integration_tests", "call_command"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);

    private static final String FOLDER_NAME_1 = "folder1";
    private static final String FOLDER_NAME_2 = "folder2";
    private static final String FOLDER_NAME_NON = "nonfolder";
    private static final String FILE_NAME_1 = "file1.txt";
    private static final String FILE_NAME_2 = "file2.txt";
    private static final String FILE_NAME_NONE = "nonfile.txt";

    @BeforeEach
    void setup() {
        Environment.currentDirectory = TEST_DIRECTORY;
        argumentResolver = new ArgumentResolver();
        applicationRunner = new ApplicationRunner();
        outputStream = new ByteArrayOutputStream();
    }

    @AfterAll
    static void cleanUp() {
        Environment.currentDirectory = ROOT_DIRECTORY;
    }

    // POSITIVE TEST CASE
    @Test
    public void callCommandIT_SingleEchoCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, "hello", "world");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "hello world" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void callCommandIT_SingleCdCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cd", FOLDER_NAME_1);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expectedDir = TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_1;
        assertEquals(expectedDir, Environment.currentDirectory);
    }

    @Test
    public void callCommandIT_SingleWcCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("wc", "-w", FILE_NAME_1);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "\t2 " + FILE_NAME_1 + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleMkDirCommand_ShouldReturnCorrectOutput() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of("mkdir", FOLDER_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2);
        assertTrue(Files.exists(path));

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    public void callCommandIT_SingleSortCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("sort", "-r", FILE_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = String.join(STRING_NEWLINE, new String[] {"orange",  "kiwi", "grape", "banana", "apple"}) + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleCatCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cat", "<", FILE_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = String.join(STRING_NEWLINE, new String[] {"banana", "apple", "orange", "grape", "kiwi", }) + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    // NEGATIVE TEST CASE
    @Test
    public void callCommandIT_SingleEchoCommand_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, "<", "123");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        String expected = String.format("%s %s %s", "No such file or directory", ":", TEST_DIRECTORY + STR_FILE_SEP + "123");
        assertEquals(new ShellException(expected).getMessage(), exception.getMessage());
    }

    @Test
    public void callCommandIT_SingleCdCommand_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cd", FOLDER_NAME_NON);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(CdException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        String expected = "No such file or directory";
        assertEquals(new CdException(expected).getMessage(), exception.getMessage());
    }

    @Test
    public void callCommandIT_SingleWcCommand_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("wc", "-o", FILE_NAME_1); // invalid flag
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(WcException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        String expected = "wc: illegal option -- o";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    public void callCommandIT_SingleMkDirCommand_ShouldReturnNegativeOutput() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of("mkdir", FOLDER_NAME_NON + STR_FILE_SEP + FILE_NAME_1); // making file with parent w/o p flag
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(MkdirException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        String expected = String.format("mkdir: %s%s%s: No such file or directory", FOLDER_NAME_NON, STR_FILE_SEP, FILE_NAME_1);
        assertEquals(expected, exception.getMessage());
    }

    @Test
    public void callCommandIT_SingleSortCommand_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("sort", FOLDER_NAME_2); // sort a dir
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(SortException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        String expected = "sort: sort: No such file or directory";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    public void callCommandIT_SingleCatCommand_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cat", "<", FOLDER_NAME_2); // cat a dir
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        String expected = String.format("shell: No such file or directory : %s%s%s", TEST_DIRECTORY, STR_FILE_SEP, FOLDER_NAME_2);
        assertEquals(expected, exception.getMessage());
    }

    // COMMAND SUBSTITUTION POSITIVE TEST CASE
    @Test
    public void callCommandIT_EchoWithSubstitutionEcho_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, "hello", "`echo world`");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "hello world" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void callCommandIT_EchoWithSubstitutionSort_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("echo", "fruits: ", String.format("`sort -r %s`", FILE_NAME_2));
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "fruits:  orange kiwi grape banana apple" + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_WcWithSubstitutionEcho_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("wc", String.format("`echo -w -l -c %s`", FILE_NAME_1));
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "\t0\t2\t11 " + FILE_NAME_1 + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    // COMMAND SUBSTITUTION NEGATIVE TEST CASE
    @Test
    public void callCommandIT_WcWithSubstitutionEcho_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("wc", String.format("`echo -w -l -c %s`", FILE_NAME_NONE));
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "wc: No such file or directory" + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_CdWithSubstitutionEcho_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cd", String.format("`echo %s`", FOLDER_NAME_NON));
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(CdException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        String expected = "No such file or directory";
        assertEquals(new CdException(expected).getMessage(), exception.getMessage());
    }
}
