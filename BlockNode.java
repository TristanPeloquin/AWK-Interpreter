import java.util.LinkedList;
import java.util.Optional;

//Represents a "block" of code, contained within two braces {}, which could be
//either a BEGIN block, END block, function block, or conditional block
public class BlockNode extends Node {

    private LinkedList<Node> statements;
    private Optional<Node> condition;

    public BlockNode(LinkedList<Node> statements, Optional<Node> condition) {
        this.statements = statements;
        this.condition = condition;
    }

    public LinkedList<Node> getStatements() {
        return statements;
    }

    public Node getCondition(){
        if(condition.isPresent()){
            return condition.get();
        }
        return null;
    }

    public String toString() {
        if (condition.isPresent())
            return "\n\t(" + condition + ") {\n\t" + statements + "\n\t}\n";
        return "{\n\t" + statements + "\n\t}\n";
    }
}