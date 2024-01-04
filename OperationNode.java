import java.util.Optional;

//Represents an operation in AWK, with the left side of the expression, the operation,
//and optionally the right side (e.g ++5 has no right node while 5+2 has left and right)
public class OperationNode extends Node {

    private Node left;
    private Operations operation;
    private Optional<Node> right;

    // Represents all types of operations accounted for in this parser
    public enum Operations {
        EQ, NE, LT, LE, GT, GE, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR,
        PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG, IN,
        EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, CONCATENATION
    }

    // Constructor if there is no right side to the operation
    public OperationNode(Node left, Operations operation) {
        this.left = left;
        this.operation = operation;
        right = Optional.empty();
    }

    public OperationNode(Node left, Operations operation, Optional<Node> right) {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Operations getOperation() {
        return operation;
    }

    public Optional<Node> getRight() {
        return right;
    }

    public String toString() {
        if (right.isPresent())
            return left + " " + operation + " " + right.get();
        return "" + operation + " " + left;
    }

}
