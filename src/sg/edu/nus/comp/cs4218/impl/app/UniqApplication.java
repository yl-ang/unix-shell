package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.io.InputStream;
import java.io.OutputStream;

public class UniqApplication {
    private OutputStream outputStream;

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException {
        UniqArgsParser parser = new UniqArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw (UniqException) new UniqException(ERR_INVALID_ARGS).initCause(e);
        }

        uniqRun(stdin, stdout, parser);
    }
}
