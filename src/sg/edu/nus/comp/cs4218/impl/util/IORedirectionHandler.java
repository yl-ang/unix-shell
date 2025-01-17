package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.util.*;

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
                continue;
            }

            if (!argsIterator.hasNext()) {
                throw new ShellException(ERR_FILE_NOT_FOUND);
            }

            String file = argsIterator.next();
            if (isRedirOperator(file)) {
                throw new ShellException(ERR_SYNTAX);
            }

            // handle quoting + globing + command substitution in file arg
            List<String> fileSegment = argumentResolver.resolveOneArgument(file);

            // globbing case
            if (file.contains("*")) {
                handleGlobbingCase(fileSegment, file, arg);
            } else {
                // other case
                if (fileSegment.size() > 1) {
                    // ambiguous redirect if file resolves to more than one parsed arg
                    throw new ShellException(ERR_SYNTAX);
                }
                file = fileSegment.get(0);
                handleStreams(arg, file);
            }
        }
    }

    private void handleGlobbingCase(List<String> fileSegment, String inputFile, String arg) throws ShellException, FileNotFoundException {
        ArrayList<InputStream> streams = new ArrayList<>();
        String file = inputFile;
        for (String s : fileSegment) {
            file = s;
            InputStream curr = IOUtils.openInputStream(file); //NOPMD - suppressed CloseResource - Close in downstream
            streams.add(curr);
        }

        if (arg.equals(String.valueOf(CHAR_REDIR_INPUT))) {
            IOUtils.closeInputStream(inputStream);
            inputStream = new SequenceInputStream(Collections.enumeration(streams));
            if (inputStream.equals(origInputStream)) { // Already have a stream
                throw new ShellException(ERR_MULTIPLE_STREAMS);
            }
            inputStream = new SequenceInputStream(Collections.enumeration(streams));
        } else if (arg.equals(String.valueOf(CHAR_REDIR_OUTPUT))) {
            IOUtils.closeOutputStream(outputStream);
            outputStream = IOUtils.openOutputStream(file);
            if (outputStream.equals(origOutputStream)) { // Already have a stream
                throw new ShellException(ERR_MULTIPLE_STREAMS);
            }
            outputStream = IOUtils.openOutputStream(file);
        }
    }

    private void handleStreams(String arg, String file) throws ShellException, FileNotFoundException {
        // replace existing inputStream / outputStream
        if (arg.equals(String.valueOf(CHAR_REDIR_INPUT))) {
            IOUtils.closeInputStream(inputStream);
            inputStream = IOUtils.openInputStream(file);
            if (inputStream.equals(origInputStream)) { // Already have a stream
                throw new ShellException(ERR_MULTIPLE_STREAMS);
            }
            inputStream = IOUtils.openInputStream(file);
        } else if (arg.equals(String.valueOf(CHAR_REDIR_OUTPUT))) {
            IOUtils.closeOutputStream(outputStream);
            outputStream = IOUtils.openOutputStream(file);
            if (outputStream.equals(origOutputStream)) { // Already have a stream
                throw new ShellException(ERR_MULTIPLE_STREAMS);
            }
            outputStream = IOUtils.openOutputStream(file);
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
}
