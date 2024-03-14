package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.MvUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

public class MvApplication implements MvInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        if (args == null) {
            throw new MvException(ERR_NULL_ARGS);
        }
        if (args.length < 2) {
            throw new MvException(ERR_NO_ARGS);
        }

        if (stdout == null) {
            throw new MvException(ERR_NO_OSTREAM);
        }

        MvArgsParser parser = new MvArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MvException(e.getMessage());
        }
        Boolean isOverwrite = parser.isOverwrite();

        if (parser.getTargetFile() == null || parser.getSrcFiles() == null) {
            throw new MvException(ERR_NO_ARGS);
        }

        String[] srcFiles = parser.getSrcFiles()
                .toArray(new String[0]);
        String targetFile = parser.getTargetFile();

        if (srcFiles.length == 1) {
            mvSrcFileToDestFile(isOverwrite, srcFiles[0], targetFile);
        } else {
            mvFilesToFolder(isOverwrite, targetFile, srcFiles);
        }
    }

    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        return null;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        return null;
    }
}
