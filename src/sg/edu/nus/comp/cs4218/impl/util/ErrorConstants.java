package sg.edu.nus.comp.cs4218.impl.util;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.ClassNamingConventions", "PMD.LongVariable"})
public class ErrorConstants {

    // Streams related
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";
    public static final String ERR_NULL_STREAMS = "Null Pointer Exception";
    public static final String ERR_CLOSING_STREAMS = "Unable to close streams";
    public static final String ERR_MULTIPLE_STREAMS = "Multiple streams provided";
    public static final String ERR_STREAM_CLOSED = "Stream is closed";
    public static final String ERR_NO_OSTREAM = "OutputStream not provided";
    public static final String ERR_NO_ISTREAM = "InputStream not provided";
    public static final String ERR_NO_INPUT = "No InputStream and no filenames";
    public static final String ERR_NO_FILE_ARGS = "No files provided";
    public static final String ERR_READING_STREAM = "Could not read streams";

    // Arguments related
    public static final String ERR_MISSING_ARG = "Missing Argument";
    public static final String ERR_NO_ARGS = "Insufficient arguments";
    public static final String ERR_NULL_ARGS = "Null arguments";
    public static final String ERR_TOO_MANY_ARGS = "Too many arguments";
    public static final String ERR_INVALID_FLAG = "Invalid flag option supplied";
    public static final String ERR_BAD_REGEX = "Invalid pattern";
    public static final String ERR_BOTH_PATHS_SAME = "Both paths are same";
    public static final String ERR_NOT_WRITEABLE = "Not Writeable";

    // Files and folders related
    public static final String ERR_FILE_NOT_FOUND = "No such file or directory";
    public static final String ERR_READING_FILE = "Could not read file";
    public static final String ERR_IS_DIR = "This is a directory";
    public static final String ERR_IS_NOT_DIR = "Not a directory";
    public static final String ERR_NO_PERM = "Permission denied";
    public static final String ERR_NO_PERM_WRITE_FILE = "No write permission to file";
    public static final String ERR_NO_PERM_READ_FILE = "No read permission to file";
    public static final String ERR_DELETING_FILE = "Could not delete file";
    public static final String ERR_DELETING_DIR = "Could not delete directory";
    public static final String ERR_FOLDER_IN_FILE_DIR = "Folder is in file directory";

    // `date` related
    public static final String ERR_INVALID_FORMAT_PREFIX = "Invalid format. Date format must start with '+'";
    public static final String ERR_INVALID_FORMAT_FIELD = "Invalid format. Missing or unknown character after '%'";
    public static final String ERR_MISSING_FIELD = "Invalid format";

    // `find` related
    public static final String ERR_INVALID_FILE = "Invalid Filename";
    public static final String ERR_NAME_FLAG = "Paths must precede -name";

    // `sed` related
    public static final String ERR_NO_REP_RULE = "No replacement rule supplied";
    public static final String ERR_INVALID_REP_RULE = "Invalid replacement rule";
    public static final String ERR_INVALID_REP_X = "X needs to be a number greater than 0";
    public static final String ERR_INVALID_REGEX = "Invalid regular expression supplied";
    public static final String ERR_EMPTY_REGEX = "Regular expression cannot be empty";

    // `grep` related
    public static final String ERR_NO_REGEX = "No regular expression supplied";
    public static final String ERR_EMPTY_PATTERN = "Pattern should not be empty.";

    // `mkdir` related
    public static final String ERR_NO_FOLDERS = "No folder names are supplied";
    public static final String ERR_FILE_EXISTS = "File or directory already exists";
    public static final String ERR_TOP_LEVEL_MISSING = "Top level folders do not exist";

    // General constants
    public static final String ERR_INVALID_APP = "Invalid app";
    public static final String ERR_NOT_SUPPORTED = "Not supported yet";
    public static final String ERR_SYNTAX = "Invalid syntax";
    public static final String ERR_GENERAL = "Exception Caught";
    public static final String ERR_IO_EXCEPTION = "IOException";

    // `tee` related

    // Cut related
    public static final String ERR_BOTH_CHAR_AND_BYTE_FLAGS_PRESENT = "Choose only either -c or -b";
    public static final String ERR_ZERO_POSITION_ARG = "Values may not include zero";
    public static final String ERR_ISCHARPO_AND_ISBYTEPO_FALSE = "Specify either -c to cut by character position or -b to cut by byte position";
    public static final String ERR_RANGE_EMPTY = "Range to cut is not provided";

}
