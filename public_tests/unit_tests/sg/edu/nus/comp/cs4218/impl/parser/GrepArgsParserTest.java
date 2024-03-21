package unit_tests.sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;

import static org.junit.jupiter.api.Assertions.*;

class GrepArgsParserTest {
    private static final String VALID_PATTERN = "hello";
    private static final String VALID_I_FLAG = "-i";
    private static final String VALID_C_FLAG = "-c";
    private static final String VALID_H_FLAG = "-H";
    private static final String INVALID_FLAG = "-";

    @InjectMocks
    private GrepArgsParser grepArgsParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isCaseInsensitive_IsFalse_ShouldReturnFalse() {
        assertFalse(grepArgsParser.isCaseInsensitive());
    }

    @Test
    public void isCaseInsensitive_IsTrue_ShouldReturnTrue() throws Exception {
        String[] args = {VALID_I_FLAG, VALID_PATTERN};

        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isCaseInsensitive());
    }

    @Test
    public void isCountOfLines_IsTrue_ShouldReturnTrue() throws Exception {
        String[] args = {VALID_C_FLAG, VALID_PATTERN};

        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isCount());
    }

    @Test
    public void isPrintFileName_IsTrue_ShouldReturnTrue() throws Exception {
        String[] args = {VALID_H_FLAG, VALID_PATTERN};

        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isPrintFileName());
    }
}
