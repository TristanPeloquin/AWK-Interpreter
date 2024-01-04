//Represents a pattern in AWK, denoted with backticks (`) in this parser.
//Simply holds the value contained inside.
public class PatternNode extends Node {

    private String pattern;

    public PatternNode(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String toString() {
        return "`" + pattern + "`";
    }

}
