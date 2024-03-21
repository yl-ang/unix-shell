package integration_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser.FLAG_IS_LINE_NUMBER;
import static sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser.FLAG_IS_RECURSIVE;
import static sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser.FLAG_IS_SORT_BY_EXT;
import static sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser.FLAG_IS_SERIAL;
import static sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser.*;
import static sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser.FLAG_IS_LINE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser.FLAG_IS_WORD_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class CallCommandIT {

    private  CallCommand callCommand;
    private ArgumentResolver argumentResolver;
    private ApplicationRunner applicationRunner;
    private ByteArrayOutputStream outputStream;

    private final InputStream systemInputStream = System.in;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String[] TEST_DIRECTORY_ARR = {ROOT_DIRECTORY, "public_tests", "resources", "integration_tests", "call_command"};
    private static final String TEST_DIRECTORY = String.join(STR_FILE_SEP, TEST_DIRECTORY_ARR);

    private static final String FOLDER_NAME_1 = "folder1";
    private static final String FOLDER_NAME_2 = "folder2";

    private static final String FOLDER_NAME_3 = "folder3";
    private static final String FOLDER_NAME_NON = "nonfolder";
    private static final String FILE_NAME_1 = "file1.txt";
    private static final String FILE_NAME_2 = "file2.txt";
    private static final String FILE_NAME_3 = "file3.txt";
    private static final String FILE_NAME_OUTPUT = "tempfile.txt";
    private static final String FILE_NAME_NONE = "nonfile.txt";
    private static final String FILE_GLOBBING = "*.txt";
    public static final String STR_REDIR_OUTPUT = String.valueOf(CHAR_REDIR_OUTPUT);
    public static final String STR_REDIR_INPUT = String.valueOf(CHAR_REDIR_INPUT);
    public static final String STR_FLAG_PREFIXTR = String.valueOf(CHAR_FLAG_PREFIX);

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

    // POSITIVE TEST CASE - PAIRWISE TESTING
    @Test
    @Tag("CallCommandIT:1")
    public void callCommandIT_EchoCommandRedirectOutputSingleQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, CHAR_SINGLE_QUOTE + "hello world" + CHAR_SINGLE_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "hello world" + STRING_NEWLINE;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:2")
    public void callCommandIT_EchoCommandBackQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, "`echo hello world`");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "hello world" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:3")
    public void callCommandIT_WcCommandRedirectInputDoubleQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_WC, STR_REDIR_INPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_3 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "\t7\t9\t50" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }
    @Test
    @Tag("CallCommandIT:4")
    public void callCommandIT_WcCommandRedirectOutputWithGlobbingBackQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_WC, STR_FLAG_PREFIXTR + FLAG_IS_WORD_COUNT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_GLOBBING + CHAR_BACK_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected =
               "\t2 file1.txt\n" +
               "\t5 file2.txt\n" +
               "\t9 file3.txt\n" +
               "\t0 tempfile.txt\n" +
               "\t16 total" +
                STRING_NEWLINE;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:5")
    public void callCommandIT_WcCommandRedirectInputAndOutputMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_WC, STR_FLAG_PREFIXTR + FLAG_IS_WORD_COUNT + FLAG_IS_LINE_COUNT, STR_REDIR_INPUT, FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected =
                "\t7\t9" +
                STRING_NEWLINE;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:6")
    @Disabled
    // TODO: Awaiting Fix
    public void callCommandIT_SortCommandRedirectInputWithGlobbingMultipleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_FLAG_PREFIXTR + FLAG_IS_REV_ORDER + FLAG_IS_CASE_IGNORE, STR_REDIR_INPUT, FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = "orange\n" +
                "kiwi\n" +
                "hello worldbanana\n" +
                "Hello World\n" +
                "Hello World\n" +
                "grape\n" +
                "Bob\n" +
                "Bob\n" +
                "apple\n" +
                "Alice\n" +
                "Alice\n" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:8")
    public void callCommandIT_SortCommandRedirectInputAndOutputBackQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_REDIR_INPUT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_NAME_3 + CHAR_BACK_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected =
                "Alice\n" +
                "Alice\n" +
                "Alice\n" +
                "Bob\n" +
                "Bob\n" +
                "Hello World\n" +
                "Hello World" +
                STRING_NEWLINE;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:9")
    @Disabled
    // TODO: Awaiting Fix
    public void callCommandIT_SortCommandRedirectInputWithGlobbingSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_FLAG_PREFIXTR + FLAG_IS_REV_ORDER, STR_REDIR_INPUT, FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected =
            "orange\n" +
            "kiwi\n" +
            "hello worldbanana\n" +
            "grape\n" +
            "apple\n" +
            "Hello World\n" +
            "Hello World\n" +
            "Bob\n" +
            "Bob\n" +
            "Alice\n" +
            "Alice\n" +
            "Alice\n" +
            STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:10")
    public void callCommandIT_SortCommandRedirectOutputSingleQuoteMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_FLAG_PREFIXTR + FLAG_IS_REV_ORDER + FLAG_IS_FIRST_NUM + FLAG_IS_CASE_IGNORE, FILE_NAME_2, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "orange\n" +
                "kiwi\n" +
                "grape\n" +
                "banana\n" +
                "apple" +
            STRING_NEWLINE;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:11")
    @Disabled
    // TODO: Awaiting Fix
    public void callCommandIT_CatCommandRedirectOutputWithGlobbingSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_FLAG_PREFIXTR + FLAG_IS_LINE_NUMBER , FILE_GLOBBING, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "     1\thello world     1\tbanana\n" +
                "     2\tapple\n" +
                "     3\torange\n" +
                "     4\tgrape\n" +
                "     5\tkiwi\n" +
                "     1\tHello World\n" +
                "     2\tHello World\n" +
                "     3\tAlice\n" +
                "     4\tAlice\n" +
                "     5\tBob\n" +
                "     6\tAlice\n" +
                "     7\tBob\n"  +
                STRING_NEWLINE;

        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:12")
    @Disabled
    // TODO: Awaiting Fix
    public void callCommandIT_CatCommandRedirectInputWithGlobbingBackQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_FLAG_PREFIXTR + FLAG_IS_LINE_NUMBER , STR_REDIR_INPUT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_GLOBBING + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = "     1  hello worldbanana\n" +
                "     2  apple\n" +
                "     3  orange\n" +
                "     4  grape\n" +
                "     5  kiwi\n" +
                "     6  Hello World\n" +
                "     7  Hello World\n" +
                "     8  Alice\n" +
                "     9  Alice\n" +
                "    10  Bob\n" +
                "    11  Alice\n" +
                "    12  Bob\n" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:13")
    public void callCommandIT_CatCommandRedirectInputBackQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_FLAG_PREFIXTR + FLAG_IS_LINE_NUMBER , STR_REDIR_INPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_2 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = "1 banana\n" +
                "2 apple\n" +
                "3 orange\n" +
                "4 grape\n" +
                "5 kiwi" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:14")
    public void callCommandIT_CatCommandRedirectInputAndOutput_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_REDIR_INPUT, FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "Hello World\n" +
                "Hello World\n" +
                "Alice\n" +
                "Alice\n" +
                "Bob\n" +
                "Alice\n" +
                "Bob" +
                STRING_NEWLINE;

        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:15")
    public void callCommandIT_LsCommandRedirectOutputBackQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_LS, STR_FLAG_PREFIXTR + FLAG_IS_SORT_BY_EXT, STR_REDIR_OUTPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_OUTPUT + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "folder1\n" +
                "folder3\n" +
                "file1.txt\n" +
                "file2.txt\n" +
                "file3.txt\n" +
                "tempfile.txt" +
                STRING_NEWLINE;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:16")
    public void callCommandIT_LsCommandWithGlobbing_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_LS, FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                file1.txt

                file2.txt

                file3.txt
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:17")
    public void callCommandIT_LsCommandRedirectOutputSingleQuoteMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_LS, STR_FLAG_PREFIXTR + FLAG_IS_SORT_BY_EXT + FLAG_IS_RECURSIVE, STR_REDIR_OUTPUT, CHAR_SINGLE_QUOTE + FILE_NAME_OUTPUT + CHAR_SINGLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                ./:
                folder1
                folder3
                file1.txt
                file2.txt
                file3.txt
                tempfile.txt

                folder1:
                placeholder.txt

                folder3:
                folder1
                file1.txt
                file2.txt
                file3.txt

                folder3/folder1:
                placeholder.txt
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:18")
    public void callCommandIT_PasteCommandBackQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, STR_FLAG_PREFIXTR + FLAG_IS_SERIAL, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + CHAR_SINGLE_QUOTE + FILE_NAME_1 + CHAR_SPACE + FILE_NAME_2 + CHAR_SINGLE_QUOTE + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                hello world
                banana\tapple\torange\tgrape\tkiwi

                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:19")
    public void callCommandIT_PasteCommandRedirectInputAndOutputSingleQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, STR_FLAG_PREFIXTR + FLAG_IS_SERIAL, STR_REDIR_INPUT, CHAR_SINGLE_QUOTE + FILE_NAME_1 + CHAR_SINGLE_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "hello world\n";

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:20")
    // TODO: Fix line break
    public void callCommandIT_PasteCommandRedirectOutputWithGlobbingSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, STR_FLAG_PREFIXTR + FLAG_IS_SERIAL, FILE_GLOBBING , STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEST_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "hello world\n" +
                "banana\tapple\torange\tgrape\tkiwi\n" +
                "Hello World\tHello World\tAlice\tAlice\tBob\tAlice\tBob\n" +
                "\n" +
                "\n";

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    // POSITIVE TEST CASE - SIMPLE
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
    public void callCommandIT_SingleWcCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("wc", "-w", FILE_NAME_1);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "\t2 " + FILE_NAME_1 + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
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

    @Test
    public void callCommandIT_SingleCdCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cd", FOLDER_NAME_1);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expectedDir = TEST_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_1;
        assertEquals(expectedDir, Environment.currentDirectory);
    }

    @Test
    public void callCommandIT_SingleLsCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Environment.currentDirectory = Environment.currentDirectory + STR_FILE_SEP + FOLDER_NAME_3;
        List<String> args = List.of("ls");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = String.join(STRING_NEWLINE, new String[] {FILE_NAME_1, FILE_NAME_2, FILE_NAME_3, FOLDER_NAME_1 }) + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleGrepCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("grep", "Hello World" , FILE_NAME_3);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "Hello World" + STRING_NEWLINE + "Hello World" + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    @Disabled
    // TODO: Spacing ISSUE, To discuss
    public void callCommandIT_SinglePasteCommand_ShouldReturnCorrectOutput() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of("paste", FILE_NAME_1, FILE_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "hello world" + CHAR_TAB + "banana" + STRING_NEWLINE +
                CHAR_TAB + "apple" + STRING_NEWLINE +
                CHAR_TAB + "orange" + STRING_NEWLINE +
                CHAR_TAB + "grape" + STRING_NEWLINE +
                CHAR_TAB + "kiwi" + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleUniqCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("uniq", "-c", FILE_NAME_3);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "2 Hello World" + STRING_NEWLINE +
                "2 Alice\n" +
                "1 Bob\n" +
                "1 Alice\n" +
                "1 Bob\n";
        assertEquals(expected, outputStream.toString());
    }

    @Test
    @Disabled
    // TODO: Directory Issue, PR-ed
    public void callCommandIT_SingleCutCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cut", "-c", "1,2" , FILE_NAME_1);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "he";
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
    public void callCommandIT_SingleTeeCommand_ShouldReturnCorrectOutput() {

    }

    @Test
    public void callCommandIT_SingleMkdirCommand_ShouldReturnCorrectOutput() {

    }

    // NEGATIVE TEST CASE - SIMPLE
    @Test
    public void callCommandIT_SingleEchoCommand_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, "<", "123");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        assertEquals(new ShellException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
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
        assertEquals(new ShellException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
    }

    // COMMAND SUBSTITUTION POSITIVE TEST CASE - SIMPLE
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

    // COMMAND SUBSTITUTION NEGATIVE TEST CASE - SIMPLE
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
