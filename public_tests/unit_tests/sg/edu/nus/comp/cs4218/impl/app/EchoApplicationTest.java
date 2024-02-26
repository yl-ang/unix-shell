package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class EchoApplicationTest {

    private static class MockOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new IOException("Simulated IOException");
        }
    }

    private static EchoApplication echoApplication;
    private static MockOutputStream mockOutputStream;
    private static final String[] VALID_ARGS_INPUT = {"Hello", "*", "\n", "  ", "World", "Java", "Echo", "Test"};
    private static final String VALID_ARGS_INPUT_STR = "Hello * \n    World Java Echo Test";
    private static final String[] VALID_ARGS_INPUT_EMPTY = new String[0];

    @BeforeEach
    public void setup() {
        echoApplication = new EchoApplication();
        mockOutputStream = new MockOutputStream();
    }

    @Test
    public void constructResult_validArgs_ShouldConstructCorrectly() throws EchoException {
        assertEquals(VALID_ARGS_INPUT_STR, echoApplication.constructResult(VALID_ARGS_INPUT));
    }

    @Test
    public void constructResult_nullArgs_ShouldThrowError() throws EchoException {
        assertThrows(EchoException.class, () -> echoApplication.constructResult(null) );
    }

    @Test
    public void constructResult_emptyArgs_ShouldConstructLineSeparator() throws EchoException {
        assertEquals(STRING_NEWLINE, echoApplication.constructResult(VALID_ARGS_INPUT_EMPTY));
    }

    @Test
    public void run_nullStdOut_ShouldThrowEchoException() throws EchoException {
        Exception exception = assertThrows(EchoException.class,
                () -> echoApplication.run(VALID_ARGS_INPUT, System.in, null));
        assertEquals(new EchoException(ERR_NO_OSTREAM).getMessage(), exception.getMessage());
    }

    @Test
    public void run_invalidStdOut_ShouldThrowEchoException() throws EchoException {
        Exception exception = assertThrows(EchoException.class,
                () -> echoApplication.run(VALID_ARGS_INPUT, System.in, mockOutputStream));
        assertEquals(new EchoException(ERR_IO_EXCEPTION).getMessage(), exception.getMessage());
    }

}