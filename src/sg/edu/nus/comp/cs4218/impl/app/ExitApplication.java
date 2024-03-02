package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.ExitInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;

import java.io.InputStream;
import java.io.OutputStream;

public class ExitApplication implements ExitInterface {
    private Runnable exitAction = () -> System.exit(0);

    /**
     * Runs the exit application.
     *
     * @param args   Array of arguments for the application, not used.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws ExitException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // Format: exit
        terminateExecution();
    }

    /**
     * Terminate shell.
     *
     * @throws ExitException
     */
    @Override
    public void terminateExecution() throws AbstractApplicationException {
        exitAction.run();
    }

    // Setter for exit action, used for testing
    public void setExitAction(Runnable exitAction) {
        this.exitAction = exitAction;
    }
}
