public class ForInNode extends StatementNode {

    private Node condition;
    private BlockNode block;

    public ForInNode(Node condition, BlockNode block) {
        this.condition = condition;
        this.block = block;
    }

    public Node getCondition(){
        return condition;
    }

    public BlockNode getBlock(){
        return block;
    }

    public String toString() {
        return "for (" + condition + ")" + block;
    }

}
