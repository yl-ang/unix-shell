package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.*;

public class CutArgsParser extends ArgsParser {
    public static final char FLAG_IS_BYTE_CUT = 'b';

    public static final char FLAG_IS_CHAR_CUT = 'c';
    private List<int[]> ranges;

    public CutArgsParser() {
        super();
        ranges = new ArrayList<>();

        legalFlags.add(FLAG_IS_BYTE_CUT);
        legalFlags.add(FLAG_IS_CHAR_CUT);
    }

    @Override
    public void parse(String... args) throws InvalidArgsException {
        super.parse(args);
        List<String> newNonFlagArgs = new ArrayList<>();

        // Regex to match a single number, a range, or a list of numbers/ranges
        String numberPattern = "\\d+(-\\d+)?"; // Matches a single number or a range
        String rangePattern = numberPattern + "(," + numberPattern + ")*"; // Matches lists of the above

        // Find all ranges args, remove from nonFlagArgs
        for (String arg : nonFlagArgs) {
            if (arg.matches(rangePattern)) {
                parseRanges(arg);
                newNonFlagArgs.add(arg);
            }
        }
        nonFlagArgs.removeAll(newNonFlagArgs);
        sortAndRemoveDuplicateRanges();
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

    private void sortAndRemoveDuplicateRanges() {
        SortedSet<Integer> hashSet = new TreeSet<>();
        List<int[]> sortedRanges = new ArrayList<>();

        for (int[] range : ranges) {
            for (int i = range[0]; i <= range[1]; i++) {
                hashSet.add(i);
            }
        }

        for (Integer num : hashSet) {
            sortedRanges.add(new int[]{num, num});
        }

        ranges = sortedRanges;
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
