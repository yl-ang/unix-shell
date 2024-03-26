package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.MkdirInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static sg.edu.nus.comp.cs4218.exception.MkdirException.ERR_FOLDER_EXISTS;
import static sg.edu.nus.comp.cs4218.exception.MkdirException.ERR_MAKE_FOLDER_FAILED;
import static sg.edu.nus.comp.cs4218.exception.MkdirException.INVALID_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

@SuppressWarnings("PMD.PreserveStackTrace") // Stacktrace part of implementation
public class MkdirApplication implements MkdirInterface {

    private final static int SINGLE_FOLDER_COUNT = 1; //NOPMD - suppressed LongVariable - Clarity
    private final static String FOLDER_SEPARATOR = File.separator;

    /**
     * Runs the mkdir application with the specified arguments.
     * Assumption: The application must take in one arg. (mkdir without args is not supported)
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws MkdirException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MkdirException {
        if (args == null) {
            throw new MkdirException(ERR_NULL_ARGS);
        }

        if (args.length == 0) {
            throw new MkdirException("No arguments provided");
        }

        MkdirParser mkdirParser = new MkdirParser();
        try {
            mkdirParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MkdirException(e.getMessage());
        }

        Boolean createParent = mkdirParser.isCreateParent();
        String[] folderPaths = mkdirParser.getFolderPath().toArray(new String[0]);

        for (String folderPath : folderPaths) {
            if (!createParent && !isParentExists(folderPath)) {
                throw new MkdirException(INVALID_DIR, folderPath);
            }

            if (!createParent && isFolderExists(folderPath)) {
                throw new MkdirException(ERR_FOLDER_EXISTS, folderPath);
            }

            createFolder(folderPath);
        }
    }

    /**
     * Creates the required folders in the folderPaths.
     *
     * @param folderPaths The paths of the folders to be created.
     * @throws MkdirException If the creation of any folder fails.
     */
    @Override
    public void createFolder(String... folderPaths) throws MkdirException {
        String currentDirectory = Environment.currentDirectory;

        if (folderPaths == null) {
            throw new MkdirException("Folder paths cannot be null");
        }

        for (String folderPath : folderPaths) {
            if (folderPath == null || folderPath.isEmpty()) {
                throw new MkdirException("Folder path cannot be null");
            }

            String path = currentDirectory + FOLDER_SEPARATOR + folderPath;
            File folder = new File(path);

            if (isFolderExists(folderPath)) {
                continue;
            }

            if (!folder.mkdirs()) {
                throw new MkdirException(ERR_MAKE_FOLDER_FAILED);
            }
        }
    }

    /**
     * Checks if the folders exists in the folderPath.
     *
     * @param folderPath The path of the folders to be checked.
     */
    private boolean isFolderExists(String folderPath) {
        String currentDirectory = Environment.currentDirectory;
        String currentPath = currentDirectory + FOLDER_SEPARATOR + folderPath;
        Path path = FileSystems.getDefault().getPath(currentPath);
        return Files.exists(path);
    }

    /**
     * Checks if the parent folders exists in the folderPath.
     *
     * @param folderPath The path of the folders to be checked.
     */
    private boolean isParentExists(String folderPath) {
        String currentDirectory = Environment.currentDirectory;
        String[] foldersInPath = folderPath.split(FOLDER_SEPARATOR);

        if (foldersInPath.length == SINGLE_FOLDER_COUNT) {
            return true; // Single folder, always exists
        }

        // Concatenate the path elements to check each parent folder
        String currentPath = currentDirectory + FOLDER_SEPARATOR;
        for (int i = 0; i < foldersInPath.length - 1; i++) {
            currentPath += foldersInPath[i] + FOLDER_SEPARATOR;
            Path path = FileSystems.getDefault().getPath(currentPath);

            if (!Files.exists(path) || !Files.isDirectory(path)) {
                return false; // Parent folder does not exist or is not a directory
            }
        }
        return true;
    }
}
