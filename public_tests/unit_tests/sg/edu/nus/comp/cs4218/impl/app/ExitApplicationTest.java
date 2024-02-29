package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExitApplicationTest {
    private static final ExitApplication exitApplication = new ExitApplication();

    @Test
    void run_ExitApplication_ShouldTerminateExecution() {
        assertThrows(AbstractApplicationException.class, () -> {
            exitApplication.run(null, null, null);
        });
    }

    @Test
    void run_terminateExecution_ExitApplication_ShouldTerminateExecution() {
        assertThrows(AbstractApplicationException.class, () -> {
            exitApplication.terminateExecution();
        });
    }

    @Test
    void run_ExitApplicationWithStreams_ShouldTerminateExecution() {
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        assertThrows(AbstractApplicationException.class, () -> {
            exitApplication.run(null, inputStream, outputStream);
        });
    }
}
