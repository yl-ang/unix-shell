package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class LsArgsParser extends ArgsParser {
    public final static char FLAG_IS_RECURSIVE = 'R';
    public final static char FLAG_IS_SORT_BY_EXT = 'X'; //NOPMD - suppressed LongVariable - Clarity

    public LsArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_SORT_BY_EXT);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public Boolean isSortByExt() {
        return flags.contains(FLAG_IS_SORT_BY_EXT);
    }

    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}
