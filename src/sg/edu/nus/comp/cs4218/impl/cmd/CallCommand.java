package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * A Call Command is a sub-command consisting of at least one non-keyword or quoted.
 * <p>
 * Command format: (<non-keyword> or <quoted>) *
 */
public class CallCommand implements Command {
    private final List<String> argsList;
    private final ApplicationRunner appRunner;
    private final ArgumentResolver argumentResolver;

    public CallCommand(List<String> argsList, ApplicationRunner appRunner, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.appRunner = appRunner;
        this.argumentResolver = argumentResolver;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        // Handle IO redirection
        IORedirectionHandler redirHandler = new IORedirectionHandler(argsList, stdin, stdout, argumentResolver);
        redirHandler.extractRedirOptions();
        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList(); //NOPMD - suppressed CloseResource - Close in downstream
        InputStream inputStream = redirHandler.getInputStream(); //NOPMD - suppressed CloseResource - Close in downstream
        OutputStream outputStream = redirHandler.getOutputStream(); //NOPMD - suppressed CloseResource - Close in downstream

        // Handle quoting + globing + command substitution
        List<String> parsedArgsList = argumentResolver.parseArguments(noRedirArgsList);
        if (!parsedArgsList.isEmpty()) {
            String app = parsedArgsList.remove(0);
            appRunner.runApp(app, parsedArgsList.toArray(new String[0]), inputStream, outputStream);
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<String> getArgsList() {
        return argsList;
    }
}
