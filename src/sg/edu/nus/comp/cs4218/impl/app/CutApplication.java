package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.parser.ArgsParser;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CutApplication implements CutInterface {

    /**
     * Runs the cut application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CutException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CutException {
        if (stdout == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }
        CutArgsParser cutArgsParser = new CutArgsParser();
        try {
            cutArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CutException(e.getMessage());
        }
        String result;
        try {
            if (cutArgsParser.getFileNames().isEmpty()) {
                result = cutFromStdin(cutArgsParser.isByteCut(), cutArgsParser.isCharCut(), ranges, stdin);
            } else if (Arrays.asList(cutArgsParser.getFileNames().toArray(new String[0])).contains("-")) {
                result = cutFromFilesAndStdin(cutArgsParser.isByteCut(), cutArgsParser.isCharCut(), ranges, stdin, cutArgsParser.getFileNames().toArray(new String[0]));
            } else {
                result = cutFromFiles(cutArgsParser.isByteCut(), cutArgsParser.isCharCut(), ranges, cutArgsParser.getFileNames().toArray(new String[0]));
            }
        } catch (Exception e) {
            // Will never happen
            throw new CutException(ERR_GENERAL); //NOPMD
        }
        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new CutException(ERR_WRITE_STREAM);//NOPMD
        }
    }

    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param fileName Array of String of file names
     * @return
     * @throws Exception
     */
    String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws CutException {
        if (fileName == null) {
            throw new CutException(ERR_GENERAL);
        }
        List<String> result = new ArrayList<>();
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                result.add("cut: " + ERR_FILE_NOT_FOUND);
                continue;
            }
            if (node.isDirectory()) {
                result.add("cut: " + ERR_IS_DIR);
                continue;
            }
            if (!node.canRead()) {
                result.add("cut: " + ERR_NO_PERM);
                continue;
            }

            InputStream input = null;
            try {
                input = IOUtils.openInputStream(file);
            } catch (ShellException e) {
                throw new CutException(e.getMessage());
            }
            String cutResult = cutFromInputStream(isCharPo, isBytePo, ranges, input);
            try {
                IOUtils.closeInputStream(input);
            } catch (ShellException e) {
                throw new CutException(e.getMessage());
            }

            result.add(cutResult);
        }
        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param fileName Array of String of file names (including "-" for reading from stdin)
     * @return
     * @throws Exception
     */
    String cutFromFilesAndStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin, String... fileName) throws CutException {
        if (fileName == null) {
            throw new CutException(ERR_GENERAL);
        }
        List<String> result = new ArrayList<>();
        String cutResult;

        for (String file : fileName) {
            if (file.equals("-")) {
                cutResult = cutFromInputStream(isCharPo, isBytePo, ranges, stdin);
            } else {
                File node = IOUtils.resolveFilePath(file).toFile();
                if (!node.exists()) {
                    result.add("cut: " + ERR_FILE_NOT_FOUND);
                    continue;
                }
                if (node.isDirectory()) {
                    result.add("cut: " + ERR_IS_DIR);
                    continue;
                }
                if (!node.canRead()) {
                    result.add("cut: " + ERR_NO_PERM);
                    continue;
                }

                InputStream input = null;
                try {
                    input = IOUtils.openInputStream(file);
                } catch (ShellException e) {
                    throw new CutException(e.getMessage());
                }
                cutResult = cutFromInputStream(isCharPo, isBytePo, ranges, input);
                try {
                    IOUtils.closeInputStream(input);
                } catch (ShellException e) {
                    throw new CutException(e.getMessage());
                }
            }

            result.add(cutResult);

        }
        return String.join(STRING_NEWLINE, result);

    }


    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param stdin    InputStream containing arguments from Stdin
     * @return
     * @throws Exception
     */
    String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin) throws CutException {
        if (stdin == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }
        String cutResult = cutFromInputStream(isCharPo, isBytePo, ranges, stdin);
        return cutResult;
    }


    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param stream   InputStream
     * @return
     * @throws Exception
     */
    String cutFromInputStream(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stream) throws CutException {

    }

}
