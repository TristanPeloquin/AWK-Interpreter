import java.util.Optional;

public class AssignmentNode extends StatementNode {

    private Node target;
    private Optional<Node> expression;
    private Node value;

    public AssignmentNode(Node target, Optional<Node> expression) {
        this.target = target;
        this.expression = expression;
    }

    public AssignmentNode(Node target, Node value) {
        this.target = target;
        expression = Optional.empty();
        this.value = value;
    }

    public Node getLeft() {
        return target;
    }

    public Node getRight() {
        if (expression.isPresent()) {
            return expression.get();
        }
        return value;
    }

    public String toString() {
        if (expression.isPresent()) {
            return target + " = " + expression.get().toString();
        }
        return target + " = " + value;
    }

}
