package external_tests.unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;

@SuppressWarnings("PMD") // Provided by CS4218 Team
public class ExitApplicationPublicTest {
    private ExitApplication app;

    @BeforeEach
    public void renewApplication() {
        app = new ExitApplication();
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    public void terminateExecution_GivenAnything_ShouldExitWithStatusCode0() throws AbstractApplicationException {
        app.terminateExecution();
    }
}
