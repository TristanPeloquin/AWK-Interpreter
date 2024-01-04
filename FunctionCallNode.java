import java.util.LinkedList;
import java.util.Optional;

public class FunctionCallNode extends StatementNode {

    private String name;
    private LinkedList<Node> parameters;

    public FunctionCallNode(String name, LinkedList<Node> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public FunctionCallNode(String name, Optional<Node> parameter) {
        parameters = new LinkedList<Node>();
        if (parameter.isPresent())
            parameters.add(parameter.get());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Node> getParams() {
        return parameters;
    }

    public String toString() {
        return name + "(" + parameters + ")";
    }

}
