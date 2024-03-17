package sg.edu.nus.comp.cs4218.impl.parser;

public class RmArgsParser extends ArgsParser {
    public static final char FLAG_IS_REV_ORDER = 'r';
    public static final char FLAG_IS_REMOVE_DIR = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_REV_ORDER);
        legalFlags.add(FLAG_IS_REMOVE_DIR);
    }

    public Boolean isReverseOrder() {
        return flags.contains(FLAG_IS_REV_ORDER);
    }

    public Boolean isRemoveDirectory() {
        return flags.contains(FLAG_IS_REMOVE_DIR);
    }
}
