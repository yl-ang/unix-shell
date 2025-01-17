package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.ArrayList;
import java.util.List;

public class MvArgsParser extends ArgsParser{
    private final static char FLAG_NOT_OVERWRITE = 'n'; //NOPMD - suppressed LongVariable - Clarity

    public MvArgsParser() {
        super();
        legalFlags.add(FLAG_NOT_OVERWRITE);
    }

    public Boolean isOverwrite() {
        return !flags.contains(FLAG_NOT_OVERWRITE);
    }

    public String getTargetFile() {
        if (nonFlagArgs.size() <= 1) {
            return null;
        } else {
            return nonFlagArgs.get(nonFlagArgs.size() - 1);
        }
    }

    public List<String> getSrcFiles() {
        int len = nonFlagArgs.size() - 1;
        List<String> srcFiles = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            srcFiles.add(nonFlagArgs.get(i));
        }
        return srcFiles;
    }
}
