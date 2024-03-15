package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
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
        return null;
    }

    @Override
    public String mergeFile(Boolean isSerial, String... fileName) throws AbstractApplicationException {
        return null;
    }

    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileName) throws Exception {
        return null;
    }
}
