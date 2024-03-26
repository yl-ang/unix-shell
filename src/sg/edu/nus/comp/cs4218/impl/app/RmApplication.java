package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

@SuppressWarnings("PMD.PreserveStackTrace") // Stacktrace part of implementation
public class RmApplication implements RmInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {

        if (args == null || args.length == 0) {
            throw new RmException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new RmException(ERR_NO_OSTREAM);
        }

        RmArgsParser parser = new RmArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new RmException(e.getMessage());
        }

        String[] fileNames = parser.getFileNames().toArray(new String[0]);

        if (fileNames.length == 0) {
            throw new RmException(ERR_NO_FILE_ARGS);
        }
        remove(parser.isEmptyFolder(), parser.isRecursive(), fileNames);
    }

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileNames) throws AbstractApplicationException {
        if (isEmptyFolder == null || isRecursive == null || fileNames == null) {
            throw new RmException(ERR_NULL_ARGS);
        }

        File file;
        String currentDirectory = Environment.currentDirectory;
        Path pathToFile;

        for (String fileName : fileNames) {
            pathToFile = Paths.get(fileName);
            if (!pathToFile.isAbsolute()) {
                pathToFile = Paths.get(currentDirectory).resolve(fileName);
            }

            file = pathToFile.toFile();
            if (!file.exists()){
                throw new RmException(pathToFile.getFileName() + ": No such file or directory");
            }

            if (file.isDirectory()) {
                if (isRecursive) {
                    deleteDirectory(file);
                } else {
                    // If it's a directory and -d option is enabled, remove empty directory
                    if (isEmptyFolder && Objects.requireNonNull(file.list()).length == 0) {
                        if (!file.delete()) {
                            throw new RmException("Failed to delete empty directory: " + fileName);
                        }
                    } else {
                        // If it's a directory and not empty or -r option not enabled, throw exception
                        throw new RmException("Cannot remove directory without -r option: " + fileName);
                    }
                }
            } else {
                if (!file.delete()) {
                    throw new RmException(ERR_DELETING_FILE);
                }
            }
        }
    }

    private void deleteDirectory(File directory) throws RmException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new RmException(ERR_DELETING_FILE);
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new RmException(ERR_DELETING_DIR);
        }
    }
}
