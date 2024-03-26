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
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

@SuppressWarnings("PMD.PreserveStackTrace") // Stacktrace part of implementation
public class UniqApplication implements UniqInterface { //NOPMD - suppressed GodClass - Application
    InputStream stdin;

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws UniqException {
        if (stdout == null || stdin == null) {
            throw new UniqException(ERR_NULL_STREAMS);
        }
        this.stdin = stdin;

        UniqArgsParser parser = new UniqArgsParser();
        String output;
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new UniqException(e.getMessage());
        }

        String outputFile= parser.getOutputFile();
        String inputFile = parser.getInputFile();

        try {
            if (inputFile == null || "-".equals(inputFile)) {
                output = uniqFromStdin(parser.isCount(), parser.isOnlyDuplicates(), parser.isAllDuplicates(), stdin, outputFile);
            } else {
                output = uniqFromFile(parser.isCount(), parser.isOnlyDuplicates(), parser.isAllDuplicates(), inputFile, outputFile);
            }
        } catch (Exception e) {
            throw new UniqException(e.getMessage());
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

                    // similar to actual shell, need to have a newline
                    stdout.write(STRING_NEWLINE.getBytes());
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
     * @param isCount          Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param inputFileName    of path to input file
     * @param outputFileName   of path to output file (if any)
     * @throws AbstractApplicationException
     */
    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String outputFileName) throws AbstractApplicationException, IOException {
        List<String> fileLines = new ArrayList<>();
        if (inputFileName == null) {
            throw new UniqException(ERR_NO_INPUT);
        }

        File inputFile = IOUtils.resolveFilePath(inputFileName).toFile();
        if (inputFile.isDirectory()) {
            throw new UniqException(inputFileName + ": " + ERR_IS_DIR);
        }
        if (!inputFile.exists()) {
            throw new UniqException(inputFileName + ": " + ERR_FILE_NOT_FOUND);
        }
        if (!inputFile.canRead()) {
            throw new UniqException(inputFileName + ": " + ERR_NO_PERM);
        }

        try {
            InputStream input = IOUtils.openInputStream(inputFileName); //NOPMD - suppressed CloseResource - Already Close
            fileLines = IOUtils.getLinesFromInputStream(input);
            IOUtils.closeInputStream(input);
        } catch (ShellException e) {
            throw new UniqException(ERR_NULL_STREAMS);
        }
        return uniqProcessInputString(isCount, isRepeated, isAllRepeated, fileLines);
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
        } catch (NullPointerException e) {
            throw new UniqException(e.getMessage());
        }
        return uniqProcessInputString(isCount, isRepeated, isAllRepeated, stdinLines);
    }


    // Main method to process the input and generate the output string.
    public String uniqProcessInputString(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, List<String> input) {
        List<String> lines = new ArrayList<>();
        List<Integer> count = new ArrayList<>();
        countOccurrences(input, lines, count);

        return generateOutput(isCount, isRepeated, isAllRepeated, lines, count);
    }

    // Method to count occurrences of each string in the input.
    private void countOccurrences(List<String> inputs, List<String> distinctStrings, List<Integer> frequencies) {
        if (inputs == null || inputs.isEmpty()) {
            return;
        }

        String previousString = null;
        int occurrenceCount = 0;

        for (String currentString : inputs) {
            if (previousString == null || previousString.equals(currentString)) {
                occurrenceCount++;
            } else {
                distinctStrings.add(previousString);
                frequencies.add(occurrenceCount);
                occurrenceCount = 1;
            }
            previousString = currentString;
        }

        if (previousString != null) {
            distinctStrings.add(previousString);
            frequencies.add(occurrenceCount);
        }
    }

    // Method to generate the output string based on the given flags.
    private String generateOutput(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, List<String> lines, List<Integer> count) {
        StringBuilder output = new StringBuilder();
        // special case for empty input files
        if (lines.isEmpty()) {
            return "\n";
        }
        for (int i = 0; i < lines.size(); i++) {
            if (isAllRepeated) {
                if (count.get(i) > 1) {
                    appendLines(output, isCount, lines.get(i), count.get(i));
                }
            } else if (isRepeated) {
                if (count.get(i) > 1) {
                    appendLine(output, isCount, lines.get(i), count.get(i));
                }
            } else {
                appendLine(output, isCount, lines.get(i), count.get(i));
            }
        }
        return output.toString();
    }

    // Helper method to append lines to the output, used for repeated and all repeated scenarios.
    private void appendLines(StringBuilder output, Boolean isCount, String line, int count) {
        for (int j = 0; j < count; j++) {
            appendLine(output, isCount, line, count);
        }
    }

    // Helper method to append a single line to the output.
    private void appendLine(StringBuilder output, Boolean isCount, String line, int count) {
        if (isCount) {
            output.append(CHAR_TAB).append(count).append(CHAR_SPACE);
        }
        output.append(line).append(STRING_NEWLINE);
    }
}


