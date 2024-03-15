package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;

import java.io.InputStream;
import java.io.OutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class PasteApplication implements PasteInterface  {
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

        // TODO(yl-ang): The application logic, controller

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
