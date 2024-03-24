package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class RmArgsParser extends ArgsParser {
    public static final char FLAG_IS_RECURSIVE = 'r';
    public static final char FLAG_IS_EMPTY_FOLDER = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_EMPTY_FOLDER);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public Boolean isEmptyFolder() {
        return flags.contains(FLAG_IS_EMPTY_FOLDER);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
