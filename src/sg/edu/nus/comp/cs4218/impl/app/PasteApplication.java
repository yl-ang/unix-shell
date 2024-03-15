package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FLAG_PREFIX;

public class PasteApplication implements PasteInterface  {
    public static final String ERR_READING_FILE = "Could not read file";
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";

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
            } else {
                for (String line : stdinLines) {
                    mergedLines.append(line).append(CHAR_TAB);
                }
                if (!stdinLines.isEmpty()) {
                    mergedLines.deleteCharAt(mergedLines.length() - 1);
                }
            }
            return mergedLines.toString();
        } catch (IOException e) {
            throw new PasteException(ERR_READING_FILE);
        }
    }

    @Override
    public String mergeFile(Boolean isSerial, String... fileNames) throws AbstractApplicationException {

        if (fileNames == null || fileNames.length == 0) {
            throw new PasteException(ERR_NO_FILE_ARGS);
        }

        List<List<String>> allLines = new ArrayList<>();

        for (String fileName : fileNames) {
            try (InputStream inputStream = IOUtils.openInputStream(fileName)) {
                List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
                allLines.add(lines);
            } catch (IOException | ShellException e) {
                throw new PasteException(e.getMessage());
            }
        }
        return mergeLines(allLines, isSerial);
    }

    // TODO(yl-ang): Fix the incorrect - and fileName parallelism bug (Still buggy)

    //    $ paste – A.txt - < B.txt > AB.txt
    //    Would consume one line at a time from each file and merge them together. Hence, the next (first) line from
    //    stdin (content of B.txt), next (first) line from A.txt, and next (second) line from
    //    stdin (content of B.txt) are read and merged into one line. Next, the next (third) line from
    //    stdin (content of B.txt), next (second) line from A.txt, and next (fourth) line from stdin (content
    //    of B.txt) are read and merged into one line. At the next step, if B.txt has only four lines, EOF
    //    is observed at stdin, and only the lines of A.txt are used in the merge.

    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileNames)
            throws AbstractApplicationException {

        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new PasteException(ERR_NO_FILE_ARGS);
        }

        List<List<String>> allLines = new ArrayList<>();
        boolean stdinProcessed = false; // Flag to track if stdin has been processed

        for (String fileName : fileNames) {
            if (fileName.equals(STRING_FLAG_PREFIX) && !stdinProcessed) {
                try {
                    List<String> stdinLines = IOUtils.getLinesFromInputStream(stdin);
                    allLines.add(stdinLines);
                    stdinProcessed = true;
                } catch (IOException e) {
                    throw new PasteException(ERR_READING_FILE);
                }
            } else if (!fileName.equals(STRING_FLAG_PREFIX)) {
                try (InputStream inputStream = IOUtils.openInputStream(fileName)) {
                    List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
                    allLines.add(lines);
                } catch (IOException | ShellException e) {
                    throw new PasteException(e.getMessage());
                }
            }
        }
        return mergeLines(allLines, isSerial);
    }

    private String mergeLines(List<List<String>> allLines, boolean isSerial) {
        StringBuilder mergedLines = new StringBuilder();

        if (isSerial) {
            mergeSerially(mergedLines, allLines);
        } else {
            mergeInParallel(mergedLines, allLines);
        }
        return mergedLines.toString();
    }

    private void mergeSerially(StringBuilder mergedLines, List<List<String>> allLines) {
        for (List<String> lines : allLines) {
            appendLines(mergedLines, lines);
            if (!lines.isEmpty()) {
                mergedLines.deleteCharAt(mergedLines.length() - 1);
            }
            mergedLines.append("\n");
        }
    }

    private void mergeInParallel(StringBuilder mergedLines, List<List<String>> allLines) {
        int maxLines = allLines.stream().mapToInt(List::size).max().orElse(0);

        for (int i = 0; i < maxLines; i++) {
            for (List<String> lines : allLines) {
                if (i < lines.size()) {
                    mergedLines.append(lines.get(i));
                    mergedLines.append(CHAR_TAB);
                }
            }
            mergedLines.deleteCharAt(mergedLines.length() - 1);
            mergedLines.append("\n");
        }
    }

    private void appendLines(StringBuilder mergedLines, List<String> lines) {
        for (String line : lines) {
            mergedLines.append(line);
            mergedLines.append(CHAR_TAB);
        }
    }

}
