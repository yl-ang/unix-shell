package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.DirectoryNotFoundException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class IORedirectionHandler {
    private final List<String> argsList;
    private final ArgumentResolver argumentResolver;
    private final InputStream origInputStream;
    private final OutputStream origOutputStream;
    private List<String> noRedirArgsList;
    private InputStream inputStream;
    private OutputStream outputStream;

    public IORedirectionHandler(List<String> argsList, InputStream origInputStream,
                                OutputStream origOutputStream, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.inputStream = origInputStream;
        this.origInputStream = origInputStream;
        this.outputStream = origOutputStream;
        this.origOutputStream = origOutputStream;
        this.argumentResolver = argumentResolver;
    }

    public void extractRedirOptions() throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        noRedirArgsList = new LinkedList<>();

        // extract redirection operators (with their corresponding files) from argsList
        ListIterator<String> argsIterator = argsList.listIterator();
        while (argsIterator.hasNext()) {
            String arg = argsIterator.next();

            // leave the other args untouched
            if (!isRedirOperator(arg)) {
                noRedirArgsList.add(arg);
            } else {
                // fast forward to last item
                while (argsIterator.hasNext()) {
                    argsIterator.next();
                }
                // move the iterator back one step
                argsIterator.previous();
                argsIterator.previous();
                if (!isRedirOperator(argsIterator.next())) {
                    throw new ShellException(ERR_SYNTAX);
                }
                String file = argsIterator.next();

                // handle quoting + globing + command substitution in file arg
                List<String> fileSegment = argumentResolver.resolveOneArgument(file);
                if (fileSegment.size() > 1) {
                    // ambiguous redirect if file resolves to more than one parsed arg
                    throw new ShellException(ERR_SYNTAX);
                }
                file = fileSegment.get(0);

                // replace existing inputStream / outputStream
                if (arg.equals(String.valueOf(CHAR_REDIR_INPUT))) {
                    IOUtils.closeInputStream(inputStream);
                    if (!inputStream.equals(origInputStream)) { // Already have a stream
                        throw new ShellException(ERR_MULTIPLE_STREAMS);
                    }
                    inputStream = IOUtils.openInputStream(file);

                } else if (arg.equals(String.valueOf(CHAR_REDIR_OUTPUT))) {
                    IOUtils.closeOutputStream(outputStream);
                    if (!outputStream.equals(origOutputStream)) { // Already have a stream
                        throw new ShellException(ERR_MULTIPLE_STREAMS);
                    }
                    String filePath = Environment.currentDirectory + CHAR_FILE_SEP + file;
                    createFileIfNotExists(filePath);
                    outputStream = IOUtils.openOutputStream(file);
                }
                break;
            }
        }
    }
    public List<String> getNoRedirArgsList() {
        return noRedirArgsList;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    private boolean isRedirOperator(String str) {
        return str.equals(String.valueOf(CHAR_REDIR_INPUT)) || str.equals(String.valueOf(CHAR_REDIR_OUTPUT));
    }

    private void createFileIfNotExists(String filePath) throws FileNotFoundException {
        Path path = Paths.get(filePath);
        try {
            // Check if the file exists
            if (!Files.exists(path)) {
                // If the file doesn't exist, create it
                Files.createFile(path);
            }
        } catch (IOException e) {
            // Handle potential IO exceptions
            String errorMessage = String.format("%s %s %s", ERR_FILE_NOT_FOUND, CHAR_COLON, e.getMessage());
            throw new FileNotFoundException(errorMessage);
        }
    }
}
