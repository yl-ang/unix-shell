package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CdApplication implements CdInterface {

    @Override
    public void changeToDirectory(String path) throws CdException {



        String result = getNormalizedAbsolutePath(path);
        File file = new File(result);
        if (!file.canExecute()) { //NOPMD - suppressed ConfusingTernary - NotConfusingAtAll
            throw new CdException(String.format(ERR_NO_PERM, file));
        } else {
            Environment.currentDirectory = result;
        }
    }

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: The application must take in one arg. (cd without args is not supported)
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws CdException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws CdException {
        if (args == null) {
            throw new CdException(ERR_NULL_ARGS);
        }

        if (args.length == 0) {
            throw new CdException(ERR_NO_ARGS);
        }

        if (args.length > 1) {
            throw new CdException(ERR_TOO_MANY_ARGS);
        }

        changeToDirectory(args[0]);
    }

    private String getNormalizedAbsolutePath(String pathStr) throws CdException {
        if (StringUtils.isBlank(pathStr)) {
            throw new CdException(ERR_NO_ARGS);
        }

        Path path = new File(pathStr).toPath();
        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory, pathStr);
        }

        if (!Files.exists(path)) {
            throw new CdException(String.format(ERR_FILE_NOT_FOUND, pathStr));
        }

        if (!Files.isDirectory(path)) {
            throw new CdException(String.format(ERR_IS_NOT_DIR, pathStr));
        }

        return path.normalize().toString();
    }
}
