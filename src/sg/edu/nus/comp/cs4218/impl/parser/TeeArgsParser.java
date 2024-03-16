package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class TeeArgsParser extends ArgsParser {
    public static final char FLAG_IS_APPEND = 'a';

    public TeeArgsParser() {
        super();

        legalFlags.add(FLAG_IS_APPEND);
    }

    public Boolean isByteCount() {
        return flags.contains(FLAG_IS_APPEND);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
