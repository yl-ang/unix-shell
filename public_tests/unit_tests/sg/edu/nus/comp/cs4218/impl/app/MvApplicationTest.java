package unit_tests.sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.app.MvApplication;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MvApplicationTest {
    private MvApplication mvApp;
    private static final String TMP_DIR = "temp-mv" + File.separator;
    private static final String FILE_A = "fileA.txt";
    private static final String FILE_A_PATH = TMP_DIR + FILE_A;
    private static final String FILE_B = "fileB.txt";
    private static final String FILE_B_PATH = TMP_DIR + FILE_B;
    private static final String FILE_C = "fileC.txt"; // New file
    private static final String FILE_C_PATH = TMP_DIR + FILE_C; // New file path
    private static final String FILE_D = "fileD.txt"; // New file
    private static final String FILE_D_PATH = TMP_DIR + FILE_D; // New file path
    private static final String DIR = "directory" + File.separator;
    private static final String DIR_PATH = TMP_DIR + DIR;
    private static final String MOVED_TXT = "movedFile.txt";
    private static final String MOVED_TXT_PATH = TMP_DIR + MOVED_TXT;
    private static final String TARGET_DIR = "targetDirectory" + File.separator;
    private static final String TARGET_DIR_PATH = TMP_DIR + TARGET_DIR;

    void createAndWriteFile(String filePath) throws Exception {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Content inside " + filePath);
        }
    }

    void createFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);
        Files.createDirectories(path);
    }

    void createMvResourcesAndFolders() throws Exception {
        createFolder(TMP_DIR);
        createFolder(TARGET_DIR_PATH);
        createFolder(DIR_PATH);
    }

    @BeforeEach
    void setup() throws Exception {
        mvApp = new MvApplication();
        createMvResourcesAndFolders();
        createAndWriteFile(FILE_A_PATH);
        createAndWriteFile(FILE_B_PATH);
        createAndWriteFile(FILE_C_PATH); // New file
        createAndWriteFile(FILE_D_PATH); // New file
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(Paths.get(TMP_DIR))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void mvSrcFileToDestFile_NonexistentSourceFile_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvSrcFileToDestFile(true, "nonexistent.txt", MOVED_TXT_PATH));
    }

    @Test
    void mvSrcFileToDestFile_InvalidTargetDirectory_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvSrcFileToDestFile(true, FILE_A_PATH, "nonexistentDir" + File.separator + MOVED_TXT));
    }

    @Test
    void mvSrcFileToDestFile_SourceAndTargetSame_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvSrcFileToDestFile(true, FILE_A_PATH, FILE_A_PATH));
    }

    @Test
    void mvFilesToFolder_NonexistentSourceFile_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvFilesToFolder(true, TARGET_DIR_PATH, "nonexistent.txt"));
    }

    @Test
    void mvFilesToFolder_InvalidTargetDirectory_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvFilesToFolder(true, "nonexistentDir" + File.separator, FILE_A_PATH, FILE_B_PATH));
    }

    @Test
    void mvFilesToFolder_SourceAndTargetSame_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvFilesToFolder(true, TMP_DIR, FILE_A_PATH));
    }

    @Test
    void mvFilesToFolder_SourceDirectoryMovedIntoItself_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvFilesToFolder(true, DIR_PATH, DIR_PATH));
    }

    @Test
    void mvSrcFileToDestFile_SourceDirectoryMovedIntoItself_ExceptionThrown() {
        Assertions.assertThrows(MvException.class, () -> mvApp.mvSrcFileToDestFile(true, DIR_PATH, DIR_PATH));
    }

    @Test
    void mvSrcFileToDestFile_FileRenamed_Success() throws Exception {
        File source = new File(FILE_B_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        mvApp.mvSrcFileToDestFile(true, FILE_B_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    @Test
    void mvFilesToFolder_FileMoved_Success() throws Exception {
        File source = new File(FILE_C_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        mvApp.mvFilesToFolder(true, TARGET_DIR_PATH, FILE_C_PATH);
        File target = new File(TARGET_DIR_PATH + FILE_C);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    @Test
    void mvSrcFileToDestFile_FolderRenamed_Success() throws Exception {
        File source = new File(DIR_PATH);

        mvApp.mvSrcFileToDestFile(true, DIR_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }

    @Test
    void mvFilesToFolder_MultipleFilesMoved_Success() throws Exception {
        File sourceA = new File(FILE_A_PATH);
        List<String> sourceAContent = Files.readAllLines(sourceA.toPath());
        File sourceB = new File(FILE_B_PATH);
        List<String> sourceBContent = Files.readAllLines(sourceB.toPath());
        File sourceC = new File(FILE_C_PATH); // New file
        List<String> sourceCContent = Files.readAllLines(sourceC.toPath()); // New file content
        File sourceD = new File(FILE_D_PATH); // New file
        List<String> sourceDContent = Files.readAllLines(sourceD.toPath()); // New file content

        mvApp.mvFilesToFolder(true, TARGET_DIR_PATH, FILE_A_PATH, FILE_B_PATH, FILE_C_PATH, FILE_D_PATH);
        File targetA = new File(TARGET_DIR_PATH + FILE_A);
        List<String> targetAContent = Files.readAllLines(targetA.toPath());
        File targetB = new File(TARGET_DIR_PATH + FILE_B);
        List<String> targetBContent = Files.readAllLines(targetB.toPath());
        File targetC = new File(TARGET_DIR_PATH + FILE_C); // New file
        List<String> targetCContent = Files.readAllLines(targetC.toPath()); // New file content
        File targetD = new File(TARGET_DIR_PATH + FILE_D); // New file
        List<String> targetDContent = Files.readAllLines(targetD.toPath()); // New file content

        assertTrue(targetA.exists());
        assertTrue(targetB.exists());
        assertTrue(targetC.exists());
        assertTrue(targetD.exists());
        assertFalse(sourceA.exists());
        assertFalse(sourceB.exists());
        assertFalse(sourceC.exists());
        assertFalse(sourceD.exists());
        assertEquals(sourceAContent, targetAContent);
        assertEquals(sourceBContent, targetBContent);
        assertEquals(sourceCContent, targetCContent);
        assertEquals(sourceDContent, targetDContent);
    }

    @Test
    void mvFilesToFolder_FolderMoved_Success() throws Exception {
        File source = new File(DIR_PATH);

        mvApp.mvFilesToFolder(true, TARGET_DIR_PATH, DIR_PATH);
        File target = new File(TARGET_DIR_PATH + DIR);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }
}
