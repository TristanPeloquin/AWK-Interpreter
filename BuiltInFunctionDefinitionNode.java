import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode {

    private boolean variadic;

    public BuiltInFunctionDefinitionNode(String name, LinkedList<Node> parameterNames,
            BlockNode block, boolean variadic) {
        super(name, parameterNames, block);
        this.variadic = variadic;
    }

    public boolean isVariadic() {
        return variadic;
    }

    public Function<HashMap<String, InterpreterDataType>, String> execute;

    public String toString() {
        return super.toString() + " " + variadic;
    }
}
