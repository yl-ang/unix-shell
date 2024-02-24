package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class WcArgsParser extends ArgsParser {
    public static final char BYTE_COUNT_FLAG = 'c';

    public static final char FLAG_IS_LINE_COUNT = 'l';

    public static final char FLAG_IS_WORD_COUNT = 'w';

    public WcArgsParser() {
        super();

        legalFlags.add(BYTE_COUNT_FLAG);
        legalFlags.add(FLAG_IS_LINE_COUNT);
        legalFlags.add(FLAG_IS_WORD_COUNT);
    }

    public Boolean isByteCount() {
        return flags.contains(BYTE_COUNT_FLAG);
    }

    public Boolean isLineCount() {
        return flags.contains(FLAG_IS_LINE_COUNT);
    }

    public Boolean isWordCount() {
        return flags.contains(FLAG_IS_WORD_COUNT);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
