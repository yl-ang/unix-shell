package sg.edu.nus.comp.cs4218.exception;

public class CutException extends AbstractApplicationException {

    private static final long serialVersionUID = 6616752571213808461L;

    public CutException(String message) {
        super("cut: " + message);
    }
}