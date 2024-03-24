package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class CatArgsParser extends ArgsParser {
    public final static char FLAG_IS_LINE_NUMBER = 'n';

    public CatArgsParser() {
        super();
        legalFlags.add(FLAG_IS_LINE_NUMBER);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
    public Boolean isLineNumber() {
        return flags.contains(FLAG_IS_LINE_NUMBER);
    }
}
