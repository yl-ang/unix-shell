package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class CatApplication implements CatInterface {
    public static final String ERR_READING_FILE = "Could not read file";
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";

    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CatException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {

        if (args == null) {
            throw new CatException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new CatException(ERR_NO_OSTREAM);
        }

        CatArgsParser parser = new CatArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage());
        }

        Boolean isLineNumber = parser.isLineNumber();
        String[] fileNames = parser.getFileNames().toArray(new String[0]);

        // fileNames wouldn't be null due to new String, need to check length
        if (stdin == null && fileNames.length == 0) {
            throw new CatException(ERR_NO_INPUT);
        }

        String output = null;

        // Determining which cat functionality to execute based on conditions
        if (fileNames.length > 0 && List.of(fileNames).contains(STRING_FLAG_PREFIX)) {
            output = catFileAndStdin(isLineNumber, stdin, fileNames);
        } else if (fileNames.length > 0) {
            output = catFiles(isLineNumber, fileNames);
        } else {
            output = catStdin(isLineNumber, stdin);
        }

        try {
            stdout.write(output.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new CatException(ERR_WRITE_STREAM);
        }
    }

    /**
    * Concatenates and prints the content of the specified files to the output stream.
    * Optionally, adds line numbers if specified.
    *
    * @param isLineNumber Boolean flag indicating whether to add line numbers.
    * @param fileNames    Array of file names to concatenate and print.
    * @return Concatenated content of the specified files.
    * @throws AbstractApplicationException If an exception occurs during file reading or processing.
    */
    @Override
    public String catFiles(Boolean isLineNumber, String... fileNames) throws AbstractApplicationException {
        if (isLineNumber == null) {
            throw new CatException(ERR_NULL_ARGS);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new CatException(ERR_NO_FILE_ARGS);
        }

        List<String> outputLines = new ArrayList<>();
        InputStream fileStream = null;
        String currentDirectory = Environment.currentDirectory;
        Path pathToFile;

        for (String fileName : fileNames) {
            File node;
            if (fileName == null) {
                outputLines.add("cat: " + ERR_NULL_ARGS + STRING_NEWLINE);
                continue;
            }
            try {
                pathToFile = Paths.get(fileName);
                if (!pathToFile.isAbsolute()) {
                    pathToFile = Paths.get(currentDirectory).resolve(fileName);
                }
                node = pathToFile.toFile();
                if (!node.exists()){
                    throw new CatException("cat: " + pathToFile.getFileName() + ": No such file or directory");
                }
                if (node.isDirectory()) {
                    throw new CatException("cat: " + pathToFile.getFileName() + ": No such file or directory");
                }
                if (!node.canRead()) {
                    throw new CatException("cat: " + ERR_NO_PERM_READ_FILE);
                }

                fileStream = IOUtils.openInputStream(fileName);
                List<String> fileLines = IOUtils.getLinesFromInputStream(fileStream);

                if (isLineNumber) {
                    fileLines = addLineNumbers(fileLines);
                }

                outputLines.addAll(fileLines);
            } catch (Exception e) {
                throw new CatException(ERR_READING_FILE + ": " + fileName);
            } finally {
                try {
                    IOUtils.closeInputStream(fileStream);
                } catch (ShellException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return String.join(StringUtils.STRING_NEWLINE, outputLines);
    }


    /**
    * Concatenates and prints the content of the standard input stream to the output stream.
    * Optionally, adds line numbers if specified.
    *
    * @param isLineNumber Boolean flag indicating whether to add line numbers.
    * @param stdin        InputStream representing standard input.
    * @return Concatenated content of the standard input stream.
    * @throws AbstractApplicationException If an exception occurs during input stream reading or processing.
    */
    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws AbstractApplicationException {

        if (isLineNumber == null) {
            throw new CatException(ERR_NULL_ARGS);
        }

        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        List<String> lines;

        try {
            lines = IOUtils.getLinesFromInputStream(stdin);
        } catch (Exception e) {
            throw new CatException(ERR_READING_STREAM);
        }

        if (isLineNumber) {
            lines = addLineNumbers(lines);
        }
        return String.join(STRING_NEWLINE, lines);
    }

    /**
    * Concatenates and prints the content of the specified files and standard input stream to the output stream.
    * Optionally, adds line numbers if specified.
    *
    * @param isLineNumber Boolean flag indicating whether to add line numbers.
    * @param stdin        InputStream representing standard input.
    * @param fileName     Array of file names to concatenate with standard input.
    * @return Concatenated content of the specified files and standard input stream.
    * @throws AbstractApplicationException If an exception occurs during file reading or processing.
    */
    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileName)
            throws AbstractApplicationException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        if (fileName == null || fileName.length == 0) {
            throw new CatException(ERR_NO_FILE_ARGS);
        }


        List<String> output = new ArrayList<String>();
        for (String name : fileName) {
            if (name.equals("-")) {
                output.add(catStdin(isLineNumber, stdin));
            } else {
                output.add(catFiles(isLineNumber, name));
            }
        }
        return String.join(STRING_NEWLINE, output);
    }

    /**
    * Adds line numbers to the list of lines.
    *
    * @param lines List of lines to which line numbers will be added.
    * @return List of lines with line numbers.
    */
    private List<String> addLineNumbers(List<String> lines) {
        int lineNumber = 1;
        List<String> numberedLines = new ArrayList<>();
        for (String line : lines) {
            numberedLines.add(lineNumber + " " + line);
            lineNumber++;
        }
        return numberedLines;
    }
}
