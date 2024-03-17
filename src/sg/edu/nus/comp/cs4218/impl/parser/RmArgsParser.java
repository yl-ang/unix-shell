package sg.edu.nus.comp.cs4218.impl.parser;

public class RmArgsParser extends ArgsParser {
    public static final char FLAG_IS_RECURSIVE = 'r';
    public static final char FLAG_IS_REMOVE_EMPTY_DIR = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_REMOVE_EMPTY_DIR);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public Boolean isRemoveEmptyDirectory() {
        return flags.contains(FLAG_IS_REMOVE_EMPTY_DIR);
    }
}
