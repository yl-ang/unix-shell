package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.ArrayList;

public class GrepArgsParser extends ArgsParser {
    private final static char FLAG_IS_INVERT = 'v';
    private final static int INDEX_PATTERN = 0;
    private final static int INDEX_FILES = 1;
    private static final char CASE_INSEN_IDENT = 'i';
    private static final char COUNT_IDENT = 'c';
    private static final char PREFIX_FN = 'H';

    public GrepArgsParser() {
        super();
        legalFlags.add(CASE_INSEN_IDENT);
        legalFlags.add(COUNT_IDENT);
        legalFlags.add(PREFIX_FN);
    }

    public Boolean isCaseInsensitive() {
        return flags.contains(CASE_INSEN_IDENT);
    }

    public Boolean isCount() {
        return flags.contains(COUNT_IDENT);
    }

    public Boolean isPrintFileName() {
        return flags.contains(PREFIX_FN);
    }

    public Boolean isInvert() {
        return flags.contains(FLAG_IS_INVERT);
    }

    public String getPattern() {
        return !nonFlagArgs.isEmpty() ? nonFlagArgs.get(INDEX_PATTERN) : null;
    }

    public ArrayList<String> getFileNames() {
        return nonFlagArgs.size() <= 1 ? new ArrayList<>() : new ArrayList<>(nonFlagArgs.subList(INDEX_FILES, nonFlagArgs.size()));
    }
}
