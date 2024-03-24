package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class WcArgsParser extends ArgsParser {
    public static final char FLAG_BYTE_COUNT = 'c';

    public static final char FLAG_LINE_COUNT = 'l';

    public static final char FLAG_WORD_COUNT = 'w';

    public WcArgsParser() {
        super();

        legalFlags.add(FLAG_BYTE_COUNT);
        legalFlags.add(FLAG_LINE_COUNT);
        legalFlags.add(FLAG_WORD_COUNT);
    }

    public Boolean isByteCount() {
        return flags.contains(FLAG_BYTE_COUNT);
    }

    public Boolean isLineCount() {
        return flags.contains(FLAG_LINE_COUNT);
    }

    public Boolean isWordCount() {
        return flags.contains(FLAG_WORD_COUNT);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
