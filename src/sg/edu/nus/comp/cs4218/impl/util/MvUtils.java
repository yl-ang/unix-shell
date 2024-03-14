package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_BOTH_PATHS_SAME;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NOT_WRITEABLE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_OVERWRITE_FAILED;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SRC_SUBDIRECTORY_DEST;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

public class MvUtils {
    /**
     * returns the normalized path.
     *
     * @param path to be converted
     */
    public static String getNormalizedPath(String path) {
        Path resolvedPath = Paths.get(Environment.currentDirectory).resolve(path).normalize();
        return resolvedPath.toString();
    }

}
