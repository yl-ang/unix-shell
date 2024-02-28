package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.exception.LsException.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


import static org.junit.jupiter.api.Assertions.*;

class LsApplicationTest {
    private static LsApplication lsApplication;

    private static final String ROOT_DIRECTORY = Environment.currentDirectory;
    private static final String TEST_DIRECTORY = ROOT_DIRECTORY + File.separator + "test_folder";
    private static final String TEST_FOLDER_NAME = "tmpSortTestFolder" + File.separator;
    private static final String TEST_PATH = ROOT_DIRECTORY + File.separator + TEST_FOLDER_NAME;
    private static final String FOLDER_ONE = "testfolder1";
    private static final String FOLDER_TWO = "testfolder2";
    private static final String FILE_ONE = "file1.txt";
    private static final String FILE_TWO = "file2.xml";
    private static final String FILE_THREE = "file3.iml";

    @BeforeAll
    static void setUp() throws IOException {
        lsApplication = new LsApplication();
    }

    @org.junit.jupiter.api.Test
    void listFolderContent() {
    }

    @org.junit.jupiter.api.Test
    void run() {
    }
}