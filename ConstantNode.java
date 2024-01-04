//Represents a constant value within the AST, such as a
//number or a string literal
public class ConstantNode extends Node {

    private String value;

    public ConstantNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }

}
