package sg.edu.nus.comp.cs4218.exception;

public class TeeException extends AbstractApplicationException {

    private static final long serialVersionUID = -7999923164724927309L;

    public TeeException(String message) {
        super("tee: " + message);
    }
}
