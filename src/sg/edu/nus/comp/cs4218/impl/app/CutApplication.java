package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
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


public class CutApplication implements CutInterface {
    private static final String CUT_ERROR_START = "cut: ";

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
        if (stdin == null) {
            throw new CutException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new CutException(ERR_NO_OSTREAM);
        }
        CutArgsParser cutArgsParser = new CutArgsParser();
        try {
            cutArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CutException(e.getMessage()); //NOPMD
        }
        String result;
        List<int[]> ranges = cutArgsParser.getRanges();

        // check if 0 is given as part of the range
        for (int[] range : ranges) {
            if (range[0] == 0 || range[1] == 0) {
                throw new CutException(ERR_ZERO_POSITION_ARG);
            }
        }

        if (cutArgsParser.getFileNames().isEmpty()) {
            result = cutFromStdin(cutArgsParser.isCharCut(), cutArgsParser.isByteCut(), ranges, stdin);
        } else if (Arrays.asList(cutArgsParser.getFileNames().toArray(new String[0])).contains("-")) {
            result = cutFromFilesAndStdin(cutArgsParser.isCharCut(), cutArgsParser.isByteCut(), ranges, stdin, cutArgsParser.getFileNames().toArray(new String[0]));
        } else {
            result = cutFromFiles(cutArgsParser.isCharCut(), cutArgsParser.isByteCut(), ranges, cutArgsParser.getFileNames().toArray(new String[0]));
        }

        try {
            stdout.write(result.getBytes());
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
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws CutException {
        if (fileName == null || fileName.length == 0) {
            throw new CutException(ERR_NULL_ARGS);
        }
        if (!isBytePo && !isCharPo) {
            throw new CutException(ERR_ISCHARPO_AND_ISBYTEPO_FALSE);
        }
        if (isBytePo && isCharPo) {
            throw new CutException(ERR_BOTH_CHAR_AND_BYTE_FLAGS_PRESENT);
        }
        if (ranges.isEmpty()) {
            throw new CutException(ERR_RANGE_EMPTY);
        }
        List<String> result = new ArrayList<>();
        for (String file : fileName) {
            if (file == null) { // Add an additional null check for each file in the array
                throw new CutException(ERR_NULL_ARGS);
            }
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                result.add(CUT_ERROR_START + file + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE);
                continue;
            }
            if (node.isDirectory()) {
                result.add(CUT_ERROR_START + file + ": " + ERR_IS_DIR + STRING_NEWLINE);
                continue;
            }
            if (!node.canRead()) {
                result.add(CUT_ERROR_START + file + ": " + ERR_NO_PERM + STRING_NEWLINE);
                continue;
            }

            String cutResult = "";
            String currentDirectory = Environment.currentDirectory;
            Path path = Paths.get(currentDirectory).resolve(file);

            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                cutResult = cutFromInputStream(isCharPo, isBytePo, ranges, reader);
            } catch (IOException e) {
                e.printStackTrace();
            }

            result.add(cutResult);
        }
        return String.join("", result);
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
    public String cutFromFilesAndStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin, String... fileName) throws CutException { //NOPMD
        if (fileName == null) {
            throw new CutException(ERR_GENERAL);
        }
        if (!isBytePo && !isCharPo) {
            throw new CutException(ERR_ISCHARPO_AND_ISBYTEPO_FALSE);
        }
        if (isBytePo && isCharPo) {
            throw new CutException(ERR_BOTH_CHAR_AND_BYTE_FLAGS_PRESENT);
        }
        if (ranges.isEmpty()) {
            throw new CutException(ERR_RANGE_EMPTY);
        }
        List<String> result = new ArrayList<>();
        String cutResult = "";

        for (String file : fileName) {
            if ("-".equals(file)) {
                cutResult = cutFromStdin(isCharPo, isBytePo, ranges, stdin);
            } else {
                File node = IOUtils.resolveFilePath(file).toFile();
                if (!node.exists()) {
                    result.add(CUT_ERROR_START + file + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE);
                    continue;
                }
                if (node.isDirectory()) {
                    result.add(CUT_ERROR_START + file + ": " + ERR_IS_DIR + STRING_NEWLINE);
                    continue;
                }
                if (!node.canRead()) {
                    result.add(CUT_ERROR_START + file + ": " + ERR_NO_PERM + STRING_NEWLINE);
                    continue;
                }

                String currentDirectory = Environment.currentDirectory;
                Path path = Paths.get(currentDirectory).resolve(file);

                try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    cutResult = cutFromInputStream(isCharPo, isBytePo, ranges, reader);
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }

            result.add(cutResult);

        }
        return String.join("", result);

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
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin) throws CutException {
        if (stdin == null) {
            throw new CutException(ERR_NO_ISTREAM);
        }
        if (!isBytePo && !isCharPo) {
            throw new CutException(ERR_ISCHARPO_AND_ISBYTEPO_FALSE);
        }
        if (isBytePo && isCharPo) {
            throw new CutException(ERR_BOTH_CHAR_AND_BYTE_FLAGS_PRESENT);
        }
        if (ranges.isEmpty()) {
            throw new CutException(ERR_RANGE_EMPTY);
        }
        String cutResult;
        BufferedReader reader = null;  //NOPMD
        reader = new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8));
        cutResult = cutFromInputStream(isCharPo, isBytePo, ranges, reader);
        return cutResult;
    }


    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param reader   BufferedReader
     * @return
     * @throws Exception
     */
    public String cutFromInputStream(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, BufferedReader reader) throws CutException {
        StringBuilder result = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String processedLine = "";

                for (int[] range : ranges) {
                    if (range.length != 2) {
                        continue;
                    }

                    int start = Math.max(range[0] - 1, 0);
                    if (start > line.length()) {
                        continue;
                    }
                    int end = Math.min(range[1], line.length());

                    if (isCharPo) {
                        processedLine = line.substring(start, end);
                    } else if (isBytePo) {
                        String lineAsBytes = new String(line.getBytes(StandardCharsets.UTF_8));
                        processedLine = lineAsBytes.substring(start, end);
                    }
                    result.append(processedLine);
                }
                result.append(STRING_NEWLINE);

            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        return result.toString();
    }

}
