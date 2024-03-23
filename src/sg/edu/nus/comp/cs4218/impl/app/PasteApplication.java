package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class PasteApplication implements PasteInterface  {
    public static final String ERR_READING_FILE = "Could not read file";
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";

    /**
     * Executes the paste command with the provided arguments, input stream, and output stream.
     *
     * @param args   Array of arguments provided to the paste command.
     * @param stdin  InputStream from which to read input, can be null.
     * @param stdout OutputStream to which the result of the paste command is written.
     * @throws AbstractApplicationException If an error occurs during the execution of the paste command.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new PasteException(ERR_NO_OSTREAM);
        }

        PasteArgsParser parser = new PasteArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new PasteException(e.getMessage());
        }

        String[] fileNames = parser.getFileNames().toArray(new String[0]);

        // This means that no stdin and no files supplied which shouldn't happen
        if (stdin == null && fileNames.length == 0) {
            throw new PasteException(ERR_NO_INPUT);
        }

        String mergedStr = "";
        try {
            if (fileNames.length > 0 && List.of(fileNames).contains(STRING_FLAG_PREFIX)) {
                mergedStr = mergeFileAndStdin(parser.isSerial(), stdin, fileNames);
            } else if (fileNames.length > 0) {
                mergedStr = mergeFile(parser.isSerial(), fileNames);
            } else {
                mergedStr = mergeStdin(parser.isSerial(), stdin);
            }
        } catch (Exception e) {
            throw new PasteException(e.toString());
        }

        try {
            stdout.write(mergedStr.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new PasteException(ERR_WRITE_STREAM);
        }
    }

    /**
     * Merges the lines from the provided input stream according to the specified mode.
     *
     * @param isSerial Flag indicating whether to merge the lines serially or in parallel.
     * @param stdin    InputStream containing the lines to be merged.
     * @return String representing the merged lines.
     * @throws AbstractApplicationException If an error occurs during the merging process.
     */
    @Override
    public String mergeStdin(Boolean isSerial, InputStream stdin) throws AbstractApplicationException {

        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }
        try {
            List<String> stdinLines = IOUtils.getLinesFromInputStream(stdin);
            StringBuilder mergedLines = new StringBuilder();

            if (isSerial) {
                appendLines(mergedLines, stdinLines);
                if (!mergedLines.isEmpty()) {
                    mergedLines.deleteCharAt(mergedLines.length() - 1);
                }
            } else {
                int size = stdinLines.size();
                for (int i = 0; i < size; i++) {
                    String line = stdinLines.get(i);
                    mergedLines.append(line);
                    if (i < size - 1) {
                        mergedLines.append(STRING_NEWLINE);
                    }
                }
            }
            return mergedLines.toString();
        } catch (IOException e) {
            throw new PasteException(ERR_READING_STREAM);
        }
    }

    /**
     * Merges the lines from the specified files according to the specified mode.
     *
     * @param isSerial   Flag indicating whether to merge the lines serially or in parallel.
     * @param fileNames Array of file names from which to read lines to be merged.
     * @return String representing the merged lines.
     * @throws AbstractApplicationException If an error occurs during the merging process.
     */
    @Override
    public String mergeFile(Boolean isSerial, String... fileNames) throws AbstractApplicationException {

        if (fileNames == null || fileNames.length == 0) {
            throw new PasteException(ERR_NO_FILE_ARGS);
        }

        List<List<String>> allLines = new ArrayList<>();
        InputStream fileStream = null;
        String currentDirectory = Environment.currentDirectory;
        Path pathToFile;

        for (String fileName : fileNames) {
            File node;
            try {
                pathToFile = Paths.get(fileName);

                if (!pathToFile.isAbsolute()) {
                    pathToFile = Paths.get(currentDirectory).resolve(fileName);
                }
                node = pathToFile.toFile();
                if (!node.exists()){
                    throw new PasteException(pathToFile.getFileName() + ": No such file or directory");
                }
                if (node.isDirectory()) {
                    throw new PasteException(pathToFile.getFileName() + ": Is a directory");
                }
                if (!node.canRead()) {
                    throw new PasteException(pathToFile.getFileName() + ": " + ERR_NO_PERM_READ_FILE);
                }
                InputStream inputStream = IOUtils.openInputStream(fileName);
                List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
                allLines.add(lines);
            } catch (IOException | ShellException e) {
                throw new PasteException(e.getMessage());
            } finally {
                try {
                    IOUtils.closeInputStream(fileStream);
                } catch (ShellException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return mergeLines(allLines, isSerial);
    }

    /**
     * Merges the lines from the provided input stream and files according to the specified mode.
     *
     * @param isSerial   Flag indicating whether to merge the lines serially or in parallel.
     * @param stdin      InputStream containing the lines to be merged.
     * @param fileNames  Array of file names from which to read lines to be merged.
     * @return String representing the merged lines.
     * @throws AbstractApplicationException If an error occurs during the merging process.
     */
    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileNames)
            throws AbstractApplicationException {

        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new PasteException(ERR_NO_FILE_ARGS);
        }

        List<String> stdinLines;
        try {
            stdinLines = IOUtils.getLinesFromInputStream(stdin);
        } catch (IOException e) {
            throw new PasteException(ERR_READING_STREAM);
        }

        int totalFlags = countOccurrences(fileNames, STRING_FLAG_PREFIX);

        List<List<String>> allLines = processInput(stdinLines, fileNames, totalFlags, isSerial);

        return mergeLines(allLines, isSerial);
    }

    /**
     * Merges the lines from the provided list of lines according to the specified mode.
     *
     * @param allLines  List of lists of lines to be merged.
     * @param isSerial  Flag indicating whether to merge the lines serially or in parallel.
     * @return String representing the merged lines.
     */
    private String mergeLines(List<List<String>> allLines, boolean isSerial) {
        StringBuilder mergedLines = new StringBuilder();

        if (isSerial) {
            mergeSerially(mergedLines, allLines);
        } else {
            mergeInParallel(mergedLines, allLines);
        }
        return mergedLines.toString();
    }

    /**
     * Merges the lines serially from the provided list of lines.
     *
     * @param mergedLines StringBuilder to which the merged lines are appended.
     * @param allLines    List of lists of lines to be merged.
     */
    private void mergeSerially(StringBuilder mergedLines, List<List<String>> allLines) {
        int totalLines = allLines.size();
        for (int i = 0; i < totalLines; i++) {
            List<String> lines = allLines.get(i);
            appendLines(mergedLines, lines);

            if (!mergedLines.isEmpty()) {
                mergedLines.deleteCharAt(mergedLines.length() - 1);
            }

            if (i < totalLines - 1) {
                mergedLines.append(STRING_NEWLINE);
            }
        }
    }

    /**
     * Merges the lines in parallel from the provided list of lines.
     *
     * @param mergedLines StringBuilder to which the merged lines are appended.
     * @param allLines    List of lists of lines to be merged.
     */
    private void mergeInParallel(StringBuilder mergedLines, List<List<String>> allLines) {
        int maxLines = allLines.stream().mapToInt(List::size).max().orElse(0);

        for (int i = 0; i < maxLines; i++) {
            for (List<String> lines : allLines) {
                if (i < lines.size()) {
                    mergedLines.append(lines.get(i)).append(CHAR_TAB);
                } else {
                    mergedLines.append(" ").append(CHAR_TAB);
                }
            }
            mergedLines.deleteCharAt(mergedLines.length() - 1);

            if (i < maxLines - 1) {
                mergedLines.append(STRING_NEWLINE);
            }
        }
    }

    /**
     * Appends lines from the provided list of lines to the StringBuilder.
     *
     * @param mergedLines StringBuilder to which the lines are appended.
     * @param lines       List of lines to be appended.
     */
    private void appendLines(StringBuilder mergedLines, List<String> lines) {
        for (String line : lines) {
            mergedLines.append(line);
            mergedLines.append(CHAR_TAB);
        }
    }

    /**
     * Processes the input from the provided stdin lines and file names.
     *
     * @param stdinLines  List of lines from the stdin.
     * @param fileNames   Array of file names from which to read lines.
     * @param totalFlags  Total number of flags in the file names array.
     * @param isSerial    Flag indicating whether to process the input serially or not.
     * @return List of lists of lines representing the processed input.
     * @throws PasteException If an error occurs during the processing of input.
     */
    private List<List<String>> processInput(List<String> stdinLines, String[] fileNames,
                                            int totalFlags, Boolean isSerial) throws PasteException {
        boolean hasProcessedSerialStdin = false;
        int currFlagCount = 0;
        List<List<String>> allLines = new ArrayList<>();
        String currentDirectory = Environment.currentDirectory;
        Path pathToFile;
        File node;
        InputStream fileStream = null;

        for (String fileName : fileNames) {
            if (fileName == null) {
                continue;
            }

            if (fileName.equals(STRING_FLAG_PREFIX)) {
                // Handle stdin input
                List<String> tempStdinLines = new ArrayList<>();
                if (isSerial) {
                    if (!hasProcessedSerialStdin) {
                        allLines.add(stdinLines);
                        hasProcessedSerialStdin = true;
                    }
                } else {
                    for (int i = currFlagCount; i < stdinLines.size(); i += totalFlags) {
                        tempStdinLines.add(stdinLines.get(i));
                    }
                    currFlagCount++;
                    allLines.add(tempStdinLines);
                }
            } else {
                try {
                    // Handle file input
                    pathToFile = Paths.get(fileName);
                    if (!pathToFile.isAbsolute()) {
                        pathToFile = Paths.get(currentDirectory).resolve(fileName);
                    }
                    node = pathToFile.toFile();
                    if (!node.exists()){
                        throw new PasteException(pathToFile.getFileName() + ": No such file or directory");
                    }
                    if (node.isDirectory()) {
                        throw new PasteException(pathToFile.getFileName() + ": Is a directory");
                    }
                    if (!node.canRead()) {
                        throw new PasteException(pathToFile.getFileName() + ": " + ERR_NO_PERM_READ_FILE);
                    }
                    fileStream = IOUtils.openInputStream(fileName);
                    List<String> lines = IOUtils.getLinesFromInputStream(fileStream);
                    allLines.add(lines);
                } catch (IOException | ShellException e) {
                    throw new PasteException(e.getMessage());
                } finally {
                    try {
                        IOUtils.closeInputStream(fileStream);
                    } catch (ShellException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return allLines;
    }

    /**
     * Counts the occurrences of the target string in the provided array.
     *
     * @param arr    Array of strings to search for occurrences.
     * @param target Target string to count occurrences of.
     * @return Number of occurrences of the target string.
     */
    private int countOccurrences(String[] arr, String target) {
        return (int) Arrays.stream(arr)
                .filter(fileName -> fileName != null && fileName.equals(target))
                .count();
    }
}
