package integration_tests.sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser.FLAG_IS_LINE_NUMBER;
import static sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser.FLAG_IS_BYTE_CUT;
import static sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser.FLAG_IS_CHAR_CUT;
import static sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser.FLAG_IS_CASING;
import static sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser.FLAG_IS_INCLUDE_FILENAME;
import static sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser.FLAG_IS_RECURSIVE;
import static sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser.FLAG_IS_SORT_BY_EXT;
import static sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser.FLAG_IS_SERIAL;
import static sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser.FLAG_IS_EMPTY_FOLDER;
import static sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser.*;
import static sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser.FLAG_IS_APPEND;
import static sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser.FLAG_IS_COUNT;
import static sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser.FLAG_IS_DUPS;
import static sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser.FLAG_IS_LINE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser.FLAG_IS_WORD_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class CallCommandIT {

    private CallCommand callCommand;
    private ArgumentResolver argumentResolver;
    private ApplicationRunner applicationRunner;
    private ByteArrayOutputStream outputStream;

    private final InputStream systemInputStream = System.in;

    private static final String STR_FILE_SEP = String.valueOf(CHAR_FILE_SEP);
    private static final String ROOT_DIRECTORY = Environment.currentDirectory;

    private static final String FOLDER_NAME_1 = "folder1";
    private static final String FOLDER_NAME_2 = "folder2";
    private static final String FOLDER_NAME_3 = "folder3";
    private static final String FOLDER_NAME_NON = "nonfolder";
    private static final String FILE_NAME_1 = "file1.txt";
    private static final String FILE_NAME_2 = "file2.txt";
    private static final String FILE_NAME_3 = "file3.txt";
    private static final String FILE_NAME_4 = "file4.txt";
    private static final String FILE_NAME_OUTPUT = "tempfile.txt";
    private static final String FILE_NAME_DYNAMIC = "tempfile2.txt";
    private static final String FILE_NAME_DYNAMIC_EMPTY = "emptyfile.txt";
    private static final String FILE_NAME_NONE = "nonfile.txt";
    private static final String FILE_GLOBBING = "*.txt";
    private static final String TEXT_DYNAMIC = "This is a dynamic text for testing" + STRING_NEWLINE;
    public static final String STR_REDIR_OUTPUT = String.valueOf(CHAR_REDIR_OUTPUT);
    public static final String STR_REDIR_INPUT = String.valueOf(CHAR_REDIR_INPUT);
    public static final String STR_FLAG_PREFIX = String.valueOf(CHAR_FLAG_PREFIX);

    @TempDir
    File TEMP_DIRECTORY;

    @BeforeEach
    void setup() throws IOException {
        Environment.currentDirectory = TEMP_DIRECTORY.getAbsolutePath();

        String folderPrefix = TEMP_DIRECTORY + STR_FILE_SEP;

        FileWriter writer = new FileWriter(folderPrefix + FILE_NAME_1);
        writer.write("hello world");
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_2);
        writer.write("""
                banana
                apple
                orange
                grape
                kiwi
                """);
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_3);
        writer.write("""
                Hello World
                Hello World
                Alice
                Alice
                Bob
                Alice
                Bob
                """);
        writer.close();

        writer = new FileWriter(folderPrefix + FILE_NAME_4);
        writer.write("""
                alice
                Alice
                sam
                sAm
                saM""");
        writer.close();

        File folder1 = new File(folderPrefix + FOLDER_NAME_1);
        // Folder 2 does not exist
        File folder3 = new File(folderPrefix + FOLDER_NAME_3);
        assertTrue(folder1.mkdir());
        assertTrue(folder3.mkdir());

        // Folder 3 sub-folder and files
        String subFolderPrefix = folderPrefix + FOLDER_NAME_3 + STR_FILE_SEP;
        folder3 = new File(subFolderPrefix + FOLDER_NAME_1);
        assertTrue(folder3.mkdir());

        Files.copy(Path.of(folderPrefix + FILE_NAME_1), Path.of(subFolderPrefix + FILE_NAME_1));
        Files.copy(Path.of(folderPrefix + FILE_NAME_2), Path.of(subFolderPrefix + FILE_NAME_2));
        Files.copy(Path.of(folderPrefix + FILE_NAME_3), Path.of(subFolderPrefix + FILE_NAME_3));

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
    @Tag("CallCommandIT:Pairwise:1")
    public void callCommandIT_EchoCommandRedirectOutputSingleQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, CHAR_SINGLE_QUOTE + "hello world" + CHAR_SINGLE_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
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
    @Tag("CallCommandIT:Pairwise:2")
    public void callCommandIT_EchoCommandBackQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_ECHO, "`echo hello world`");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "hello world" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:3")
    public void callCommandIT_WcCommandRedirectInputDoubleQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_WC, STR_REDIR_INPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_3 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "\t7\t9\t50" + STRING_NEWLINE;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:4")
    public void callCommandIT_WcCommandRedirectOutputWithGlobbingBackQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_WC, STR_FLAG_PREFIX + FLAG_IS_WORD_COUNT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_GLOBBING + CHAR_BACK_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                \t2 file1.txt
                \t5 file2.txt
                \t9 file3.txt
                \t5 file4.txt
                \t0 tempfile.txt
                \t21 total
                """;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:5")
    public void callCommandIT_WcCommandRedirectInputAndOutputMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_WC, STR_FLAG_PREFIX + FLAG_IS_WORD_COUNT + FLAG_IS_LINE_COUNT, STR_REDIR_INPUT, FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected =
                "\t7\t9" +
                        STRING_NEWLINE;
        assertEquals(expected, fileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:6")
    public void callCommandIT_SortCommandRedirectInputWithGlobbingMultipleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_FLAG_PREFIX + FLAG_IS_REV_ORDER + FLAG_IS_CASE_IGNORE, STR_REDIR_INPUT, FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                sam
                saM
                sAm
                orange
                kiwi
                hello worldbanana
                Hello World
                Hello World
                grape
                Bob
                Bob
                apple
                alice
                Alice
                Alice
                Alice
                Alice
                """;
        String actual = outputStream.toString();
        assertEquals(expected.toUpperCase(), actual.toUpperCase());
    }

    @Test
    @Tag("CallCommandIT:Pairwise:8")
    public void callCommandIT_SortCommandRedirectInputAndOutputBackQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_REDIR_INPUT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_NAME_3 + CHAR_BACK_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Alice
                Alice
                Alice
                Bob
                Bob
                Hello World
                Hello World
                """;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:9")
    public void callCommandIT_SortCommandRedirectInputWithGlobbingSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_FLAG_PREFIX + FLAG_IS_REV_ORDER, STR_REDIR_INPUT, FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                sam
                saM
                sAm
                orange
                kiwi
                hello worldbanana
                grape
                apple
                alice
                Hello World
                Hello World
                Bob
                Bob
                Alice
                Alice
                Alice
                Alice
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:10")
    public void callCommandIT_SortCommandRedirectOutputSingleQuoteMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_SORT, STR_FLAG_PREFIX + FLAG_IS_REV_ORDER + FLAG_IS_FIRST_NUM + FLAG_IS_CASE_IGNORE, FILE_NAME_2, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                orange
                kiwi
                grape
                banana
                apple
                """;
        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:11")
    public void callCommandIT_CatCommandRedirectOutputWithGlobbingSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_FLAG_PREFIX + FLAG_IS_LINE_NUMBER, FILE_GLOBBING, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                \t1 hello world
                \t1 banana
                \t2 apple
                \t3 orange
                \t4 grape
                \t5 kiwi
                \t1 Hello World
                \t2 Hello World
                \t3 Alice
                \t4 Alice
                \t5 Bob
                \t6 Alice
                \t7 Bob
                \t1 alice
                \t2 Alice
                \t3 sam
                \t4 sAm
                \t5 saM
                """;

        assertEquals(expected, fileContent);

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:12")
    // NEGATIVE TEST CASE - PAIRWISE
    // RENAMED
    public void callCommandIT_CatCommandRedirectInputWithGlobbingDoubleQuoteSingleOption_ShouldReturnNegativeOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_FLAG_PREFIX + FLAG_IS_LINE_NUMBER , STR_REDIR_INPUT, CHAR_DOUBLE_QUOTE + FILE_GLOBBING + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);

        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(systemInputStream, outputStream));
        assertEquals(new ShellException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
    }

    @Test
    @Tag("CallCommandIT:Pairwise:13")
    public void callCommandIT_CatCommandRedirectInputBackQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_FLAG_PREFIX + FLAG_IS_LINE_NUMBER, STR_REDIR_INPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_2 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                \t1 banana
                \t2 apple
                \t3 orange
                \t4 grape
                \t5 kiwi
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:14")
    public void callCommandIT_CatCommandRedirectInputAndOutput_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CAT, STR_REDIR_INPUT, FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY.getAbsolutePath() + STR_FILE_SEP + FILE_NAME_OUTPUT);
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
    @Tag("CallCommandIT:Pairwise:15")
    public void callCommandIT_LsCommandRedirectOutputDoubleQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_LS, STR_FLAG_PREFIX + FLAG_IS_SORT_BY_EXT, STR_REDIR_OUTPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_OUTPUT + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = "folder1\n" +
                "folder3\n" +
                "file1.txt\n" +
                "file2.txt\n" +
                "file3.txt\n" +
                "file4.txt\n" +
                "tempfile.txt" +
                STRING_NEWLINE;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:16")
    public void callCommandIT_LsCommandWithGlobbing_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_LS, FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                file1.txt

                file2.txt

                file3.txt
                                
                file4.txt
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:17")
    public void callCommandIT_LsCommandRedirectOutputSingleQuoteMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_LS, STR_FLAG_PREFIX + FLAG_IS_SORT_BY_EXT + FLAG_IS_RECURSIVE, STR_REDIR_OUTPUT, CHAR_SINGLE_QUOTE + FILE_NAME_OUTPUT + CHAR_SINGLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                ./:
                folder1
                folder3
                file1.txt
                file2.txt
                file3.txt
                file4.txt
                tempfile.txt

                folder1:

                folder3:
                folder1
                file1.txt
                file2.txt
                file3.txt
                                
                folder3/folder1:
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:18")
    public void callCommandIT_PasteCommandBackQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, STR_FLAG_PREFIX + FLAG_IS_SERIAL, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + CHAR_SINGLE_QUOTE + FILE_NAME_1 + CHAR_SPACE + FILE_NAME_2 + CHAR_SINGLE_QUOTE + CHAR_BACK_QUOTE);
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
    @Tag("CallCommandIT:Pairwise:19")
    public void callCommandIT_PasteCommandRedirectInputAndOutputSingleQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, STR_FLAG_PREFIX + FLAG_IS_SERIAL, STR_REDIR_INPUT, CHAR_SINGLE_QUOTE + FILE_NAME_1 + CHAR_SINGLE_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
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
    @Tag("CallCommandIT:Pairwise:20")
    public void callCommandIT_PasteCommandRedirectOutputWithGlobbingSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, STR_FLAG_PREFIX + FLAG_IS_SERIAL, FILE_GLOBBING, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                hello world
                banana\tapple\torange\tgrape\tkiwi
                Hello World\tHello World\tAlice\tAlice\tBob\tAlice\tBob
                alice\tAlice\tsam\tsAm\tsaM
                """;
        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:21")
    public void callCommandIT_PasteCommandDoubleQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, CHAR_DOUBLE_QUOTE + FILE_NAME_2 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                banana
                apple
                orange
                grape
                kiwi
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:22")
    public void callCommandIT_UniqCommandRedirectInputWithGlobbingBackQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_UNIQ, STR_FLAG_PREFIX + FLAG_IS_COUNT, STR_REDIR_INPUT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_GLOBBING + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                \t1 hello worldbanana
                \t1 apple
                \t1 orange
                \t1 grape
                \t1 kiwi
                \t2 Hello World
                \t2 Alice
                \t1 Bob
                \t1 Alice
                \t1 Bob
                \t1 alice
                \t1 Alice
                \t1 sam
                \t1 sAm
                \t1 saM
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:23")
    public void callCommandIT_UniqCommandMultipleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_UNIQ, STR_FLAG_PREFIX + FLAG_IS_COUNT + FLAG_IS_DUPS, FILE_NAME_3);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                \t2 Hello World
                \t2 Alice
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:24")
    public void callCommandIT_UniqCommandRedirectInputSingleQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_UNIQ, STR_FLAG_PREFIX + FLAG_IS_COUNT, STR_REDIR_INPUT, CHAR_SINGLE_QUOTE + FILE_NAME_3 + CHAR_SINGLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                \t2 Hello World
                \t2 Alice
                \t1 Bob
                \t1 Alice
                \t1 Bob
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:25")
    public void callCommandIT_UniqCommandRedirectInputAndOutputBackQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_UNIQ, STR_REDIR_INPUT, FILE_NAME_3, STR_REDIR_OUTPUT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_NAME_OUTPUT + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Hello World
                Alice
                Bob
                Alice
                Bob
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:26")
    public void callCommandIT_UniqCommandRedirectOutputDoubleQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_UNIQ, FILE_NAME_3, STR_REDIR_OUTPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_OUTPUT + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Hello World
                Alice
                Bob
                Alice
                Bob
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:27")
    public void callCommandIT_CutCommandRedirectOutputSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CUT, STR_FLAG_PREFIX + FLAG_IS_BYTE_CUT, "1-8", FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Hello Wo
                Hello Wo
                Alice
                Alice
                Bob
                Alice
                Bob
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:28")
    public void callCommandIT_CutCommandDoubleQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CUT, STR_FLAG_PREFIX + FLAG_IS_BYTE_CUT, "1,3", CHAR_DOUBLE_QUOTE + FILE_NAME_3 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                Hl
                Hl
                Ai
                Ai
                Bb
                Ai
                Bb
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:29")
    public void callCommandIT_CutCommandRedirectInputDoubleQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CUT, STR_FLAG_PREFIX + FLAG_IS_CHAR_CUT, "1,3", STR_REDIR_INPUT, CHAR_DOUBLE_QUOTE + FILE_NAME_3 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                Hl
                Hl
                Ai
                Ai
                Bb
                Ai
                Bb
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:30")
    public void callCommandIT_CutCommandRedirectInputAndOutputWithGlobbingSingleQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CUT, STR_FLAG_PREFIX + FLAG_IS_CHAR_CUT, "1-99", STR_REDIR_INPUT, FILE_GLOBBING, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                hello worldbanana
                apple
                orange
                grape
                kiwi
                Hello World
                Hello World
                Alice
                Alice
                Bob
                Alice
                Bob
                alice
                Alice
                sam
                sAm
                saM
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:31")
    public void callCommandIT_CutCommandRedirectInputAndOutputSingleQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_CUT, STR_FLAG_PREFIX + FLAG_IS_CHAR_CUT, "1-99", STR_REDIR_INPUT, FILE_NAME_1, FILE_NAME_2, FILE_NAME_3, FILE_NAME_4, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                banana
                apple
                orange
                grape
                kiwi
                Hello World
                Hello World
                Alice
                Alice
                Bob
                Alice
                Bob
                alice
                Alice
                sam
                sAm
                saM
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }

    }

    @Test
    @Tag("CallCommandIT:Pairwise:32")
    public void callCommandIT_GrepCommandRedirectInputSingleQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, "Hello", STR_REDIR_INPUT, CHAR_SINGLE_QUOTE + FILE_NAME_3 + CHAR_SINGLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                Hello World
                Hello World
                  """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:33")
    public void callCommandIT_GrepCommandRedirectInputAndOutputBackQuoteMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, STR_FLAG_PREFIX + FLAG_IS_INCLUDE_FILENAME, STR_FLAG_PREFIX + FLAG_IS_CASING, "Hello", STR_REDIR_INPUT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_NAME_3 + CHAR_BACK_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                (standard input):Hello World
                (standard input):Hello World
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:34")
    public void callCommandIT_GrepCommandWithGlobbing_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, "Alice", FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                file3.txt:Alice
                file3.txt:Alice
                file3.txt:Alice
                file4.txt:Alice
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:35")
    public void callCommandIT_GrepCommandRedirectInputSingleQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, STR_FLAG_PREFIX + FLAG_IS_CASING, CHAR_SINGLE_QUOTE + "alice" + CHAR_SINGLE_QUOTE, STR_REDIR_INPUT, CHAR_SINGLE_QUOTE + FILE_NAME_4 + CHAR_SINGLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                alice
                Alice
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:36")
    public void callCommandIT_GrepCommandRedirectInputAndOutput_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, "Hello", STR_REDIR_INPUT, FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Hello World
                Hello World
                """;

        assertEquals(expected, fileContent);
        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @Tag("CallCommandIT:Pairwise:37")
    public void callCommandIT_GrepCommandRedirectOutputDoubleQuoteMultipleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, STR_FLAG_PREFIX + FLAG_IS_INCLUDE_FILENAME + FLAG_IS_COUNT, STR_FLAG_PREFIX + FLAG_IS_CASING, CHAR_DOUBLE_QUOTE + "Hello" + CHAR_DOUBLE_QUOTE, FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                file3.txt:2
                """;

        assertEquals(expected, fileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:38")
    public void callCommandIT_GrepCommandRedirectInputAndOutputWithGlobbingBackQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + "Hello" + CHAR_BACK_QUOTE, FILE_GLOBBING, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                file3.txt:Hello World
                file3.txt:Hello World
                """;

        assertEquals(expected, fileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:39")
    public void callCommandIT_GrepCommandSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, STR_FLAG_PREFIX + FLAG_IS_COUNT, "alice", FILE_NAME_4);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                1
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:40")
    public void callCommandIT_GrepCommandRedirectOutputDoubleQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, "World", CHAR_DOUBLE_QUOTE + FILE_NAME_3 + CHAR_DOUBLE_QUOTE, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Hello World
                Hello World
                """;

        assertEquals(expected, fileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:41")
    public void callCommandIT_GrepCommandRedirectInputAndOutputWithGlobbingBackQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + "Hello" + CHAR_BACK_QUOTE, STR_REDIR_INPUT, FILE_GLOBBING, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Hello World
                Hello World
                """;

        assertEquals(expected, fileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:42")
    public void callCommandIT_GrepCommandWithGlobbingMultipleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, STR_FLAG_PREFIX + FLAG_IS_COUNT, STR_FLAG_PREFIX + FLAG_IS_INCLUDE_FILENAME, STR_FLAG_PREFIX + FLAG_IS_CASING, "hello", FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                file1.txt:1
                file2.txt:0
                file3.txt:2
                file4.txt:0
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:43")
    public void callCommandIT_TeeCommandRedirectOutputSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        FileWriter writer = new FileWriter(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        writer.write(TEXT_DYNAMIC);
        writer.close();
        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        assertTrue(Files.exists(path));

        File teeInputFile = new File(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_2);
        InputStream teeInputStream = new FileInputStream(teeInputFile.getAbsolutePath());

        List<String> args = List.of(APP_TEE, STR_FLAG_PREFIX + FLAG_IS_APPEND, FILE_NAME_DYNAMIC);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(teeInputStream, outputStream);

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                This is a dynamic text for testing
                banana
                apple
                orange
                grape
                kiwi
                """;

        assertEquals(expected, fileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:44")
    public void callCommandIT_TeeCommandDoubleQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        FileWriter writer = new FileWriter(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        writer.write(TEXT_DYNAMIC);
        writer.close();
        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        assertTrue(Files.exists(path));

        File teeInputFile = new File(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_2);
        InputStream teeInputStream = new FileInputStream(teeInputFile.getAbsolutePath());

        List<String> args = List.of(APP_TEE, CHAR_DOUBLE_QUOTE + FILE_NAME_DYNAMIC + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(teeInputStream, outputStream);

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                banana
                apple
                orange
                grape
                kiwi
                """;

        assertEquals(expected, fileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:45")
    public void callCommandIT_TeeCommandRedirectInputWithGlobbingSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_TEE, STR_FLAG_PREFIX + FLAG_IS_APPEND, STR_REDIR_INPUT, FILE_GLOBBING);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String actual = outputStream.toString();
        String expected = """
                hello worldbanana
                apple
                orange
                grape
                kiwi
                Hello World
                Hello World
                Alice
                Alice
                Bob
                Alice
                Bob
                alice
                Alice
                sam
                sAm
                saM""";
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:46")
    public void callCommandIT_TeeCommandRedirectInputBackQuoteSingleOption_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        FileWriter writer = new FileWriter(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        writer.write(TEXT_DYNAMIC);
        writer.close();
        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        assertTrue(Files.exists(path));

        List<String> args = List.of(APP_TEE, FILE_NAME_DYNAMIC, STR_REDIR_INPUT, FILE_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String fileContent = new String(Files.readAllBytes(path));
        String actual = outputStream.toString();
        String expected = """
                banana
                apple
                orange
                grape
                kiwi
                """;
        assertEquals(expected, fileContent);
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:47")
    public void callCommandIT_TeeCommandRedirectInputAndOutputSingleQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        FileWriter writer = new FileWriter(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        writer.write(TEXT_DYNAMIC);
        writer.close();
        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_DYNAMIC);
        assertTrue(Files.exists(path));

        File teeInputFile = new File(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_2);
        InputStream teeInputStream = new FileInputStream(teeInputFile.getAbsolutePath());

        List<String> args = List.of(APP_TEE, FILE_NAME_DYNAMIC, STR_REDIR_INPUT, FILE_NAME_4, STR_REDIR_OUTPUT, CHAR_SINGLE_QUOTE + FILE_NAME_OUTPUT + CHAR_SINGLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(teeInputStream, outputStream);

        Path outputFilePath = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(outputFilePath));

        String fileContent = new String(Files.readAllBytes(path));
        String outputFileContent = new String(Files.readAllBytes(outputFilePath));
        String expected = """
                alice
                Alice
                sam
                sAm
                saM""";
        assertEquals(expected, fileContent);
        assertEquals(expected, outputFileContent);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:48")
    public void callCommandIT_TeeCommandRedirectOutputWithGlobbing_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        File teeInputFile = new File(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_2);
        InputStream teeInputStream = new FileInputStream(teeInputFile.getAbsolutePath());

        List<String> args = List.of(APP_TEE, FILE_GLOBBING, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(teeInputStream, outputStream);

        String file1 = new String(Files.readAllBytes(Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_1)));
        String file2 = new String(Files.readAllBytes(Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_2)));
        String file3 = new String(Files.readAllBytes(Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_3)));
        String file4 = new String(Files.readAllBytes(Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_4)));
        String fileOutput = new String(Files.readAllBytes(Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT)));
        String expected = """
                banana
                apple
                orange
                grape
                kiwi
                """;
        assertEquals(expected, file1);
        assertEquals(expected, file2);
        assertEquals(expected, file3);
        assertEquals(expected, file4);
        assertEquals(expected, fileOutput);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:49")
    public void callCommandIT_TeeCommandRedirectInputWithGlobbingBackQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_TEE, STR_REDIR_INPUT, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_GLOBBING + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String actual = outputStream.toString();

        String expected = """
                hello worldbanana
                apple
                orange
                grape
                kiwi
                Hello World
                Hello World
                Alice
                Alice
                Bob
                Alice
                Bob
                alice
                Alice
                sam
                sAm
                saM""";
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:50")
    public void callCommandIT_MkdirCommandBackQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_MKDIR, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FOLDER_NAME_2 + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path outputFilePath = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2);
        assertTrue(Files.exists(outputFilePath));
    }

    @Test
    @Tag("CallCommandIT:Pairwise:51")
    public void callCommandIT_MvCommandWithGlobbingSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String targetFolder = TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2 + STR_FILE_SEP;

        File folder1 = new File(targetFolder);
        assertTrue(folder1.mkdir());

        List<String> args = List.of(APP_MV, FILE_GLOBBING, FOLDER_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path file = Path.of(targetFolder + FILE_NAME_1);
        assertTrue(Files.exists(file));

        file = Path.of(targetFolder + FILE_NAME_2);
        assertTrue(Files.exists(file));

        file = Path.of(targetFolder + FILE_NAME_3);
        assertTrue(Files.exists(file));

        file = Path.of(targetFolder + FILE_NAME_4);
        assertTrue(Files.exists(file));
    }

    @Test
    @Tag("CallCommandIT:Pairwise:52")
    public void callCommandIT_RmCommandDoubleQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String targetFile = TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2 + FILE_NAME_1;

        List<String> args = List.of(APP_RM, CHAR_DOUBLE_QUOTE + FILE_NAME_1 + CHAR_DOUBLE_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        assertFalse(Files.exists(Path.of(targetFile)));
    }

    @Test
    @Tag("CallCommandIT:Pairwise:53")
    public void callCommandIT_RmCommandBackQuoteMultipleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_RM, STR_FLAG_PREFIX + RmArgsParser.FLAG_IS_RECURSIVE + FLAG_IS_EMPTY_FOLDER, TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_3);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        assertFalse(Files.exists(Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_3)));
    }

    @Test
    @Tag("CallCommandIT:Pairwise:54")
    public void callCommandIT_RmCommandBackQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String targetFile = TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2 + FILE_NAME_1;

        List<String> args = List.of(APP_RM, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_NAME_1 + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        assertFalse(Files.exists(Path.of(targetFile)));
    }

    @Test
    @Tag("CallCommandIT:Pairwise:55")
    public void callCommandIT_PasteCommandWithGlobbingBackQuoteSingleOption_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_PASTE, STR_FLAG_PREFIX + FLAG_IS_SERIAL, CHAR_BACK_QUOTE + APP_ECHO + CHAR_SPACE + FILE_GLOBBING + CHAR_BACK_QUOTE);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                hello world
                banana\tapple\torange\tgrape\tkiwi
                Hello World\tHello World\tAlice\tAlice\tBob\tAlice\tBob
                alice\tAlice\tsam\tsAm\tsaM
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:56")
    public void callCommandIT_GrepCommandDoubleQuote_ShouldReturnCorrectResult() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, CHAR_DOUBLE_QUOTE + "Hello" + CHAR_DOUBLE_QUOTE, FILE_NAME_3);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        String expected = """
                Hello World
                Hello World
                """;
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }

    @Test
    @Tag("CallCommandIT:Pairwise:57")
    public void callCommandIT_GrepCommandRedirectInputAndOutputSingleQuote_ShouldReturnCorrectResult() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_GREP, CHAR_SINGLE_QUOTE + "Hello" + CHAR_SINGLE_QUOTE, STR_REDIR_INPUT, FILE_NAME_3, STR_REDIR_OUTPUT, FILE_NAME_OUTPUT);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_OUTPUT);
        assertTrue(Files.exists(path));

        String fileContent = new String(Files.readAllBytes(path));
        String expected = """
                Hello World
                Hello World
                """;

        assertEquals(expected, fileContent);
    }

    // POSITIVE TEST CASE - SIMPLE
    @Test
    public void callCommandIT_SingleEchoCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("echo", "hello", "world");
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
        String expected = String.join(STRING_NEWLINE, new String[]{"orange", "kiwi", "grape", "banana", "apple"}) + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleCatCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cat", "<", FILE_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = String.join(STRING_NEWLINE, new String[]{"banana", "apple", "orange", "grape", "kiwi",}) + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleCdCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cd", FOLDER_NAME_1);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expectedDir = TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_1;
        assertEquals(expectedDir, Environment.currentDirectory);
    }

    @Test
    public void callCommandIT_SingleLsCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("ls");
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = String.join(STRING_NEWLINE, new String[]{FILE_NAME_1, FILE_NAME_2, FILE_NAME_3, FILE_NAME_4, FOLDER_NAME_1, FOLDER_NAME_3}) + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleGrepCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("grep", "Hello World", FILE_NAME_3);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "Hello World" + STRING_NEWLINE + "Hello World" + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
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
        String expected = CHAR_TAB + "2 Hello World" + STRING_NEWLINE +
                CHAR_TAB + "2 Alice\n" +
                CHAR_TAB + "1 Bob\n" +
                CHAR_TAB + "1 Alice\n" +
                CHAR_TAB + "1 Bob\n";
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleCutCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of("cut", "-c", "1,2", FILE_NAME_1);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        String expected = "he\n";
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleMkDirCommand_ShouldReturnCorrectOutput() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of("mkdir", FOLDER_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);
        Path path = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2);
        assertTrue(Files.exists(path));

        // Clean
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    public void callCommandIT_SingleTeeCommand_ShouldReturnCorrectOutput() throws IOException, AbstractApplicationException, ShellException {
        List<String> args = List.of("tee", STR_FLAG_PREFIX + FLAG_IS_APPEND, FILE_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);

        String targetFile = TEMP_DIRECTORY + STR_FILE_SEP + FILE_NAME_2;

        File teeInputFile = new File(targetFile);
        InputStream teeInputStream = new FileInputStream(teeInputFile.getAbsolutePath());
        callCommand.evaluate(teeInputStream, outputStream);

        String fileContent = new String(Files.readAllBytes(Path.of(targetFile)));
        String expected = """
                banana
                apple
                orange
                grape
                kiwi
                banana
                apple
                orange
                grape
                kiwi
                """;
        assertEquals(expected, fileContent);
        expected = """
                banana
                apple
                orange
                grape
                kiwi
                """;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void callCommandIT_SingleMkdirCommand_ShouldReturnCorrectOutput() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> args = List.of(APP_MKDIR, FOLDER_NAME_2);
        callCommand = new CallCommand(args, applicationRunner, argumentResolver);
        callCommand.evaluate(systemInputStream, outputStream);

        Path outputFilePath = Path.of(TEMP_DIRECTORY + STR_FILE_SEP + FOLDER_NAME_2);
        assertTrue(Files.exists(outputFilePath));
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
        String expected = "sort: No such file or directory";
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
        String expected = "wc: " + FILE_NAME_NONE + ": " + "No such file or directory" + STRING_NEWLINE;
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
