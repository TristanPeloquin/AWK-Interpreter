import java.util.Optional;

public class IfNode extends StatementNode {

    private Optional<Node> condition;
    private BlockNode ifBlock;
    private Optional<Node> nextIf;

    public IfNode(Optional<Node> condition, BlockNode ifBlock, Optional<Node> nextIf) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        this.nextIf = nextIf;
    }

    public Node getCondition() {
        if (condition.isPresent()) {
            return condition.get();
        }
        return null;
    }

    public BlockNode getBlock() {
        return ifBlock;
    }

    public Node getNext() {
        if (nextIf.isPresent()) {
            return nextIf.get();
        }
        return null;
    }

    public String toString() {
        if (nextIf.isPresent())
            return "if ( " + condition.get() + " )" + ifBlock + "\telse " + nextIf.get();
        return "if ( " + condition.get() + " )" + ifBlock;
    }

}
