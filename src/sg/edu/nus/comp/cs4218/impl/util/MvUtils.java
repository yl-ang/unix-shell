package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_BOTH_PATHS_SAME;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FOLDER_IN_FILE_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NOT_WRITEABLE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM_READ_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class MvUtils {
    /**
     * Check if arguments are null
     *
     * @param args arguments received
     * @throws MvException
     */
    public static void checkArgsIsNull(String[] args) throws MvException{
        if (args == null) {
            throw new MvException(ERR_NULL_ARGS);
        }
    }

    /**
     * Check if arguments has files
     *
     * @param args arguments received
     * @throws MvException
     */
    public static void checkArgsHasFiles(String[] args) throws MvException{
        if (args.length < 2) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }
    }

    /**
     * Check if output is null
     *
     * @param output output received
     * @throws MvException
     */
    public static void checkOutputIsNull(OutputStream output) throws MvException{
        if (output == null) {
            throw new MvException(ERR_NO_OSTREAM);
        }
    }

    /**
     * Check if src and target file is null
     *
     * @param src src files received
     * @param target target files received
     * @throws MvException
     */
    public static void checkIsSrcAndTargetFileNull(List<String> src, String target) throws MvException{
        if (src == null || target == null) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }
    }

    /**
     * returns the normalized path.
     *
     * @param path to be converted
     */
    public static String getNormalizedPath(String path) {
        Path resolvedPath = Paths.get(Environment.currentDirectory).resolve(path).normalize();
        return resolvedPath.toString();
    }

    /**
     * checks if path provided is a directory.
     *
     * @param path to check if directory
     */
    public static boolean isDirectory(String path) {
        Path resolvedPath = Paths.get(Environment.currentDirectory).resolve(path).normalize();
        return Files.isDirectory(resolvedPath);
    }

    /**
     * Check if the provided file exists.
     *
     * @param srcFile of path to source file
     * @throws MvException
     */
    public static void checkFileExists(String srcFile) throws MvException {
        File checkFile = new File(getNormalizedPath(srcFile));
        if (!checkFile.exists()) {
            throw new MvException(ERR_FILE_NOT_FOUND + ": " + srcFile);
        }
    }

    /**
     * Check and ensures that the absolute source path and absolute target path are different.
     *
     * @param absoluteSrcPath absolute path of source
     * @param absoluteTargetPath absolute path of target
     * @throws MvException
     */
    public static void checkSrcAndTargetAreDifferent(Path absoluteSrcPath, Path absoluteTargetPath) throws MvException {
        if (absoluteSrcPath.equals(absoluteTargetPath)) {
            throw new MvException(ERR_BOTH_PATHS_SAME + ": " + absoluteTargetPath + "=" + absoluteSrcPath);
        }
    }

    /**
     * Check and ensures that the srcFile can be written.
     *
     * @param srcFile of path to source file
     * @throws MvException
     */
    public static void checkSrcIsWritable(String srcFile) throws MvException {
        if (!isWriteable(srcFile)) {
            throw new MvException(ERR_NOT_WRITEABLE + "src:" + srcFile);
        }
    }

    /**
     * checks if path provided contains a file that can be written to.
     *
     * @param path to check if is writeable
     */
    private static boolean isWriteable(String path) {
        Path resolvedPath = Paths.get(Environment.currentDirectory).resolve(path).normalize();
        File file = resolvedPath.toFile();
        return file.canWrite();
    }

    /**
     * Check and ensures that the destination file can be written to.
     *
     * @param absoluteDestPath absolute path of destination
     * @param destFile of path to destination file
     * @throws MvException
     */
    public static void checkDestIsWritable(Path absoluteDestPath, String destFile) throws MvException {
        File destinationFile = new File(String.valueOf(absoluteDestPath));
        if (!isWriteable(destFile) && destinationFile.exists()) {
            throw new MvException(ERR_NOT_WRITEABLE + "dest:" + destFile);
        }
    }

    /**
     * Function that checks if the folder is in the directory of the file.
     * @param src path of the source
     * @param dest path in String of the destination
     * @throws IOException
     * @throws MvException
     */
    public static void checkFolderIsWithinDirectoryOfFile(Path src, String dest) throws IOException, MvException {
        File srcDir = src.toFile();
        Path absoluteDestPath= Path.of(getNormalizedPath(dest));
        File desDir = absoluteDestPath.toFile();
        if (srcDir.getParentFile().getCanonicalFile().equals(desDir.getCanonicalFile())) {
            throw new MvException(ERR_FOLDER_IN_FILE_DIR + ":" + dest);
        }
    }

}
