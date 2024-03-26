package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.ArrayList;

public class GrepArgsParser extends ArgsParser {
    private final static char FLAG_IS_INVERT = 'v';
    private final static int INDEX_PATTERN = 0;
    private final static int INDEX_FILES = 1;
    public static final char FLAG_IS_CASING = 'i';
    public static final char FLAG_IS_COUNT_LINES = 'c'; //NOPMD - suppressed LongVariable - Clarity
    public static final char FLAG_IS_INCLUDE_FILENAME = 'H'; //NOPMD - suppressed LongVariable - Clarity

    public GrepArgsParser() {
        super();
        legalFlags.add(FLAG_IS_CASING);
        legalFlags.add(FLAG_IS_COUNT_LINES);
        legalFlags.add(FLAG_IS_INCLUDE_FILENAME);
    }

    public Boolean isCaseInsensitive() {
        return flags.contains(FLAG_IS_CASING);
    }

    public Boolean isCount() {
        return flags.contains(FLAG_IS_COUNT_LINES);
    }

    public Boolean isPrintFileName() {
        return flags.contains(FLAG_IS_INCLUDE_FILENAME);
    }

    public Boolean isInvert() {
        return flags.contains(FLAG_IS_INVERT);
    }

    public String getPattern() {
        return !nonFlagArgs.isEmpty() ? nonFlagArgs.get(INDEX_PATTERN) : null; //NOPMD - suppressed ConfusingTernary - NotConfusingAtAll
    }

    public ArrayList<String> getFileNames() {
        return nonFlagArgs.size() <= 1 ? new ArrayList<>() : new ArrayList<>(nonFlagArgs.subList(INDEX_FILES, nonFlagArgs.size()));
    }
}
