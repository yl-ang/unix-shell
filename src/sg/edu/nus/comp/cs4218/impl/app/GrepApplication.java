package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class GrepApplication implements GrepInterface {
    public static final String INVALID_PATTERN = "Invalid pattern syntax";
    public static final String EMPTY_PATTERN = "Pattern should not be empty.";
    public static final String IS_DIRECTORY = "Is a directory";
    public static final String NULL_POINTER = "Null Pointer Exception";

    private static final int NUM_ARGUMENTS = 3;
    private static final char CASE_INSEN_IDENT = 'i';
    private static final char COUNT_IDENT = 'c';
    private static final char PREFIX_FN = 'H';
    private static final int CASE_INSEN_IDX = 0;
    private static final int COUNT_INDEX = 1;
    private static final int PREFIX_FN_IDX = 2;


    /**
     * Returns string containing lines which match the specified pattern in the given files
     *
     * @param pattern           String specifying a regular expression in JAVA format
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isCountLines      Boolean option to only write out a count of matched lines
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param fileNames         Array of file names (not including "-" for reading from stdin)
     * @throws Exception
     */
    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, String... fileNames) throws Exception {
        if (fileNames == null || pattern == null) {
            throw new GrepException(NULL_POINTER);
        }

        StringJoiner lineResults = new StringJoiner(STRING_NEWLINE);
        StringJoiner countResults = new StringJoiner(STRING_NEWLINE);

        grepResultsFromFiles(pattern, isCaseInsensitive, isPrefixFileName, lineResults, countResults, fileNames);

        String results = "";
        if (isCountLines) {
            results = countResults.toString() + STRING_NEWLINE;
        } else {
            if (!lineResults.toString().isEmpty()) {
                results = lineResults.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    /**
     * Extract the lines and count number of lines for grep from files
     * lineResults and countResults respectively.
     *
     * @param pattern           Pattern to grep with
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param lineResults       Result of String after grepping file(s)
     * @param countResults      a StringJoiner of the grep line count results
     * @param files             Array of String of files
     */
    private void grepResultsFromFiles(String pattern, boolean isCaseInsensitive, boolean isPrefixFileName,
                                      StringJoiner lineResults, StringJoiner countResults, String... files) throws Exception {
        boolean forcePrefix = files.length > 1;

        for (String filePath : files) {
            if (Objects.equals(filePath, "-")) { // Skip special handling for stdin
                continue;
            }

            File file = getFileIfValid(filePath, lineResults, countResults);
            if (file != null) {
                Pattern compiledPattern = compilePattern(pattern, isCaseInsensitive);
                try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.toURI()))) {
                    processFile(filePath, reader, compiledPattern, forcePrefix || isPrefixFileName, lineResults, countResults);
                } catch (Exception e) {
                    System.err.println("Error processing file " + filePath + ": " + e.getMessage());
                    // Optionally re-throw or handle the exception based on requirements
                }
            }
        }
    }

    private File getFileIfValid(String filePath, StringJoiner lineResults, StringJoiner countResults) {
        File file = new File(filePath);
        if (!file.exists()) {
            String errorMessage = "grep: " + filePath + ": File not found";
            lineResults.add(errorMessage);
            countResults.add(errorMessage);
            return null;
        }
        if (file.isDirectory()) {
            String directoryMessage = "grep: " + filePath + ": Is a directory";
            lineResults.add(directoryMessage);
            countResults.add(directoryMessage);
            countResults.add(filePath + ": 0");
            return null;
        }
        return file;
    }

    private Pattern compilePattern(String pattern, boolean isCaseInsensitive) {
        int flags = isCaseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
        return Pattern.compile(pattern, flags);
    }

    private void processFile(String filePath, BufferedReader reader, Pattern pattern, boolean includeFileName,
                             StringJoiner lineResults, StringJoiner countResults) throws Exception {
        String line;
        int matches = 0;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                lineResults.add(includeFileName ? filePath + ": " + line : line);
                matches++;
            }
        }
        countResults.add(includeFileName ? filePath + ": " + matches : String.valueOf(matches));
    }

    /**
     * Converts filename to absolute path, if initially was relative path
     *
     * @param fileName supplied by user
     * @return a String of the absolute path of the filename
     */
    private String convertToAbsolutePath(String fileName) {
        String home = System.getProperty("user.home").trim();
        String currentDir = Environment.currentDirectory.trim();
        String convertedPath = convertPathToSystemPath(fileName);

        String newPath;
        if (convertedPath.length() >= home.length() && convertedPath.substring(0, home.length()).trim().equals(home)) {
            newPath = convertedPath;
        } else {
            newPath = currentDir + CHAR_FILE_SEP + convertedPath;
        }
        return newPath;
    }

    /**
     * Converts path provided by user into path recognised by the system
     *
     * @param path supplied by user
     * @return a String of the converted path
     */
    private String convertPathToSystemPath(String path) {
        String convertedPath = path;
        String pathIdentifier = "\\" + Character.toString(CHAR_FILE_SEP);
        convertedPath = convertedPath.replaceAll("(\\\\)+", pathIdentifier);
        convertedPath = convertedPath.replaceAll("/+", pathIdentifier);

        if (convertedPath.length() != 0 && convertedPath.charAt(convertedPath.length() - 1) == CHAR_FILE_SEP) {
            convertedPath = convertedPath.substring(0, convertedPath.length() - 1);
        }

        return convertedPath;
    }

    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws AbstractApplicationException {
        // TODO: To implement -H flag print file name with output lines
        int count = 0;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            String line;
            Pattern compiledPattern;
            if (isCaseInsensitive) {
                compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } else {
                compiledPattern = Pattern.compile(pattern);
            }
            while ((line = reader.readLine()) != null) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) { // match
                    stringJoiner.add(line);
                    count++;
                }
            }
            reader.close();
        } catch (PatternSyntaxException pse) {
            throw new GrepException(ERR_INVALID_REGEX);
        } catch (NullPointerException npe) {
            throw new GrepException(ERR_FILE_NOT_FOUND);
        } catch (IOException e) {
            throw new GrepException(ERR_IO_EXCEPTION);
        }

        String results = "";
        if (isCountLines) {
            results = count + STRING_NEWLINE;
        } else {
            if (!stringJoiner.toString().isEmpty()) {
                results = stringJoiner.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        try {
            GrepArgsParser parser = new GrepArgsParser();
            boolean[] grepFlags = new boolean[NUM_ARGUMENTS]; // Assuming this is defined elsewhere
            parser.parse(args); // Adjusted to use the parse method as in your friend's code

            String pattern = parser.getPattern();
            ArrayList<String> inputFilesList = parser.getFileNames(); // Assuming getFileNames now returns ArrayList<String> directly
            grepFlags[CASE_INSEN_IDX] = parser.isCaseInsensitive();
            grepFlags[COUNT_INDEX] = parser.isCount();
            grepFlags[PREFIX_FN_IDX] = parser.isPrintFileName();

            if (stdin == null && inputFilesList.isEmpty()) {
                throw new Exception(ERR_NO_INPUT);
            }
            if (pattern == null) {
                throw new Exception(ERR_SYNTAX);
            }
            if (pattern.isEmpty()) {
                throw new Exception(EMPTY_PATTERN);
            }

            String result = "";
            if (inputFilesList.isEmpty()) {
                result = grepFromStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin);
            } else {
                String[] inputFilesArray = inputFilesList.toArray(new String[0]);

                Boolean toReadFromStdin = inputFilesList.contains(String.valueOf(CHAR_FLAG_PREFIX));
                Boolean toReadFromFiles = inputFilesList.stream().anyMatch(fileName -> !Objects.equals(fileName, String.valueOf(CHAR_FLAG_PREFIX)));

                if (toReadFromFiles && toReadFromStdin) {
                    result = grepFromFileAndStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin, inputFilesArray);
                } else if (toReadFromFiles) {
                    result = grepFromFiles(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], inputFilesArray);
                } else if (toReadFromStdin) {
                    result = grepFromStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin);
                }
            }
            stdout.write(result.getBytes());
        } catch (GrepException grepException) {
            throw grepException;
        } catch (Exception e) {
            throw new GrepException(e.getMessage());
        }
    }


    /**
     * Separates the arguments provided by user into the flags, pattern and input files.
     *
     * @param args       supplied by user
     * @param grepFlags  a bool array of possible flags in grep
     * @param inputFiles a ArrayList<String> of file names supplied by user
     * @return regex pattern supplied by user. An empty String if not supplied.
     */
    private String getGrepArguments(String[] args, boolean[] grepFlags, ArrayList<String> inputFiles) throws AbstractApplicationException {
        String pattern = null;
        boolean isFile = false; // files can only appear after pattern

        for (String s : args) {
            char[] arg = s.toCharArray();
            if (isFile) {
                inputFiles.add(s);
            } else {
                if (!s.isEmpty() && arg[0] == CHAR_FLAG_PREFIX) {
                    arg = Arrays.copyOfRange(arg, 1, arg.length);
                    for (char c : arg) {
                        switch (c) {
                            case CASE_INSEN_IDENT:
                                grepFlags[CASE_INSEN_IDX] = true;
                                break;
                            case COUNT_IDENT:
                                grepFlags[COUNT_INDEX] = true;
                                break;
                            case PREFIX_FN:
                                grepFlags[PREFIX_FN_IDX] = true;
                                break;
                            default:
                                throw new GrepException(ERR_SYNTAX);
                        }
                    }
                } else { // pattern must come before file names
                    pattern = s;
                    isFile = true; // next arg onwards will be file
                }
            }
        }
        return pattern;
    }

    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin, String... fileNames) throws AbstractApplicationException {
        // TODO: To implement
        return null;
    }
}
