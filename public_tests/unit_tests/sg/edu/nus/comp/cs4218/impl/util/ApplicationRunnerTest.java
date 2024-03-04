package unit_tests.sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.*;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class ApplicationRunnerTest {

    @InjectMocks
    private ApplicationRunner applicationRunner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testRunApp_UnsupportedApplication_ThrowsShellException() {
        // GIVEN
        String unsupportedApp = "unsupportedApp";
        String[] argsArray = {};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        // WHEN / THEN
        assertThrows(ShellException.class, () -> applicationRunner.runApp(unsupportedApp, argsArray, inputStream, outputStream));
    }

    @Test
    void testRunApp_CreateLsApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"Hello"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<LsApplication> lsApplicationMockedConstruction = Mockito.mockConstruction(LsApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_LS, argsArray, inputStream, outputStream);

            // Then
            verify(lsApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateWcApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"file.txt"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<WcApplication> wcApplicationMockedConstruction = Mockito.mockConstruction(WcApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_WC, argsArray, inputStream, outputStream);

            // Then
            verify(wcApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateEchoApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"Hello", "World"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<EchoApplication> echoApplicationMockedConstruction = Mockito.mockConstruction(EchoApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_ECHO, argsArray, inputStream, outputStream);

            // Then
            verify(echoApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateExitApplication_Success() throws Exception {
        // Given
        String[] argsArray = {};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<ExitApplication> exitApplicationMockedConstruction = Mockito.mockConstruction(ExitApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_EXIT, argsArray, inputStream, outputStream);

            // Then
            verify(exitApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateGrepApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"pattern", "file.txt"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<GrepApplication> grepApplicationMockedConstruction = Mockito.mockConstruction(GrepApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_GREP, argsArray, inputStream, outputStream);

            // Then
            verify(grepApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateCdApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"directory"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<CdApplication> cdApplicationMockedConstruction = Mockito.mockConstruction(CdApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_CD, argsArray, inputStream, outputStream);

            // Then
            verify(cdApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateCatApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"file.txt"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<CatApplication> catApplicationMockedConstruction = Mockito.mockConstruction(CatApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_CAT, argsArray, inputStream, outputStream);

            // Then
            verify(catApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateMkdirApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"dirName"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<MkdirApplication> mkdirApplicationMockedConstruction = Mockito.mockConstruction(MkdirApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_MKDIR, argsArray, inputStream, outputStream);

            // Then
            verify(mkdirApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }

    @Test
    void testRunApp_CreateSortApplication_Success() throws Exception {
        // Given
        String[] argsArray = {"file.txt"};
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        try (MockedConstruction<SortApplication> sortApplicationMockedConstruction = Mockito.mockConstruction(SortApplication.class)) {
            // When
            applicationRunner.runApp(ApplicationRunner.APP_SORT, argsArray, inputStream, outputStream);

            // Then
            verify(sortApplicationMockedConstruction.constructed().get(0)).run(eq(argsArray), eq(inputStream), eq(outputStream));
        }
    }
}
