package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class UniqApplication implements UniqInterface {
    private OutputStream outputStream;
    InputStream stdin;

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws UniqException {

        if (stdin == null || stdout == null) {
            throw new UniqException(ERR_NULL_STREAMS);
        }

        this.stdin = stdin;
        // Parse arguments
        UniqArgsParser parser = new UniqArgsParser();
        String inputFileName = parser.getInputFile();
        String outputFileName = parser.getOutputFile();
        String output;
        outputStream = stdout;
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new UniqException(e.getMessage());
        }

        // Perform uniq operation
        try {
            if (inputFileName == null) {
                output = uniqFromStdin(parser.isCount(), parser.isOnlyDuplicates(), parser.isAllDuplicates(), stdin, parser.getOutputFile());
            } else {
                output = uniqFromFile(parser.isCount(), parser.isOnlyDuplicates(), parser.isAllDuplicates(), parser.getInputFile(), parser.getOutputFile());
            }
        } catch (Exception e) {
            throw new UniqException(e.getMessage());
        }

        if (outputFileName == null && stdout == null) {
            throw new UniqException(ERR_NO_OSTREAM);
        }

        // Write output
        writeOutput(output, parser.getOutputFile(), stdout);
    }

    private void writeOutput(String output, String outputFile, OutputStream stdout) throws UniqException {
        try {
            if (!output.isEmpty()) {
                if (outputFile != null) {
                    Path outputPath = IOUtils.resolveFilePath(outputFile);
                    Files.write(outputPath, output.getBytes());
                } else {
                    stdout.write(output.getBytes());
                }
            }
        } catch (IOException e) {
            throw new UniqException("Error writing to output stream: " + e.getMessage());
        }
    }

    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isOnlyDuplicates     Boolean option to print only duplicate lines, one for each group
     * @param isAllDuplicates  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param inputFileName  of path to input file
     * @param outputFileName of path to output file (if any)
     * @throws AbstractApplicationException
     */
    @Override
    public String uniqFromFile(Boolean isCount, Boolean isOnlyDuplicates, Boolean isAllDuplicates, String inputFileName, String outputFileName) throws AbstractApplicationException, IOException {

        if (inputFileName == null) {
            throw new UniqException(ERR_NO_INPUT);
        }

        List<String> fileLines = new ArrayList<>();
        //if only "-"
        if (inputFileName.length() == 1 && inputFileName.toCharArray()[0] == '-') {
            List<String> linesFromInput = IOUtils.getLinesFromInputStream(stdin);
            fileLines.addAll(linesFromInput);

            return uniqInputString(isCount, isOnlyDuplicates, isAllDuplicates, fileLines, outputFileName);
        }
        File inputFile = IOUtils.resolveFilePath(inputFileName).toFile();
        if (!inputFile.exists()) {
            throw new UniqException(inputFileName + ": " + ERR_FILE_NOT_FOUND);
        }
        if (inputFile.isDirectory()) {
            throw new UniqException(inputFileName + ": " + ERR_IS_DIR);
        }
        if (!inputFile.canRead()) {
            throw new UniqException(inputFileName + ": " + ERR_NO_PERM);
        }

        try {
            InputStream input = IOUtils.openInputStream(inputFileName);
            fileLines = IOUtils.getLinesFromInputStream(input);
            IOUtils.closeInputStream(input);
        }
        catch (ShellException e) {
            throw new UniqException(ERR_NULL_STREAMS);
        }
        return uniqInputString(isCount, isOnlyDuplicates, isAllDuplicates, fileLines, outputFileName);
    }

    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param stdin          InputStream containing arguments from Stdin
     * @param outputFileName of path to output file (if any)
     * @throws AbstractApplicationException
     */
    @Override
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String outputFileName) throws AbstractApplicationException {
        List<String> stdinLines;
        try {
            stdinLines = IOUtils.getLinesFromInputStream(stdin);
        } catch (IOException e) {
            throw new UniqException(ERR_IO_EXCEPTION);
        }
        return uniqInputString(isCount, isRepeated, isAllRepeated, stdinLines, outputFileName);
    }

    public String uniqInputString(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, List<String> input, String outputFileName) {
        StringBuilder output = new StringBuilder();
        String prevString = null;
        int count = 0;

        for (String s : input) {
            if (prevString == null || !prevString.equals(s)) {
                if (prevString != null) {
                    appendOutput(output, prevString, count, isCount, isRepeated, isAllRepeated);
                }
                prevString = s;
                count = 1;
            } else {
                count++;
            }
        }

        // Append the last set of lines
        if (prevString != null) {
            appendOutput(output, prevString, count, isCount, isRepeated, isAllRepeated);
        }

        return output.toString();
    }

    private void appendOutput(StringBuilder output, String line, int count, boolean isCount, boolean isRepeated, boolean isAllRepeated) {
        if (isAllRepeated || (isRepeated && count > 1)) {
            if (isCount) {
                for (int i = 0; i < count; i++) {
                    output.append(count).append(" ").append(line).append(STRING_NEWLINE);
                }
            } else {
                output.append(line.repeat(count)).append(STRING_NEWLINE);
            }
        } else if (isCount) {
            output.append(count).append(" ").append(line).append(STRING_NEWLINE);
        } else {
            output.append(line).append(STRING_NEWLINE);
        }
    }
}
