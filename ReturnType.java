public class ReturnType {

    private Type type;
    private String value;

    public enum Type {
        NONE, BREAK, CONTINUE, RETURN
    }

    public ReturnType(Type type) {
        this.type = type;
    }

    public ReturnType(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String toString() {
        return type.toString() + " " + value;
    }

    public String getValue() {
        return value;
    }

}
