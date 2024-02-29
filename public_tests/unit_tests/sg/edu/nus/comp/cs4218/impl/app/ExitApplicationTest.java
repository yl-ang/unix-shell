package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;

import static org.mockito.Mockito.*;

class ExitApplicationTest {
    @Mock
    Runnable mockExitAction;

    @InjectMocks
    ExitApplication exitApplication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void terminateExecution_WithCustomExitAction_CallsExitAction() throws Exception {
        // When
        exitApplication.terminateExecution();

        // Then
        verify(mockExitAction, times(1)).run();
    }
}
