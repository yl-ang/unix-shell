package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.MvUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

public class MvApplication implements MvInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        MvUtils.checkArgsIsNull(args);
        MvUtils.checkArgsHasFiles(args);
        MvUtils.checkOutputIsNull(stdout);

        MvArgsParser parser = new MvArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MvException(e.getMessage());
        }
        Boolean isOverwrite = parser.isOverwrite();
        MvUtils.checkIsSrcAndTargetFileNull(parser.getSrcFiles(), parser.getTargetFile());

        String[] srcFiles = parser.getSrcFiles()
                .toArray(new String[0]);
        String targetFile = parser.getTargetFile();

        if (MvUtils.isDirectory(targetFile)) {
            mvFilesToFolder(isOverwrite, targetFile, srcFiles);
        } else if (srcFiles.length == 1) {
            mvSrcFileToDestFile(isOverwrite, srcFiles[0], targetFile);
        } else {
            throw new MvException(ERR_TOO_MANY_ARGS);
        }
    }

    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        Path absoluteSrcPath = Path.of(MvUtils.getNormalizedPath(srcFile));
        Path absoluteTargetPath = Path.of(MvUtils.getNormalizedPath(destFile));

        MvUtils.checkFileExists(srcFile);
        MvUtils.checkSrcIsWritable(srcFile);
        MvUtils.checkSrcAndTargetAreDifferent(absoluteSrcPath, absoluteTargetPath);
        try {
            MvUtils.checkFolderIsWithinDirectoryOfFile(absoluteSrcPath, absoluteTargetPath.toString());
        } catch(IOException e) {
            throw new MvException(ERR_IO_EXCEPTION);
        }
        try {
            if (isOverwrite) {
                MvUtils.checkDestIsWritable(absoluteTargetPath, destFile);
                if (Files.isDirectory(absoluteTargetPath)) {
                    absoluteTargetPath = absoluteTargetPath.resolve(absoluteSrcPath.getFileName());
                }
                Files.move(absoluteSrcPath, absoluteTargetPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                if (Files.isDirectory(absoluteTargetPath)) {
                    absoluteTargetPath = absoluteTargetPath.resolve(absoluteSrcPath.getFileName());
                }
                if (!Files.exists(absoluteTargetPath)) {
                    Files.move(absoluteSrcPath, absoluteTargetPath);
                }
            }
        } catch (IOException e) {
            throw new MvException(e.getMessage());
        }
        return destFile;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        for (String srcfile : fileName) {
            MvUtils.checkFileExists(srcfile);
            MvUtils.checkFileExists(destFolder);

            if (!MvUtils.isDirectory(destFolder)) {
                throw new MvException(ERR_IS_NOT_DIR + ": " + destFolder);
            }

            MvException mvException = null;
            try {
                mvSrcFileToDestFile(isOverwrite, srcfile, destFolder);
            } catch (MvException e) {
                    mvException = e;
            }

            if (mvException != null) {
                throw mvException;
            }
        }
        return destFolder;
    }
}
