import java.util.Optional;

//Represents a variable, containing its name and possibly the index expression
//if its declared as an array
public class VariableReferenceNode extends Node {

    private String name;
    private Optional<Node> indexExp;

    public VariableReferenceNode(String name, Optional<Node> indexExp) {
        this.name = name;
        this.indexExp = indexExp;
    }

    public String getName() {
        return name;
    }

    public Node getIndex() {
        if (indexExp.isPresent())
            return indexExp.get();
        return null;
    }

    public String toString() {
        if (indexExp.isPresent()) {
            return name + "[" + indexExp.get() + "]";
        }
        return name;
    }

}
