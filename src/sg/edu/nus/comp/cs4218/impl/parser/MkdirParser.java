package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class MkdirParser extends ArgsParser {
    public static final char FLAG_IS_CREATE_PARENT = 'p';

    public MkdirParser() {
        super();

        legalFlags.add(FLAG_IS_CREATE_PARENT);
    }

    public Boolean isCreateParent() {
        return flags.contains(FLAG_IS_CREATE_PARENT);
    }

    public List<String> getFolderPath() {
        return nonFlagArgs;
    }
}
