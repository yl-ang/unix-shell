package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;

public class CutArgsParser extends ArgsParser {
    public static final char FLAG_IS_BYTE_CUT = 'b';

    public static final char FLAG_IS_CHAR_CUT = 'c';
    private List<int[]> ranges;

    public CutArgsParser() {
        super();

        legalFlags.add(FLAG_IS_BYTE_CUT);
        legalFlags.add(FLAG_IS_CHAR_CUT);
    }

    @Override
    public void parse(String... args) throws InvalidArgsException {
        super.parse(args);

        for (String arg : nonFlagArgs) {
            if (arg.contains("-") || arg.matches("\\d+")) {
                parseRanges(arg);
            }
        }
    }

    private void parseRanges(String arg) {
        String[] splitRanges = arg.split(",");
        for (String range : splitRanges) {
            if (range.contains("-")) {
                String[] bounds = range.split("-");
                int start = Integer.parseInt(bounds[0]);
                int end = Integer.parseInt(bounds[1]);
                ranges.add(new int[]{start, end});
            } else {
                int num = Integer.parseInt(range);
                ranges.add(new int[]{num, num});
            }
        }
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

    public List<int[]> getRanges() {
        return ranges;
    }
}
