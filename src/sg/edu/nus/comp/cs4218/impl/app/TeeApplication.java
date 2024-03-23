package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

@SuppressWarnings({"PMD.PreserveStackTrace"})   // Suppress as exception is thrown in the catch block
public class TeeApplication implements TeeInterface {


    /**
     * Runs the tee application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws TeeException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws TeeException {
        if (stdin == null || stdout == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }
        TeeArgsParser teeArgsParser = new TeeArgsParser();
        try {
            teeArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new TeeException(e.getMessage()); //NOPMD
        }
        String result;
        try {
            result = teeFromStdin(teeArgsParser.isAppend(), stdin, teeArgsParser.getFileNames().toArray(new String[0]));
        } catch (Exception e) {
            // Will never happen
            throw new TeeException(ERR_GENERAL); //NOPMD
        }
        try {
            stdout.write(result.getBytes());
        } catch (IOException e) {
            throw new TeeException(ERR_WRITE_STREAM);//NOPMD
        }
    }


    /**
     * Reads from standard input and write to both the standard output and files
     *
     * @param isAppend Boolean option to append the standard input to the contents of the input files
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return
     * @throws TeeException
     */
    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws AbstractApplicationException { //NOPMD
        if (stdin == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }

        if (isAppend == null) {
            throw new TeeException(ERR_NULL_ARGS);
        }

        // Loop through and check for invalid files, remove from fileName
        List<String> inValidFiles = new ArrayList<>();
        List<String> result = new ArrayList<>();

        for (String file : fileName) {
            if (file == null) {
                result.add("tee: " + ERR_NULL_ARGS + STRING_NEWLINE);
                inValidFiles.add(file);
                continue;
            }
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                continue;
            }
            if (node.isDirectory()) {
                result.add("tee: " + ERR_IS_DIR + STRING_NEWLINE);
                inValidFiles.add(file);
                continue;
            }
            if (!node.canWrite()) {
                result.add("tee: " + ERR_NO_PERM_WRITE_FILE + STRING_NEWLINE);
                inValidFiles.add(file);
                continue;
            }

        }
        fileName = Arrays.stream(fileName).filter(s -> !inValidFiles.contains(s)).toArray(String[]::new);
        ByteArrayOutputStream inputBytes = new ByteArrayOutputStream();

        // Read everything from stdin and store it in inputBytes
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = stdin.read(buffer)) != -1) {
                inputBytes.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new TeeException(e.getMessage());
        }

        byte[] input = inputBytes.toByteArray(); // Convert ByteArrayOutputStream to byte array
        String currentDirectory = Environment.currentDirectory;
        Path pathToFile;

        // Loop through the files, create if not exist, then write or append input
        for (String file : fileName) {
            try {
                // Check if the provided filename is an absolute path
                if (Paths.get(file).isAbsolute()) {
                    // Use the absolute path as is
                    pathToFile = Paths.get(file);
                } else {
                    // If it's not absolute, resolve it against the current directory
                    pathToFile = Paths.get(currentDirectory).resolve(file);
                }

                File node = pathToFile.toFile();
                if (!node.exists()) {
                    boolean isCreated = node.createNewFile();
                    if (!isCreated) {
                        result.add("tee: Error creating file " + file);
                        continue;
                    }
                }

                try (FileOutputStream fileOutputStream = new FileOutputStream(node, isAppend)) {
                    fileOutputStream.write(input);
                }

            } catch (IOException e) {
                throw new TeeException(ERR_WRITE_STREAM);
            }
        }

        // Convert the byte array to a String for return
        String inputAsString = new String(input, StandardCharsets.UTF_8);
        return String.join("", result) + inputAsString;

    }
}
