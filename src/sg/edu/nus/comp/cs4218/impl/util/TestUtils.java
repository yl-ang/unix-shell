package sg.edu.nus.comp.cs4218.impl.util;

import java.io.File;

public final class TestUtils {
    private TestUtils() {
    }

    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

}
