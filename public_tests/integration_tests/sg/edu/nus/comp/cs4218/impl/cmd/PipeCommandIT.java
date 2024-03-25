package integration_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PipeCommandIT {
    private PipeCommand pipeCommand;

    private ApplicationRunner applicationRunner;
    private ByteArrayOutputStream outputStream;
    private final InputStream systemInputStream = System.in;

    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String FOLDER_NAME_1 = "folder1";
    private static final String FILE_NAME_1 = "file1.txt";
    private static final String FILE_NAME_2 = "file2.txt";
    private static final String FILE_NAME_3 = "file3.txt";

    private static final String FOLDER_NAME_NON = "nonfolder";
    @TempDir
    File TEMP_DIRECTORY;

    @BeforeEach
    void setup() throws IOException {
        Environment.currentDirectory = TEMP_DIRECTORY.getAbsolutePath();
        String folderPrefix = TEMP_DIRECTORY + STR_FILE_SEP;

        FileWriter writer = new FileWriter(folderPrefix + FILE_NAME_1);
        writer.write("""
                north
                south
                east
                west
                """);
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_2);
        writer.write("""
                woodlands
                marina bay sands
                pasir ris
                jurong east""");
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_3);
        writer.write("""
                yishun
                harbour front
                tampines
                lake side""");
        writer.close();

        File folder1 = new File(folderPrefix + FOLDER_NAME_1);
        // Folder 2 does not exist
        assertTrue(folder1.mkdir());

        applicationRunner = new ApplicationRunner();
        outputStream = new ByteArrayOutputStream();
    }

    @AfterAll
    static void cleanUp() {
        Environment.currentDirectory = ROOT_DIRECTORY;
    }

    // POSITIVE TEST CASE - PAIRWISE TESTING
    @Test
    public void pipeCommandIT_CommandPipeWithCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("paste %s %s %s | sort", FILE_NAME_1, FILE_NAME_2, FILE_NAME_3);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        pipeCommand = (PipeCommand) command;
        pipeCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                east\tpasir ris\ttampines
                north\twoodlands\tyishun
                south\tmarina bay sands\tharbour front
                west\tjurong east\tlake side
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void pipeCommandIT_CommandPipeWithPipe_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("paste %s %s %s | sort | grep east", FILE_NAME_1, FILE_NAME_2, FILE_NAME_3);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        pipeCommand = (PipeCommand) command;
        pipeCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                east\tpasir ris\ttampines
                west\tjurong east\tlake side
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    // POSITIVE TEST CASE - EDGE CASE TESTING
    @Test
    public void pipeCommandIT_NoOutputFromFirstCommand_ShouldChangeFolder() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s | cat", FOLDER_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        pipeCommand = (PipeCommand) command;
        pipeCommand.evaluate(systemInputStream, outputStream);

        String expected = STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);

        expected = TEMP_DIRECTORY.getAbsolutePath() + STR_FILE_SEP + FOLDER_NAME_1;
        actual = Environment.currentDirectory;
        assertEquals(expected, actual);
    }

    @Test
    public void pipeCommandIT_MiddleCommandNoOutput_ShouldReturnEmptyResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("cat %s | grep lol | sort", FILE_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        pipeCommand = (PipeCommand) command;
        pipeCommand.evaluate(systemInputStream, outputStream);

        String expected = STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    // NEGATIVE TEST CASE - SIMPLE TESTING
    @Test
    public void pipeCommandIT_FirstCommandFail_ShouldTerminate() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s | cat", FOLDER_NAME_NON);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        pipeCommand = (PipeCommand) command;

        Exception exception = assertThrows(CdException.class, () -> pipeCommand.evaluate(systemInputStream, outputStream));
        String expected = "No such file or directory";
        assertEquals(new CdException(expected).getMessage(), exception.getMessage());

        expected = TEMP_DIRECTORY.getAbsolutePath();
        String actual = Environment.currentDirectory;
        assertEquals(expected, actual);
    }

    @Test
    public void pipeCommandIT_SecondCommandFail_ShouldTerminate() throws ShellException {
        String commandInputStr = String.format("cat %s | cat -z", FILE_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        pipeCommand = (PipeCommand) command;

        Exception exception = assertThrows(CatException.class, () -> pipeCommand.evaluate(systemInputStream, outputStream));
        String expected = "illegal option -- z";
        assertEquals(new CatException(expected).getMessage(), exception.getMessage());
    }

    @Test
    public void pipeCommandIT_InvalidCommand_ShouldTerminate() throws ShellException {
        String commandInputStr = String.format("cs4218 %s | cat -z", FILE_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        pipeCommand = (PipeCommand) command;

        Exception exception = assertThrows(ShellException.class, () -> pipeCommand.evaluate(systemInputStream, outputStream));
        String expected = "cs4218: Invalid app";
        assertEquals(new ShellException(expected).getMessage(), exception.getMessage());
    }

}
