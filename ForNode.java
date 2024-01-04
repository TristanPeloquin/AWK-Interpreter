import java.util.Optional;

public class ForNode extends StatementNode {

    private Optional<Node> expression1;
    private Optional<Node> expression2;
    private Optional<Node> expression3;
    private BlockNode block;

    public ForNode(Optional<Node> expression1, Optional<Node> expression2, Optional<Node> expression3,
            BlockNode block) {
        this.expression1 = expression1;
        this.expression2 = expression2;
        this.expression3 = expression3;
        this.block = block;
    }

    public Node getInit(){
        if(expression1.isPresent()){
            return expression1.get();
        }
        return null;
    }

    public Node getCondition(){
        if(expression2.isPresent()){
            return expression2.get();
        }
        return null;
    }

    public Node getInc(){
        if(expression3.isPresent()){
            return expression3.get();
        }
        return null;
    }

    public BlockNode getBlock(){
        return block;
    }

    public String toString() {
        String totalString = "for ( ";
        if (expression1.isPresent()) {
            totalString += expression1.get() + "; ";
        } else
            totalString += "; ";
        if (expression2.isPresent()) {
            totalString += expression2.get() + "; ";
        } else
            totalString += "; ";
        if (expression3.isPresent()) {
            totalString += expression3.get() + ") " + block;
        } else
            totalString += " ) " + block;
        return totalString;
    }

}
