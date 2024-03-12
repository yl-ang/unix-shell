package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class CutArgsParser extends ArgsParser {
    public static final char FLAG_IS_BYTE_CUT = 'b';

    public static final char FLAG_IS_CHAR_CUT = 'c';

    public CutArgsParser() {
        super();

        legalFlags.add(FLAG_IS_BYTE_CUT);
        legalFlags.add(FLAG_IS_CHAR_CUT);
    }

    public Boolean isByteCut() {
        return flags.contains(FLAG_IS_BYTE_CUT);
    }

    public Boolean isCharCut() {
        return flags.contains(FLAG_IS_CHAR_CUT);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
