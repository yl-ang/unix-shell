package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;

import static org.mockito.Mockito.*;

class ExitApplicationTest {
    ExitApplication exitApplication = new ExitApplication();

    @Test
    void testTerminateExecutionWithCustomExitAction() throws Exception {
        // Given
        Runnable mockExitAction = mock(Runnable.class);
        exitApplication.setExitAction(mockExitAction);

        // When
        exitApplication.terminateExecution();

        // Then
        verify(mockExitAction, times(1)).run();
    }
}
