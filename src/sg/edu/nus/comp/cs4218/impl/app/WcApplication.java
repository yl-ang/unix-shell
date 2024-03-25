package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

@SuppressWarnings({"PMD.PreserveStackTrace"})
public class WcApplication implements WcInterface {

    private static final String NUMBER_FORMAT = "\t%d";
    private static final String WC_ERROR_START = "wc: ";
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new WcException(ERR_NO_OSTREAM);
        }
        if (args == null) {
            throw new WcException(ERR_NULL_ARGS);
        }
        for (String arg : args) {
            if (arg == null) {
                throw new WcException(ERR_NULL_ARGS);
            }
        }
        WcArgsParser wcArgsParser = new WcArgsParser();
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
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
            throw new WcException(ERR_GENERAL); //NOPMD
        }
        try {
            stdout.write(result.getBytes());
        } catch (IOException e) {
            throw new WcException(ERR_WRITE_STREAM);//NOPMD
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName Array of String of file names
     * @throws Exception
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords, //NOPMD
                                 String... fileName) throws AbstractApplicationException {
        if (fileName == null) {
            throw new WcException(ERR_GENERAL);
        }
        List<String> result = new ArrayList<>();
        long totalBytes = 0, totalLines = 0, totalWords = 0;
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                result.add(WC_ERROR_START + file + ": " + ERR_FILE_NOT_FOUND);
                continue;
            }
            if (node.isDirectory()) {
                result.add(WC_ERROR_START + file + ": " + ERR_IS_DIR);
                continue;
            }
            if (!node.canRead()) {
                result.add(WC_ERROR_START + file + ": " + ERR_NO_PERM);
                continue;
            }
            long[] count;
            try (InputStream input = IOUtils.openInputStream(file)) {
                count = getCountReport(input); // lines words bytes
            } catch (ShellException e) {
                throw new WcException(e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Update total count
            totalLines += count[LINES_INDEX];
            totalWords += count[WORDS_INDEX];
            totalBytes += count[BYTES_INDEX];

            // Format all output: " \t%d \t%d \t%d %s"
            // Output in the following order: lines words bytes filename
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

            sb.append(String.format(" %s", file));
            result.add(sb.toString());
        }

        // Print cumulative counts for all the files
        if (fileName.length > 1) {
            StringBuilder sb = new StringBuilder(); //NOPMD
            if (isLines) {
                sb.append(String.format(NUMBER_FORMAT, totalLines));
            }
            if (isWords) {
                sb.append(String.format(NUMBER_FORMAT, totalWords));
            }
            if (isBytes) {
                sb.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            if (!isLines && !isWords && !isBytes) {
                sb.append(String.format(NUMBER_FORMAT, totalLines));
                sb.append(String.format(NUMBER_FORMAT, totalWords));
                sb.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            sb.append(" total");
            result.add(sb.toString());
        }
        return String.join(STRING_NEWLINE, result) + STRING_NEWLINE;
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
            throw new WcException(ERR_NULL_STREAMS);
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

        return sb + STRING_NEWLINE;
    }

    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin, String... fileName) throws AbstractApplicationException { //NOPMD
        if (fileName == null) {
            throw new WcException(ERR_GENERAL);
        }
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        List<String> result = new ArrayList<>();
        long totalBytes = 0, totalLines = 0, totalWords = 0;
        long[] count = {};

        for (String file : fileName) {
            if ("-".equals(file)) {
                count = getCountReport(stdin);
            } else {
                File node = IOUtils.resolveFilePath(file).toFile();
                if (!node.exists()) {
                    result.add(WC_ERROR_START + file + ": " + ERR_FILE_NOT_FOUND);
                    continue;
                }
                if (node.isDirectory()) {
                    result.add(WC_ERROR_START + file + ": " + ERR_IS_DIR);
                    continue;
                }
                if (!node.canRead()) {
                    result.add(WC_ERROR_START + file + ": " + ERR_NO_PERM);
                    continue;
                }

                try (InputStream input = IOUtils.openInputStream(file)) {
                    count = getCountReport(input); // lines words bytes

                } catch (ShellException e) {
                    throw new WcException(e.getMessage());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            // Update total count
            totalLines += count[LINES_INDEX];
            totalWords += count[WORDS_INDEX];
            totalBytes += count[BYTES_INDEX];

            // Format all output: " \t%d \t%d \t%d %s"
            // Output in the following order: lines words bytes filename
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

            if ("-".equals(file)) {
                sb.append((" -"));
            } else {
                sb.append(String.format(" %s", file));
            }
            result.add(sb.toString());
        }

        // Print cumulative counts for all the files
        if (fileName.length > 1) {
            StringBuilder sb = new StringBuilder(); //NOPMD
            if (isLines) {
                sb.append(String.format(NUMBER_FORMAT, totalLines));
            }
            if (isWords) {
                sb.append(String.format(NUMBER_FORMAT, totalWords));
            }
            if (isBytes) {
                sb.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            if (!isLines && !isWords && !isBytes) {
                sb.append(String.format(NUMBER_FORMAT, totalLines));
                sb.append(String.format(NUMBER_FORMAT, totalWords));
                sb.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            sb.append(" total");
            result.add(sb.toString());
        }
        return String.join(STRING_NEWLINE, result) + STRING_NEWLINE;
    }

    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws IOException
     */
    public long[] getCountReport(InputStream input) throws AbstractApplicationException {
        if (input == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] result = new long[3]; // lines, words, bytes

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int inRead = 0;
        boolean inWord = false;
        try {
            while ((inRead = input.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < inRead; ++i) {
                    if (Character.isWhitespace(data[i])) {
                        // Use <newline> character here. (Ref: UNIX)
                        if (data[i] == '\n') {
                            ++result[LINES_INDEX];
                        }
                        if (inWord) {
                            ++result[WORDS_INDEX];
                        }

                        inWord = false;
                    } else {
                        inWord = true;
                    }
                }
                result[BYTES_INDEX] += inRead;
                buffer.write(data, 0, inRead);
            }
            buffer.flush();
            if (inWord) {
                ++result[WORDS_INDEX]; // To handle last word
            }
        } catch (IOException e) {
            throw new SortException(ERR_IO_EXCEPTION);
        }

        return result;
    }
}
