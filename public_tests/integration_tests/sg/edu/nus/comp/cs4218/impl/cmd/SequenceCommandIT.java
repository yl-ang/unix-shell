package integration_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
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

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class SequenceCommandIT {
    private SequenceCommand sequenceCommand;
    private ApplicationRunner applicationRunner;
    private ByteArrayOutputStream outputStream;
    private final InputStream systemInputStream = System.in;
    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String FOLDER_NAME_1 = "folder1";
    private static final String FOLDER_NAME_2 = "folder2"; // will be created and delete
    private static final String FOLDER_NAME_3 = "folder3"; // used for subfolder
    private static final String FILE_NAME_OUTPUT = "tempfile.txt";
    private static final String FOLDER_NAME_SUB = "sub_folder";
    private static final String FOLDER_PATH_SUB = FOLDER_NAME_3 + STR_FILE_SEP + FOLDER_NAME_SUB;
    private static final String FOLDER_NAME_NON = "nonfolder";
    private static final String FILE_NAME_1 = "file1.txt";
    private static final String FILE_NAME_2 = "file2.txt";
    private static final String FILE_NAME_3 = "file3.txt";
    private static final String FILE_NAME_4 = "file4.txt";
    private static final String FILE_NAME_5 = "file5.txt";
    private static final String FILE_NAME_6 = "file6.txt";
    private static final String FILE_GLOBBING = "*.txt";

    @TempDir
    File tempDirectory;
    @BeforeEach
    void setup() throws IOException {
        Environment.currentDirectory = tempDirectory.getAbsolutePath();

        String folderPrefix = tempDirectory + STR_FILE_SEP;

        FileWriter writer = new FileWriter(folderPrefix + FILE_NAME_1);
        writer.write("bye world");
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_2);
        writer.write("""
                1 but who?
                2
                9 but not me
                4
                5""");
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_3);
        writer.write("""
                John
                Woods
                mAson
                bowman
                john
                johhn
                j0hn
                johN
                3 mason
                q q
                grep grep grep -h cs4218
                """);
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_4);
        writer.write("""
                apple
                banana
                banana
                orange
                orange
                kiwi
                """);
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_5);
        writer.write("""
                banana
                grape
                apple
                apple
                orange""");
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_6);
        writer.write("""
                Name,Age,Location
                Kat,25,New York
                Alice,30,Los Angeles
                Bob,35,Chicago
                """);
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

    // POSITIVE TEST CASE - PAIRWISE
    @Test
    @Tag("SequenceCommandIT:Pairwise:1")
    public void callCommandIT_CallCommandThenCallCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("sort %s > %s; grep -iH john %s", FILE_NAME_3, FILE_NAME_OUTPUT, FILE_GLOBBING);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = """
                file3.txt:John
                file3.txt:john
                file3.txt:johN
                tempfile.txt:John
                tempfile.txt:johN
                tempfile.txt:john
                """;
        assertEquals(expectedOutput, outputStream.toString());
        Path path = Path.of(tempDirectory + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:2")
    public void callCommandIT_CallCommandThenSequenceCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("wc %s%s%s; wc %s%s%s; wc %secho %s%s",
                CHAR_DOUBLE_QUOTE, FILE_GLOBBING, CHAR_DOUBLE_QUOTE,
                CHAR_SINGLE_QUOTE, FILE_GLOBBING, CHAR_SINGLE_QUOTE,
                CHAR_BACK_QUOTE, FILE_GLOBBING, CHAR_BACK_QUOTE);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);

        String expectedOutput = """
                wc: *.txt: No such file or directory
                wc: *.txt: No such file or directory
                \t0\t2\t9 file1.txt
                \t4\t10\t29 file2.txt
                \t11\t17\t82 file3.txt
                \t6\t6\t39 file4.txt
                \t4\t5\t31 file5.txt
                \t4\t6\t70 file6.txt
                \t29\t46\t260 total
                """;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:3")
    public void callCommandIT_CallCommandThenPipeCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String inputText = "She sells sea shells by the sea shore.";
        String commandInputStr = String.format("echo '%s' > %s; paste %s | grep sells", inputText, FILE_NAME_OUTPUT, FILE_NAME_OUTPUT);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);

        String expectedOutput = "She sells sea shells by the sea shore." + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());

        Path path = Path.of(tempDirectory + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:4")
    public void callCommandIT_SequenceCommandThenCallCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("echo Hello, World! > %s; cat %s ; rm %s", FILE_NAME_OUTPUT, FILE_NAME_OUTPUT, FILE_NAME_OUTPUT);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "Hello, World!" + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
        Path path = Path.of(tempDirectory + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertFalse(Files.exists(path));
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:5")
    public void callCommandIT_SequenceCommandThenSequenceCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("mkdir -p %s; cd %s; ls; mv %s %s; rm -d %s", FOLDER_PATH_SUB, FOLDER_NAME_3, FOLDER_NAME_SUB, FOLDER_NAME_1, FOLDER_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = FOLDER_NAME_SUB + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
        Path path = Path.of(tempDirectory + STR_FILE_SEP + FOLDER_NAME_3);
        assertTrue(Files.exists(path));
        path = Path.of(tempDirectory + STR_FILE_SEP + FOLDER_NAME_3 + STR_FILE_SEP + FOLDER_NAME_SUB);
        assertFalse(Files.exists(path));
        path = Path.of(tempDirectory + STR_FILE_SEP + FOLDER_NAME_3 + STR_FILE_SEP + FOLDER_NAME_1);
        assertFalse(Files.exists(path));
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:6")
    public void callCommandIT_SequenceCommandThenPipeCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("echo monkey; cat %s ; cat %s | sort -r | uniq", FILE_NAME_4, FILE_NAME_5);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = """
                monkey
                apple
                banana
                banana
                orange
                orange
                kiwi
                orange
                grape
                banana
                apple
                """;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:7")
    // NEGATIVE TEST CASE - PAIRWISE
    public void callCommandIT_PipeCommandThenCallCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("echo Hello, World | tee %s %s /%s/%s; echo sequence_continued", FILE_NAME_1, FILE_NAME_2, FOLDER_NAME_NON, FILE_NAME_3);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);

        String expectedOutput = """
                tee: /nonfolder/file3.txt: No such file or directory
                Hello, World
                sequence_continued
                """;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:8")
    public void callCommandIT_PipeCommandThenSequenceCommand_ShouldReturnCorrectResult() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = "echo CS4218-CS4222-CS5222 | cut -b 2,6; echo total; echo 90";
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);

        String expectedOutput = """
                S8
                total
                90
                """;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    @Tag("SequenceCommandIT:Pairwise:9")
    public void callCommandIT_PipeCommandThenPipeCommand_ShouldReturnCorrectResult() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("paste %s %s | sort -r > %s; paste %s %s | sort -f", FILE_NAME_6, FILE_NAME_5, FILE_NAME_OUTPUT, FILE_NAME_5, FILE_NAME_6 );
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);

        String expectedOutput = """
                apple\tAlice,30,Los Angeles
                apple\tBob,35,Chicago
                banana\tName,Age,Location
                grape\tKat,25,New York
                orange\t
                """;
        assertEquals(expectedOutput, outputStream.toString());

        Path path = Path.of(tempDirectory + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Name,Age,Location\tbanana
                Kat,25,New York\tgrape
                Bob,35,Chicago\tapple
                Alice,30,Los Angeles\tapple
                \torange
                """;
        assertEquals(expected, fileContent);
    }

    // POSITIVE TEST CASE - MILESTONE 1
    @Test
    public void sequenceCommandIT_EchoThenCd_ShouldReturnCorrectOutput() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; cd %s", FOLDER_NAME_1);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "hello world" + STRING_NEWLINE;
        String expectedDir = tempDirectory.getAbsolutePath() + CHAR_FILE_SEP + FOLDER_NAME_1;
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
        Path path = Path.of(tempDirectory + STR_FILE_SEP + FOLDER_NAME_2);
        assertTrue(Files.exists(path));
    }

    @Test
    public void sequenceCommandIT_EchoThenSort_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; sort -n -r %s", FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[]{"hello world", "9 but not me", "5", "4", "2", "1 but who?"}) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
    }

    // NEGATIVE TEST CASE - Milestone 1
    @Test
    public void sequenceCommandIT_EchoThenCat_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("echo hello world; cat %s", FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[]{"hello world", "1 but who?", "2", "9 but not me", "4", "5"}) + STRING_NEWLINE;
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
        assertEquals(tempDirectory.getAbsolutePath(), Environment.currentDirectory);
    }

    @Test
    public void sequenceCommandIT_CdThenMkdir_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s; mkdir -p %s", FOLDER_NAME_NON, FOLDER_PATH_SUB);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = "cd: No such file or directory" + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());

        Path path = Path.of(tempDirectory + STR_FILE_SEP + FOLDER_PATH_SUB);
        Path pathParent = Path.of(tempDirectory + STR_FILE_SEP + FOLDER_NAME_3);
        assertTrue(Files.exists(path));
        assertTrue(Files.exists(pathParent));
    }

    @Test
    public void sequenceCommandIT_CdThenSort_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s; sort -n -r %s", FOLDER_NAME_NON, FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[]{"cd: No such file or directory", "9 but not me", "5", "4", "2", "1 but who?"}) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void sequenceCommandIT_CdThenCat_ShouldReturnCorrectOutput() throws ShellException, IOException, AbstractApplicationException {
        String commandInputStr = String.format("cd %s; cat %s", FOLDER_NAME_NON, FILE_NAME_2);
        Command command = CommandBuilder.parseCommand(commandInputStr, applicationRunner);
        sequenceCommand = (SequenceCommand) command;
        sequenceCommand.evaluate(systemInputStream, outputStream);
        String expectedOutput = String.join(STRING_NEWLINE, new String[]{"cd: No such file or directory", "1 but who?", "2", "9 but not me", "4", "5"}) + STRING_NEWLINE;
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
        String[] expectedOResultArr = new String[]{
                String.format("cat: %s: No such file or directory", FILE_NAME_2),
                "chicken-rice",
                String.format("wc: %s: No such file or directory", FILE_NAME_1),
                "sort: No such file or directory"
        };
        String expectedOutput = String.join(STRING_NEWLINE, expectedOResultArr) + STRING_NEWLINE;
        assertEquals(expectedOutput, outputStream.toString());
        String filePathStr = tempDirectory + STR_FILE_SEP + FOLDER_NAME_2;
        Path path = Path.of(filePathStr);
        assertTrue(Files.exists(path));
        assertEquals(Environment.currentDirectory, filePathStr);
    }
}