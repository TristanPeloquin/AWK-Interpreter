public class TernaryNode extends Node {
    Node expr1, expr2, expr3;

    public TernaryNode(Node expr1, Node expr2, Node expr3) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.expr3 = expr3;
    }

    public Node getCondition() {
        return expr1;
    }

    public Node getTrue() {
        return expr2;
    }

    public Node getFalse() {
        return expr3;
    }

    public String toString() {
        return expr1 + " ? " + expr2 + " : " + expr3;
    }

}
