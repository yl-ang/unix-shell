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

        if (MvUtils.isDirectory(srcFile) && !MvUtils.isDirectory(destFile)) {
            throw new MvException(ERR_IS_NOT_DIR + ": " + destFile);
        }

        MvUtils.checkFileExists(srcFile);
        MvUtils.checkFileExists(destFile);
        MvUtils.checkSrcIsWritable(srcFile);
        MvUtils.checkSrcAndTargetAreDifferent(absoluteSrcPath, absoluteTargetPath);
        try {
            if (isOverwrite) {
                MvUtils.checkDestIsWritable(absoluteTargetPath, destFile);
                Files.move(absoluteSrcPath, absoluteTargetPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(absoluteSrcPath, absoluteTargetPath);
            }
        } catch (IOException e) {
            throw new MvException(e.getMessage());
        }
        return destFile;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        MvUtils.checkFileExists(destFolder);

        if (!MvUtils.isDirectory(destFolder)) {
            throw new MvException(ERR_IS_NOT_DIR + ": " + destFolder);
        }

        MvException mvException = null;
        for (String srcFile : fileName) {
            try {
                mvSrcFileToDestFile(isOverwrite, srcFile, destFolder);
            } catch (MvException e) {
                mvException = e;
            }
        }
        if (mvException != null) {
            throw mvException;
        }

        return destFolder;
    }
}
