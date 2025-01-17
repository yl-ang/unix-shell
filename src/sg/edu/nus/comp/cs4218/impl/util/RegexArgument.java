package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;

@SuppressWarnings("PMD.AvoidStringBufferField")
public final class RegexArgument {
    private StringBuilder plaintext;
    private StringBuilder regex;
    private boolean contentIsRegex;

    public RegexArgument() {
        this.plaintext = new StringBuilder();
        this.regex = new StringBuilder();
        this.contentIsRegex = false;
    }

    public RegexArgument(String str) {
        this();
        merge(str);
    }
    public RegexArgument(String str, String text, boolean isRegex) {
        this();
        this.plaintext.append(text);
        this.contentIsRegex = isRegex;
        this.regex.append(".*"); // We want to match filenames
        for (char c : str.toCharArray()) {
            if (c == CHAR_ASTERISK) {
                this.regex.append("[^").append(StringUtils.fileSeparator()).append("]*");
            } else {
                this.regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
    }

    public void append(char chr) {
        plaintext.append(chr);
        regex.append(Pattern.quote(String.valueOf(chr)));
    }

    public void appendAsterisk() {
        plaintext.append(CHAR_ASTERISK);
        regex.append("[^").append(StringUtils.fileSeparator()).append("]*");
        contentIsRegex = true;
    }

    public void merge(RegexArgument other) {
        if (other == null) {
            return;
        }
        plaintext.append(other.plaintext);
        regex.append(other.regex);
        contentIsRegex = contentIsRegex || other.contentIsRegex;
    }

    public void merge(String str) {
        if (str == null) {
            return;
        }
        plaintext.append(str);
        regex.append(Pattern.quote(str));
    }

    public List<String> globFiles() {
        List<String> globbedFiles = new LinkedList<>();

        if (contentIsRegex) {
            Pattern regexPattern = Pattern.compile(regex.toString());
            String dir = "";
            String tokens[] = plaintext.toString().replaceAll("\\\\", "/").split("/");
            for (int i = 0; i < tokens.length - 1; i++) {
                dir += tokens[i] + File.separator;
            }

            File currentDir = Paths.get(Environment.currentDirectory + File.separator + dir).toFile();

            for (String candidate : Objects.requireNonNull(currentDir.list())) {
                if (regexPattern.matcher(dir + candidate).matches()) {
                    globbedFiles.add(dir + candidate);
                }
            }

            Collections.sort(globbedFiles);
        }

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(plaintext.toString());
        }

        return globbedFiles;
    }


    /**
     * Traverses a given File node and returns a list of absolute path that match the given regexPattern.
     * <p>
     * Assumptions:
     * - ignores files and folders that we do not have access to (insufficient read permissions)
     * - regexPattern should not be null
     *
     * @param regexPattern    Pattern object
     * @param node            File object
     * @param isAbsolute      Boolean option to indicate that the regexPattern refers to an absolute path
     * @param onlyDirectories Boolean option to list only the directories
     */
    private List<String> traverseAndFilter(Pattern regexPattern, File node, boolean isAbsolute, boolean onlyDirectories) {
        List<String> matches = new ArrayList<>();
        if (regexPattern == null || node == null || !node.canRead() || !node.isDirectory()) {
            return matches;
        }
        String[] list = node.list();
        if (list == null) {
            return matches;
        }
        for (String current : list) {
            File nextNode = new File(node, current);
            String match = isAbsolute
                    ? nextNode.getPath()
                    : nextNode.getPath().substring(Environment.currentDirectory.length() + 1);
            // TODO: Find a better way to handle this.
            if (onlyDirectories && nextNode.isDirectory()) {
                match += File.separator;
            }
            if (!nextNode.isHidden() && regexPattern.matcher(match).matches()) {
                matches.add(nextNode.getAbsolutePath());
            }
            matches.addAll(traverseAndFilter(regexPattern, nextNode, isAbsolute, onlyDirectories));
        }
        return matches;
    }

    public boolean isRegex() {
        return contentIsRegex;
    }

    public boolean isEmpty() {
        return plaintext.isEmpty();
    }

    public String toString() {
        return plaintext.toString();
    }
}
