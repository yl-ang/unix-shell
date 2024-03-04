package integration_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class SequenceCommandIT {
    private SequenceCommand sequenceCommand;
    private ApplicationRunner applicationRunner;
    private ByteArrayOutputStream outputStream;
    private final InputStream systemInputStream = System.in;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "integration_tests", "sequence_command"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);
    private static final String FOLDER_NAME_1 = "folder1";
    private static final String FOLDER_NAME_2 = "folder2"; // will be created and delete
    private static final String FOLDER_NAME_3 = "folder3"; // used for subfolder
    private static final String FOLDER_NAME_SUB = FOLDER_NAME_3 + STR_FILE_SEP + "sub_folder";
    private static final String FOLDER_NAME_NON = "nonfolder";
    private static final String FILE_NAME_1 = "file1.txt";
    private static final String FILE_NAME_2 = "file2.txt";
    private static final String FILE_NAME_NONE = "nonfile.txt";

    @BeforeEach
    void setup() {
        Environment.currentDirectory = TEST_DIRECTORY;
        applicationRunner = new ApplicationRunner();
        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2);
        Path pathSub = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_SUB);
        Path pathParent = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_3);

        if (Files.exists(path)) {
            Files.delete(path);
        }
        if (Files.exists(pathSub)) {
            Files.delete(pathSub);
        }
        if (Files.exists(pathParent)) {
            Files.delete(pathParent);
        }
    }

    @AfterAll
    static void cleanUp() {
        Environment.currentDirectory = ROOT_DIRECTORY;
    }

    // POSITIVE TEST CASE
    @Test
    public void sequenceCommandIT_EchoThenCd_ShouldReturnCorrectOutput() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; cd %s", FOLDER_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "hello world" + STRING_NEWLINE;
        String expectedDir = TEST_DIRECTORY + CHAR_FILE_SEP + FOLDER_NAME_1;
        assertEquals(expectedOutput, outputStream.toString());
        assertEquals(expectedDir, Environment.currentDirectory);
    }

    @Test
    public void sequenceCommandIT_EchoThenWc_ShouldReturnCorrectOutput() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; wc -w %s", FILE_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "hello world" + STRING_NEWLINE + "\t2 " + FILE_NAME_1 + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void sequenceCommandIT_EchoThenMkdir_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; mkdir %s", FOLDER_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "hello world" + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2);
        assertTrue(Files.exists(path));
    }

    @Test
    public void sequenceCommandIT_EchoThenSort_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; sort -n -r %s", FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[] {"hello world", "9 but not me", "5", "4", "2", "1 but who?"}) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void sequenceCommandIT_EchoThenCat_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; cat %s", FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[] {"hello world","1 but who?", "2", "9 but not me", "4", "5" }) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void sequenceCommandIT_CdThenWc_ShouldReturnCorrectOutput() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s; wc -w %s", FOLDER_NAME_NON, FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "cd: No such file or directory" + STRING_NEWLINE + "\t10 " + FILE_NAME_2 + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
        assertEquals(TEST_DIRECTORY, Environment.currentDirectory);
    }

    @Test
    public void sequenceCommandIT_CdThenMkdir_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s; mkdir -p %s", FOLDER_NAME_NON, FOLDER_NAME_SUB);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "cd: No such file or directory" + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_SUB);
        Path pathParent = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_3);
        assertTrue(Files.exists(path));
        assertTrue(Files.exists(pathParent));
    }

    @Test
    public void sequenceCommandIT_CdThenSort_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s; sort -n -r %s", FOLDER_NAME_NON, FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[] {"cd: No such file or directory", "9 but not me", "5", "4", "2", "1 but who?"}) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void sequenceCommandIT_CdThenCat_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s; cat %s", FOLDER_NAME_NON, FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[] {"cd: No such file or directory","1 but who?", "2", "9 but not me", "4", "5" }) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void sequenceCommandIT_BF1Application_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format(
                "mkdir %s; " +
                "cd %s; " +
                "cat %s; " +
                "echo chicken-rice; " +
                "wc %s; sort -n -r %s",
                FOLDER_NAME_2, FOLDER_NAME_2, FILE_NAME_2, FILE_NAME_1, FILE_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String[] expectedOResultArr = new String[] {
                String.format("cat: Could not read file: %s", FILE_NAME_2),
                "chicken-rice",
                "wc: No such file or directory",
                "sort: sort: No such file or directory"
        };
        String expectedOutput = String.join(STRING_NEWLINE, expectedOResultArr) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
        String filePathStr = TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2;
        Path path = Path.of(filePathStr);
        assertTrue(Files.exists(path));
        assertEquals(Environment.currentDirectory, filePathStr);
    }
}