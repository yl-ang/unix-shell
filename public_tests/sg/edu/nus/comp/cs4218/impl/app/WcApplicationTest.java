package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;

import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class WcApplicationTest {
    static FileWriter myWriter;

    @BeforeAll
    static void setUp() throws IOException {
        try {
            myWriter = new FileWriter("wctestfile.txt");
            myWriter.write("Test txt file for wc");
            myWriter.write("Second line");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * null stream
     * normal report
     * file empty
     * ioexception file too big?
     * */
    @Test
    void getCountReport() {
    }

    /*one file no arg show all
    one file arg show in seq
    * no file ,exception
    file dont exist, exception
    dir, exception
    no perm, exception
    * many files, no arg show all, show count
    * many files, one arg, show count
    many files, arg show in seq, show count
    *
    * */
    @Test
    void countFromFiles() {
    }

    /*one file no arg show all
    one file arg show in seq
    one file one arg show in seq
    * no file ,exception
    * */
    @Test
    void countFromStdin() {
    }

    @Test
    @Disabled
    void countFromFileAndStdin() {
    }

    @Test
    void run() {
    }


}