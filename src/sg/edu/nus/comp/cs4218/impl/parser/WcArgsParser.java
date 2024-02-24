package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class WcArgsParser extends ArgsParser {
    public static final char BYTE_COUNT_FLAG = 'c';

    public static final char LINE_COUNT_FLAG = 'l';

    public static final char WORD_COUNT_FLAG = 'w';

    public WcArgsParser() {
        super();

        legalFlags.add(BYTE_COUNT_FLAG);
        legalFlags.add(LINE_COUNT_FLAG);
        legalFlags.add(WORD_COUNT_FLAG);
    }

    public Boolean isByteCount() {
        return flags.contains(BYTE_COUNT_FLAG);
    }

    public Boolean isLineCount() {
        return flags.contains(LINE_COUNT_FLAG);
    }

    public Boolean isWordCount() {
        return flags.contains(WORD_COUNT_FLAG);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
