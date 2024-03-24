package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class UniqArgsParser extends ArgsParser {
    public static final char FLAG_IS_COUNT = 'c';
    public static final char FLAG_IS_DUPS = 'd';
    public static final char FLAG_IS_ALL_DUPS = 'D';

    public UniqArgsParser() {
        super();
        legalFlags.add(FLAG_IS_COUNT);
        legalFlags.add(FLAG_IS_DUPS);
        legalFlags.add(FLAG_IS_ALL_DUPS);
    }
    public String getInputFile() {
        if (nonFlagArgs.size() == 0) {
            return null;
        }
        return nonFlagArgs.get(0);
    }

    public String getOutputFile() {
        if (nonFlagArgs.size() <= 1) {
            return null;
        }
        return nonFlagArgs.get(1);
    }

    public boolean isCount() {
        return flags.contains(FLAG_IS_COUNT);
    }

    public boolean isOnlyDuplicates() {
        return flags.contains(FLAG_IS_DUPS);
    }

    public boolean isAllDuplicates() {
        return flags.contains(FLAG_IS_ALL_DUPS);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
