import java.util.LinkedList;

//Represents a function with attributes name, a list of parameters, and a list of
//statements contained in the block of the function
public class FunctionDefinitionNode extends Node {

    private String name;
    private LinkedList<Node> parameterNames;
    private BlockNode block;

    public FunctionDefinitionNode(String name, LinkedList<Node> parameterNames,
            BlockNode block) {
        this.name = name;
        this.parameterNames = parameterNames;
        this.block = block;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Node> getParams() {
        return parameterNames;
    }

    public LinkedList<Node> getStatements() {
        return block.getStatements();
    }

    public boolean isVariadic() {
        return false;
    }

    public String toString() {
        return "\n\nfunction " + name + " (" + parameterNames + ") " + "{\n\t" + block + "\n}\n\n";
    }

}
