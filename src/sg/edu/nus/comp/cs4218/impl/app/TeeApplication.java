package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

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
            throw new TeeException(e.getMessage());
        }
        String result;
        try {
            if (teeArgsParser.getFileNames().isEmpty()) {
                return;
            }
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
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws AbstractApplicationException {
        if (stdin == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }
        if (fileName == null) {
            throw new TeeException(ERR_NULL_ARGS);
        }
        if (isAppend == null) {
            throw new TeeException(ERR_NULL_ARGS);
        }

        // Loop through and check for invalid files, remove from fileName
        List<String> inValidFiles = new ArrayList<>();
        List<String> result = new ArrayList<>();

        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                continue;
            }
            if (node.isDirectory()) {
                result.add("tee: " + ERR_IS_DIR);
                inValidFiles.add(file);
                continue;
            }
            if (!node.canWrite()) {
                result.add("tee: " + ERR_NO_PERM);
                inValidFiles.add(file);
                continue;
            }

        }
        fileName = Arrays.stream(fileName).filter(s -> !inValidFiles.contains(s)).toArray(String[]::new);
        StringBuilder inputContent = new StringBuilder();

        // Read everything from stdin and store it in inputContent
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdin))) {
            String line;
            while ((line = reader.readLine()) != null) {
                inputContent.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new TeeException(e.getMessage());
        }

        String input = inputContent.toString(); // Convert StringBuilder to String

        // Loop through the files, create if not exist, then write or append input
        for (String file : fileName) {
            try {
                File node = new File(file);
                if (!node.exists()) {
                    boolean isCreated = node.createNewFile();
                    if (!isCreated) {
                        result.add("tee: Error creating file " + file);
                        continue;
                    }
                }

                try (FileOutputStream fileOutputStream = new FileOutputStream(node, isAppend);
                     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                     BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {
                    writer.write(input);
                }
            } catch (IOException e) {
                throw new TeeException(ERR_WRITE_STREAM);
            }
        }

        // Return input as output
        return input;

    }
}
