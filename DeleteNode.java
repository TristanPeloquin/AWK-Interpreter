public class DeleteNode extends StatementNode {

    private Node expression;

    public DeleteNode(Node expression) {
        this.expression = expression;
    }

    public Node getArray() {
        return expression;
    }

    public String toString() {
        return "delete " + expression;
    }

}
