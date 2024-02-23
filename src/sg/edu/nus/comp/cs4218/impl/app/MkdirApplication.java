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
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.exception.MkdirException.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

public class MkdirApplication implements MkdirInterface {

    private final static int SINGLE_FOLDER_COUNT = 1;
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

            createFolder(folderPath);
        }
    }

    @Override
    public void createFolder(String... folderNames) throws MkdirException {
        String currentDirectory = Environment.currentDirectory;

        for (String folderPath : folderNames) {
            File folder = new File(currentDirectory + FOLDER_SEPARATOR + folderPath);

            if (folder.exists()) {
                throw new MkdirException(ERR_FOLDER_EXISTS, folderPath);
            }

            if (!folder.mkdirs()) {
                throw new MkdirException(ERR_MAKE_FOLDER_FAILED);
            }
        }
    }


    private boolean isParentExists(String folderPath) {
        String currentDirectory = Environment.currentDirectory;
        String[] foldersInPath = folderPath.split(FOLDER_SEPARATOR);

        if (foldersInPath.length == SINGLE_FOLDER_COUNT) {
            return true; // Single folder, always exists
        }

        // Concatenate the path elements to check each parent folder
        String currentPath = currentDirectory + FOLDER_SEPARATOR;
        for (int i = 0; i < foldersInPath.length - 1; i++) {
            currentPath += foldersInPath[i];
            Path path = FileSystems.getDefault().getPath(currentPath);

            if (!Files.exists(path) || !Files.isDirectory(path)) {
                return false; // Parent folder does not exist or is not a directory
            }
        }

        return true; // All parent folders exist
    }

    private Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.currentDirectory).relativize(path);
    }
}
