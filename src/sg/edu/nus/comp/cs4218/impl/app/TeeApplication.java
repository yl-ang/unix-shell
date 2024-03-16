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
     * Runs the wc application with the specified arguments.
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
        // Format: wc [-clw] [FILES]
        if (stdout == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }
        TeeArgsParser wcArgsParser = new TeeArgsParser();
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new TeeException(e.getMessage());
        }
        String result;
        try {
            if (wcArgsParser.getFileNames().isEmpty()) {
                result = countFromStdin(wcArgsParser.isByteCount(), wcArgsParser.isLineCount(), wcArgsParser.isWordCount(), stdin);
            } else if (Arrays.asList(wcArgsParser.getFileNames().toArray(new String[0])).contains("-")) {
                result = countFromFileAndStdin(wcArgsParser.isByteCount(), wcArgsParser.isLineCount(), wcArgsParser.isWordCount(), stdin, wcArgsParser.getFileNames().toArray(new String[0]));
            } else {
                result = countFromFiles(wcArgsParser.isByteCount(), wcArgsParser.isLineCount(), wcArgsParser.isWordCount(), wcArgsParser.getFileNames().toArray(new String[0]));
            }
        } catch (Exception e) {
            // Will never happen
            throw new TeeException(ERR_GENERAL); //NOPMD
        }
        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new TeeException(ERR_WRITE_STREAM);//NOPMD
        }
    }


    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws Exception
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 InputStream stdin) throws AbstractApplicationException {
        if (stdin == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }
        long[] count = getCountReport(stdin); // lines words bytes;

        StringBuilder sb = new StringBuilder(); //NOPMD
        if (isLines) {
            sb.append(String.format(NUMBER_FORMAT, count[LINES_INDEX]));
        }
        if (isWords) {
            sb.append(String.format(NUMBER_FORMAT, count[WORDS_INDEX]));
        }
        if (isBytes) {
            sb.append(String.format(NUMBER_FORMAT, count[BYTES_INDEX]));
        }
        if (!isLines && !isWords && !isBytes) {
            sb.append(String.format(NUMBER_FORMAT, count[LINES_INDEX]));
            sb.append(String.format(NUMBER_FORMAT, count[WORDS_INDEX]));
            sb.append(String.format(NUMBER_FORMAT, count[BYTES_INDEX]));
        }

        return sb.toString();
    }
}
