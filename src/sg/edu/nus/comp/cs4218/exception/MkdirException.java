package sg.edu.nus.comp.cs4218.exception;

public class MkdirException extends AbstractApplicationException {

    private static final long serialVersionUID = -7005801205007805286L;

    public static final String INVALID_DIR = ": No such file or directory";

    public static final String ERR_FOLDER_EXISTS = ": File exists";

    public static final String ERR_MAKE_FOLDER_FAILED = "Failed to create folder"; //NOPMD - suppressed LongVariable - Clarity

    public MkdirException(String message) {
        super("mkdir: " + message);
    }

    public MkdirException(String message, String folderPath) {
        super("mkdir: " + folderPath + message);
    }
}
