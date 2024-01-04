import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

//Represents an interpreter, which in short, assigns meaning and functionality
//to an awk program. Currently implemented: global variables (FILENAME, FS, NR,
//etc.), built-in functions (print, next, split, etc.), line handler
public class Interpreter {

    public HashMap<String, InterpreterDataType> variables;
    private HashMap<String, FunctionDefinitionNode> functions;
    private LineHandler handler;
    private ProgramNode tree;

    // Constructor; initializes the global variables, functions, and built-in
    // functions
    public Interpreter(ProgramNode tree, String path) throws IOException {
        this.tree = tree;
        variables = new HashMap<>();
        functions = new HashMap<>();
        if (path.isEmpty()) {
            handler = new LineHandler(new LinkedList<>());
        } else {
            handler = new LineHandler(Files.readAllLines(Paths.get(path)));
        }

        // Global variable default initialization
        variables.put("FILENAME", new InterpreterDataType(path));
        variables.put("OFMT", new InterpreterDataType("%.6g"));
        variables.put("OFS", new InterpreterDataType(" "));
        variables.put("ORS", new InterpreterDataType("\n"));
        variables.put("FS", new InterpreterDataType(" "));
        variables.put("NF", new InterpreterDataType("0"));
        variables.put("NR", new InterpreterDataType("0"));
        variables.put("FNR", new InterpreterDataType("0"));

        // Populating the hashmap with custom function
        for (int i = 0; i < tree.funcDefNodes.size(); i++) {
            functions.put(tree.funcDefNodes.get(i).getName(), tree.funcDefNodes.get(i));
        }

        // Populating the hashmap with built in functions, using lambda expressions for
        // streamlining and conciseness.
        // For summary, these functions are: print, printf, getline, next, gsub, match,
        // sub, index, length, split, substr, tolower, toupper
        LinkedList<Node> parameters1 = new LinkedList<>();
        parameters1.add(new VariableReferenceNode("string", null));
        BuiltInFunctionDefinitionNode print = new BuiltInFunctionDefinitionNode("print", parameters1, null, true);
        print.execute = this::printImplementation;
        functions.put("print", print);

        LinkedList<Node> parameters2 = new LinkedList<>();
        parameters2.add(new VariableReferenceNode("string", null));
        parameters2.add(new VariableReferenceNode("variadic", null));
        BuiltInFunctionDefinitionNode printf = new BuiltInFunctionDefinitionNode("printf", parameters2, null, true);
        printf.execute = this::printfImplementation;
        functions.put("printf", printf);

        BuiltInFunctionDefinitionNode getline = new BuiltInFunctionDefinitionNode("getline", new LinkedList<Node>(),
                null, false);
        getline.execute = this::getlineImplementation;
        functions.put("getline", getline);

        BuiltInFunctionDefinitionNode next = new BuiltInFunctionDefinitionNode("next", new LinkedList<Node>(), null,
                false);
        next.execute = this::nextImplementation;
        functions.put("next", next);

        LinkedList<Node> parameters3 = new LinkedList<>();
        parameters3.add(new VariableReferenceNode("regexp", null));
        parameters3.add(new VariableReferenceNode("replacement", null));
        parameters3.add(new VariableReferenceNode("target", null));
        BuiltInFunctionDefinitionNode gsub = new BuiltInFunctionDefinitionNode("gsub", parameters3, null, false);
        gsub.execute = this::gsubImplementation;
        functions.put("gsub", gsub);

        LinkedList<Node> parameters4 = new LinkedList<>();
        parameters4.add(new VariableReferenceNode("string", null));
        parameters4.add(new VariableReferenceNode("regexp", null));
        parameters4.add(new VariableReferenceNode("array", null));
        BuiltInFunctionDefinitionNode match = new BuiltInFunctionDefinitionNode("match", parameters4, null, false);
        match.execute = this::matchImplementation;
        functions.put("match", match);

        LinkedList<Node> parameters5 = new LinkedList<>();
        parameters5.add(new VariableReferenceNode("regexp", null));
        parameters5.add(new VariableReferenceNode("replacement", null));
        parameters5.add(new VariableReferenceNode("target", null));
        BuiltInFunctionDefinitionNode sub = new BuiltInFunctionDefinitionNode("sub", parameters5, null, false);
        sub.execute = this::subImplementation;
        functions.put("sub", sub);

        LinkedList<Node> parameters6 = new LinkedList<>();
        parameters6.add(new VariableReferenceNode("in", null));
        parameters6.add(new VariableReferenceNode("find", null));
        BuiltInFunctionDefinitionNode index = new BuiltInFunctionDefinitionNode("index", parameters6, null, false);
        index.execute = this::indexImplementation;
        functions.put("index", index);

        LinkedList<Node> parameters7 = new LinkedList<>();
        parameters7.add(new VariableReferenceNode("string", null));
        BuiltInFunctionDefinitionNode length = new BuiltInFunctionDefinitionNode("length", parameters7, null, false);
        length.execute = this::lengthImplementation;
        functions.put("length", length);

        LinkedList<Node> parameters8 = new LinkedList<>();
        parameters8.add(new VariableReferenceNode("string", null));
        parameters8.add(new VariableReferenceNode("array", null));
        parameters8.add(new VariableReferenceNode("fieldsep", null));
        parameters8.add(new VariableReferenceNode("seps", null));
        BuiltInFunctionDefinitionNode split = new BuiltInFunctionDefinitionNode("split", parameters8, null, false);
        split.execute = this::splitImplementation;
        functions.put("split", split);

        LinkedList<Node> parameters9 = new LinkedList<>();
        parameters9.add(new VariableReferenceNode("string", null));
        parameters9.add(new VariableReferenceNode("start", null));
        parameters9.add(new VariableReferenceNode("length", null));
        BuiltInFunctionDefinitionNode substr = new BuiltInFunctionDefinitionNode("substr", parameters9, null, false);
        substr.execute = this::substrImplementation;
        functions.put("substr", substr);

        LinkedList<Node> parameters10 = new LinkedList<>();
        parameters10.add(new VariableReferenceNode("string", null));
        BuiltInFunctionDefinitionNode tolower = new BuiltInFunctionDefinitionNode("tolower", parameters10, null, false);
        tolower.execute = this::tolowerImplementation;
        functions.put("tolower", tolower);

        LinkedList<Node> parameters11 = new LinkedList<>();
        parameters11.add(new VariableReferenceNode("string", null));
        BuiltInFunctionDefinitionNode toupper = new BuiltInFunctionDefinitionNode("toupper", parameters11, null, false);
        toupper.execute = this::toupperImplementation;
        functions.put("toupper", toupper);

    }

    // Helper class that splits lines into their fields (e.g $0, $1, etc.) for
    // easy usage
    public class LineHandler {

        private List<String> file;

        public LineHandler(List<String> file) {
            this.file = file;
            variables.replace("FNR", new InterpreterDataType("0"));
            variables.put("$0", new InterpreterDataType(file.get(0)));
        }

        // Splits lines into their respective fields and set the NR/FNR
        // variables
        public boolean splitAssign() {
            if (file.isEmpty()) {
                return false;
            }

            // Reads the next line and removes it from the list to increment
            // through the lines
            String line = file.get(0);
            file.remove(0);

            String[] words = line.split(variables.get("FS").get());
            variables.replace("NF", new InterpreterDataType(words.length + ""));

            // Sets the $0 field to the entire line and increments through the line to
            // assign fields
            variables.put("$0", new InterpreterDataType(line));
            for (int i = 1; i < words.length; i++) {
                variables.put("$" + i, new InterpreterDataType(words[i]));
            }

            int NR = Integer.parseInt(variables.get("NR").get());
            variables.put("NR", new InterpreterDataType(NR + 1 + ""));
            int FNR = Integer.parseInt(variables.get("FNR").get());
            variables.put("FNR", new InterpreterDataType(FNR + 1 + ""));

            return true;
        }

    }

    // The "main" method of the interpreter, this will run interpretBlock() on all
    // of the blocks within our program.
    public void interpretProgram() throws Exception {
        for (BlockNode block : tree.beginBlocks) {
            interpretBlock(block);
        }
        while (handler.splitAssign()) {
            for (BlockNode block : tree.blocks) {
                interpretBlock(block);
            }
        }
        for (BlockNode block : tree.endBlocks) {
            interpretBlock(block);
        }
    }

    // Checks a block condition for truth, otherwise runs processStatement() on all
    // of the statements within the block.
    public void interpretBlock(BlockNode block) throws Exception {
        if (block.getCondition() != null) {
            if (!getIDT(block.getCondition(), new HashMap<>()).get().equals("0")) {
                for (Node statement : block.getStatements()) {
                    processStatement(statement, new HashMap<>());
                }
            }
        } else {
            for (Node statement : block.getStatements()) {
                processStatement(statement, new HashMap<>());
            }
        }
    }

    // Interprets the following types of node: assignment, constants, function
    // calls, patterns, ternary, variable references, and operations(add, subtract,
    // AND, ++, etc.). Returns the result of this interpretation, for example if a
    // 1+1 operation node is passed in, 2 is returned within an InterpreterDataType
    public InterpreterDataType getIDT(Node node, HashMap<String, InterpreterDataType> localVars) throws Exception {

        // =============ASSIGNMENT=============
        // Sets the target variable in the variables hash map to the value of the IDT on
        // the right side of the assignment. If it is an array, finds the array in the
        // hash map and adds the value to it.
        if (node.getClass() == AssignmentNode.class) {

            AssignmentNode aNode = (AssignmentNode) node;
            if (aNode.getLeft().getClass() == VariableReferenceNode.class) {

                VariableReferenceNode left = (VariableReferenceNode) aNode.getLeft();
                InterpreterDataType right = getIDT(aNode.getRight(), localVars);
                InterpreterDataType temp;

                // If array...
                if (left.getIndex() != null) {

                    // Checks that the hash map contains the array and that it is an IADT, then adds
                    // the value to the given index and puts it in the hashmap
                    if ((temp = variables.get(left.getName())) != null) {

                        if (temp.getClass() == InterpreterArrayDataType.class) {

                            InterpreterArrayDataType array = (InterpreterArrayDataType) temp;
                            array.add(getIDT(left.getIndex(), localVars).get(), right);

                            if (localVars.containsKey(left.getName()))
                                localVars.put(left.getName(), array);
                            else
                                variables.put(left.getName(), array);
                            return array;
                        }

                        throw new Exception("Array of invalid type");
                    }

                    // If the hash map does not contain the array, then create a new one and put it
                    // in the hash map.
                    HashMap<String, InterpreterDataType> init = new HashMap<>();
                    init.put(getIDT(left.getIndex(), localVars).get(), right);
                    InterpreterArrayDataType array = new InterpreterArrayDataType(init);
                    variables.put(left.getName(), array);
                    return array;
                }

                // If variable...
                if (localVars != null && localVars.containsKey(left.getName()))
                    localVars.put(left.getName(), right);
                else
                    variables.put(left.getName(), right);
                return right;
            }

            // If the node is an operation and has a dollar operator, put the variable in
            // the hash map. This is for assigning values to fields (e.g. $1 = "hello
            // world").
            else if (aNode.getLeft().getClass() == OperationNode.class) {

                OperationNode left = (OperationNode) aNode.getLeft();
                InterpreterDataType right = getIDT(aNode.getRight(), localVars);

                if (left.getOperation() == OperationNode.Operations.DOLLAR) {

                    variables.put(getIDT(left, localVars).get(), right);
                    return right;
                }

                throw new Exception("Assigning to invalid type");
            }
            throw new Exception("Assigning to invalid type");
        }

        // =============CONSTANT=============
        else if (node.getClass() == ConstantNode.class) {
            return new InterpreterDataType(((ConstantNode) node).getValue());
        }

        // =============FUNCTION-CALL=============
        else if (node.getClass() == FunctionCallNode.class) {
            return new InterpreterDataType(runFunctionCall((FunctionCallNode) node, localVars));
        }

        // =============PATTERN=============
        else if (node.getClass() == PatternNode.class) {
            throw new Exception("Can not pass pattern to a function");
        }

        // =============TERNARY=============
        else if (node.getClass() == TernaryNode.class) {

            TernaryNode tNode = (TernaryNode) node;
            if (getIDT(tNode.getCondition(), localVars).get().equals("1")) {
                return getIDT(tNode.getTrue(), localVars);
            }

            else {
                return getIDT(tNode.getFalse(), localVars);
            }
        }

        // =============VARIABLE-REFERENCE=============
        // A variable can be of two different types: an array, or a regular variable.
        // Returns 0 if the variable is not initialized within the local variables or
        // global hash map.
        else if (node.getClass() == VariableReferenceNode.class) {

            VariableReferenceNode vNode = (VariableReferenceNode) node;

            // If array...
            if (vNode.getIndex() != null) {

                InterpreterDataType temp;
                if (((temp = localVars.get(vNode.getName())) != null)
                        || (temp = variables.get(vNode.getName())) != null) {

                    if (temp.getClass() == InterpreterArrayDataType.class) {

                        InterpreterArrayDataType array = (InterpreterArrayDataType) temp;
                        return array.getArray().get(getIDT(vNode.getIndex(), localVars).get());
                    }

                    throw new Exception("Array not of proper type");
                }
                return new InterpreterDataType("0");
            }

            // If variable...
            else {

                InterpreterDataType variable;
                if (((variable = localVars.get(vNode.getName())) != null)
                        || (variable = variables.get(vNode.getName())) != null) {
                    return variable;
                }
                return new InterpreterDataType("0");
            }
        }

        // =============OPERATION=============
        // Handles all mathematical, comparison, boolean, matching, unary, and "IN"
        // operators. This will return the result of the operations, for example 1>0
        // returns 1.
        else if (node.getClass() == OperationNode.class) {
            final DecimalFormat decimalFormat = new DecimalFormat("0.#####");
            OperationNode oNode = (OperationNode) node;
            InterpreterDataType left = getIDT(oNode.getLeft(), localVars);
            OperationNode.Operations op = oNode.getOperation();
            float leftValue;
            float rightValue;

            // Handles all non-unary operations
            if (oNode.getRight().isPresent()) {

                // ----------MATCH----------
                // Utilizes the String match() method to emulate AWK matching. Note: the right
                // side MUST be a pattern. NOTMATCH uses the same principles.
                if (op == OperationNode.Operations.MATCH) {

                    if (oNode.getRight().get().getClass() == PatternNode.class) {

                        PatternNode pattern = (PatternNode) oNode.getRight().get();
                        if (left.get().matches(pattern.getPattern())) {
                            return new InterpreterDataType("1");
                        }
                        return new InterpreterDataType("0");
                    }
                    throw new Exception("Invalid type on match");
                }

                // ----------NOTMATCH----------
                else if (op == OperationNode.Operations.NOTMATCH) {

                    if (oNode.getRight().get().getClass() == PatternNode.class) {

                        PatternNode pattern = (PatternNode) oNode.getRight().get();
                        if (left.get().matches(pattern.getPattern())) {
                            return new InterpreterDataType("0");
                        }

                        return new InterpreterDataType("1");
                    }
                    throw new Exception("Invalid type on match");
                }

                InterpreterDataType right = getIDT(oNode.getRight().get(), localVars);

                // ----------ADD----------
                // Tries to parse each left and right for floats, and sets them to 0 if it is
                // unable to parse, and then returns the result of the operation. The following
                // utilize the same principles: SUBTRACT, MULTIPLY, DIVIDE, MODULO, EXPONENT.
                if (op == OperationNode.Operations.ADD) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }

                    try {
                        rightValue = Float.parseFloat(right.get());
                    } catch (NumberFormatException e) {
                        rightValue = 0;
                    }

                    float value = leftValue + rightValue;
                    return new InterpreterDataType(decimalFormat.format(value));
                }

                // ----------SUBTRACT----------
                else if (op == OperationNode.Operations.SUBTRACT) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }

                    try {
                        rightValue = Float.parseFloat(right.get());
                    } catch (NumberFormatException e) {
                        rightValue = 0;
                    }

                    float value = leftValue - rightValue;
                    return new InterpreterDataType(decimalFormat.format(value));
                }

                // ----------MULTIPLY----------
                else if (op == OperationNode.Operations.MULTIPLY) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }

                    try {
                        rightValue = Float.parseFloat(right.get());
                    } catch (NumberFormatException e) {
                        rightValue = 0;
                    }

                    float value = leftValue * rightValue;
                    return new InterpreterDataType(decimalFormat.format(value));
                }

                // ----------DIVIDE----------
                else if (op == OperationNode.Operations.DIVIDE) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }

                    try {
                        rightValue = Float.parseFloat(right.get());
                    } catch (NumberFormatException e) {
                        rightValue = 0;
                    }

                    float value = leftValue / rightValue;
                    return new InterpreterDataType(decimalFormat.format(value));
                }

                // ----------MODULO----------
                else if (op == OperationNode.Operations.MODULO) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }

                    try {
                        rightValue = Float.parseFloat(right.get());
                    } catch (NumberFormatException e) {
                        rightValue = 0;
                    }

                    float value = leftValue % rightValue;
                    return new InterpreterDataType(decimalFormat.format(value));
                }

                // ----------EXPONENT----------
                else if (op == OperationNode.Operations.EXPONENT) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }

                    try {
                        rightValue = Float.parseFloat(right.get());
                    } catch (NumberFormatException e) {
                        rightValue = 0;
                    }

                    double value = Math.pow(leftValue, rightValue);
                    return new InterpreterDataType(decimalFormat.format(value));
                }

                // ----------EQUALS----------
                // Tries to parse both left and right and compare them as floats - if it is
                // unable to, compares them as strings. The following utilize the same
                // principles: NOTEQUALS, LESSTHAN, LESSEQUAL, GREATERTHAN, GREATEREQUAL.
                else if (op == OperationNode.Operations.EQ) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                        rightValue = Float.parseFloat(right.get());
                        if (leftValue == rightValue) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    } catch (NumberFormatException e) {
                        if (left.get().equals(right.get())) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    }
                }

                // ----------NOT-EQUALS----------
                else if (op == OperationNode.Operations.NE) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                        rightValue = Float.parseFloat(right.get());
                        if (leftValue == rightValue) {
                            return new InterpreterDataType("0");
                        } else {
                            return new InterpreterDataType("1");
                        }
                    } catch (NumberFormatException e) {
                        if (left.get().equals(right.get())) {
                            return new InterpreterDataType("0");
                        } else {
                            return new InterpreterDataType("1");
                        }
                    }
                }

                // ----------LESS-THAN----------
                else if (op == OperationNode.Operations.LT) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                        rightValue = Float.parseFloat(right.get());
                        if (leftValue < rightValue) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    } catch (NumberFormatException e) {
                        if (left.get().compareTo(right.get()) < 0) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    }
                }

                // ----------LESS-THAN-OR-EQUAL----------
                else if (op == OperationNode.Operations.LE) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                        rightValue = Float.parseFloat(right.get());
                        if (leftValue <= rightValue) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    } catch (NumberFormatException e) {
                        if (left.get().compareTo(right.get()) <= 0) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    }
                }

                // ----------GREATER-THAN----------
                else if (op == OperationNode.Operations.GT) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                        rightValue = Float.parseFloat(right.get());
                        if (leftValue > rightValue) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    } catch (NumberFormatException e) {
                        if (left.get().compareTo(right.get()) > 0) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    }
                }

                // ----------GREATER-THAN-OR-EQUAL----------
                else if (op == OperationNode.Operations.GE) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                        rightValue = Float.parseFloat(right.get());
                        if (leftValue >= rightValue) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    } catch (NumberFormatException e) {
                        if (left.get().compareTo(right.get()) >= 0) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    }
                }

                // ----------AND----------
                // Tries to parse the left and right to perform a boolean operation on, by
                // default a string is false and any number other than 0 is true.
                // OR uses the same principle.
                else if (op == OperationNode.Operations.AND) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                        rightValue = Float.parseFloat(right.get());
                        if (leftValue != 0 && rightValue != 0) {
                            return new InterpreterDataType("1");
                        } else {
                            return new InterpreterDataType("0");
                        }
                    } catch (NumberFormatException e) {
                        return new InterpreterDataType("0");
                    }
                }

                // ----------OR----------
                else if (op == OperationNode.Operations.OR) {
                    boolean l, r;
                    try {
                        leftValue = Float.parseFloat(left.get());
                        if (leftValue != 0) {
                            l = true;
                        } else
                            l = false;
                    } catch (NumberFormatException e) {
                        l = false;
                    }

                    try {
                        rightValue = Float.parseFloat(right.get());
                        if (rightValue != 0) {
                            r = true;
                        } else
                            r = false;
                    } catch (NumberFormatException e) {
                        r = false;
                    }

                    if (l || r) {
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
                }

                // ----------CONCATENTATION----------
                else if (op == OperationNode.Operations.CONCATENATION) {
                    return new InterpreterDataType(left.get() + right.get());
                }

                // ----------IN----------
                else if (op == OperationNode.Operations.IN) {
                    if (localVars.containsKey(right.get()) || variables.containsKey(right.get())) {
                        if (right.getClass() == InterpreterArrayDataType.class) {
                            InterpreterArrayDataType rightArray = (InterpreterArrayDataType) right;
                            if (rightArray.getArray().get(left.get()) != null) {
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        throw new Exception("Array is not of correct type");
                    }
                    throw new Exception("Array is not a initialized");
                }
            }

            // ----------NOT----------
            else if (op == OperationNode.Operations.NOT) {
                boolean l;
                try {
                    leftValue = Float.parseFloat(left.get());
                    if (leftValue != 0) {
                        l = true;
                    } else
                        l = false;
                } catch (NumberFormatException e) {
                    l = false;
                }

                if (l) {
                    return new InterpreterDataType("0");
                }
                return new InterpreterDataType("1");
            }

            // ----------DOLLAR----------
            else if (op == OperationNode.Operations.DOLLAR) {
                String field = "$" + left.get();
                InterpreterDataType retVal;
                if ((retVal = variables.get(field)) != null) {
                    return retVal;
                }
                return new InterpreterDataType("");
            }

            else if (oNode.getLeft().getClass() == VariableReferenceNode.class) {
                VariableReferenceNode leftVar = (VariableReferenceNode) oNode.getLeft();
                // ----------PRE-INCREMENT----------
                if (op == OperationNode.Operations.PREINC) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }
                    variables.put(leftVar.getName(), new InterpreterDataType(decimalFormat.format(leftValue + 1)));
                    return new InterpreterDataType(decimalFormat.format(leftValue + 1));
                }

                // ----------POST-INCREMENT----------
                else if (op == OperationNode.Operations.POSTINC) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }
                    variables.put(leftVar.getName(), new InterpreterDataType(decimalFormat.format(leftValue + 1)));
                    return new InterpreterDataType(decimalFormat.format(leftValue + 1));
                }

                // ----------POST-DECREMENT----------
                else if (op == OperationNode.Operations.POSTDEC) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }
                    variables.put(leftVar.getName(), new InterpreterDataType(decimalFormat.format(leftValue - 1)));
                    return new InterpreterDataType(decimalFormat.format(leftValue - 1));
                }

                // ----------PRE-DECREMENT----------
                else if (op == OperationNode.Operations.PREDEC) {
                    try {
                        leftValue = Float.parseFloat(left.get());
                    } catch (NumberFormatException e) {
                        leftValue = 0;
                    }
                    variables.put(leftVar.getName(), new InterpreterDataType(decimalFormat.format(leftValue - 1)));
                    return new InterpreterDataType(decimalFormat.format(leftValue - 1));
                }
            }

            // ----------POSITIVE----------
            else if (op == OperationNode.Operations.UNARYPOS) {
                try {
                    leftValue = Float.parseFloat(left.get());
                } catch (NumberFormatException e) {
                    leftValue = 0;
                }
                return new InterpreterDataType(leftValue + "");
            }

            // ----------NEGATIVE----------
            else if (op == OperationNode.Operations.UNARYNEG) {
                try {
                    leftValue = Float.parseFloat(left.get());
                } catch (NumberFormatException e) {
                    leftValue = 0;
                }
                return new InterpreterDataType(-leftValue + "");
            }
        }

        return null;
    }

    public ReturnType processStatement(Node statement, HashMap<String, InterpreterDataType> localVars)
            throws Exception {

        // =============BREAK=============
        if (statement.getClass() == BreakNode.class) {
            return new ReturnType(ReturnType.Type.BREAK);
        }

        // =============CONTINUE=============
        else if (statement.getClass() == ContinueNode.class) {
            return new ReturnType(ReturnType.Type.CONTINUE);
        }

        // =============DELETE=============
        // Works according to AWK functionality, deleting the index specified, or the
        // entire array if no index is given.
        else if (statement.getClass() == DeleteNode.class) {
            DeleteNode dNode = (DeleteNode) statement;
            if (dNode.getArray().getClass() != VariableReferenceNode.class) {
                throw new Exception("Syntax error: invalid type on delete");
            }
            VariableReferenceNode vNode = (VariableReferenceNode) dNode.getArray();
            InterpreterArrayDataType array;
            InterpreterDataType temp;

            // Checks if the array is present in the local or global variables map.
            temp = localVars.get(vNode.getName());
            if (temp.getClass() != InterpreterArrayDataType.class) {
                temp = variables.get(vNode.getName());
            }
            if (temp.getClass() != InterpreterArrayDataType.class) {
                throw new Exception("Array not found");
            }

            // Deletes the index at the array if an index is passed,
            // else delete the entire array.
            array = (InterpreterArrayDataType) temp;
            if (vNode.getIndex() != null) {
                array.delete(getIDT(vNode.getIndex(), localVars).get());
            } else {
                array.delete();
            }
            return new ReturnType(ReturnType.Type.NONE);
        }

        // =============DO=WHILE=============
        // Emulates the functionality of AWK by simply interpreting the list of
        // statements inside of a do-while loop; checks that the condition is
        // true using getIDT() to evaluate its truth.
        else if (statement.getClass() == DoWhileNode.class) {
            DoWhileNode dwNode = (DoWhileNode) statement;
            do {
                ReturnType type = interpStatementList(dwNode.getBlock().getStatements(), localVars);

                // Checks if the return type is break/return inside the statements
                if (type.getType() == ReturnType.Type.BREAK) {
                    break;
                } else if (type.getType() == ReturnType.Type.RETURN) {
                    return type;
                }

            } while (getIDT(dwNode.getCondition(), localVars).get().equals("1"));
            return new ReturnType(ReturnType.Type.NONE);
        }

        // =============FOR=============
        // Similar to do-while except we use this method (processStatement()) in order
        // to initialize the variable and increment the for loop.
        else if (statement.getClass() == ForNode.class) {
            ForNode fNode = (ForNode) statement;
            if (fNode.getInit() != null) {
                processStatement(fNode.getInit(), localVars);
            }
            while (getIDT(fNode.getCondition(), localVars).get().equals("1")) {
                ReturnType type = interpStatementList(fNode.getBlock().getStatements(), localVars);
                if (type.getType() == ReturnType.Type.BREAK) {
                    break;
                } else if (type.getType() == ReturnType.Type.RETURN) {
                    return type;
                }
                processStatement(fNode.getInc(), localVars);
            }
        }

        // =============FOR=IN=============
        // Resolves the operation inside the for-in statement in this order:
        // for-in -> operation -> variable reference -> array -> hashmap. It
        // loops over all entries in the hashmap and emulates the functionality
        // of a for-in loop by setting the left side variable to the key in the
        // hashmap.
        else if (statement.getClass() == ForInNode.class) {

            ForInNode fNode = (ForInNode) statement;
            if (fNode.getCondition().getClass() == OperationNode.class) {

                OperationNode oNode = (OperationNode) fNode.getCondition();
                if (oNode.getOperation() == OperationNode.Operations.IN) {

                    if (oNode.getLeft().getClass() == VariableReferenceNode.class) {

                        VariableReferenceNode left = (VariableReferenceNode) oNode.getLeft();
                        if (oNode.getRight().isPresent()) {

                            if (oNode.getRight().get().getClass() == VariableReferenceNode.class) {

                                VariableReferenceNode arrayRef = (VariableReferenceNode) oNode.getRight().get();
                                if (variables.get(arrayRef.getName()).getClass() == InterpreterArrayDataType.class) {

                                    // Ensures that the array is contained within the local/global variables,
                                    // and then loops through said array.
                                    InterpreterDataType temp = localVars.get(arrayRef.getName());
                                    if (temp == null) {
                                        temp = variables.get(arrayRef.getName());
                                    } else {
                                        throw new Exception("Array not found");
                                    }
                                    if (temp.getClass() != InterpreterArrayDataType.class) {
                                        throw new Exception("Not an array");
                                    }
                                    InterpreterArrayDataType array = (InterpreterArrayDataType) temp;
                                    for (Map.Entry<String, InterpreterDataType> entry : array.getArray().entrySet()) {

                                        localVars.put(left.getName(), entry.getValue());
                                        ReturnType type = interpStatementList(fNode.getBlock().getStatements(),
                                                localVars);
                                        if (type.getType() == ReturnType.Type.BREAK) {
                                            break;
                                        } else if (type.getType() == ReturnType.Type.RETURN) {
                                            return type;
                                        }

                                    }
                                    return new ReturnType(ReturnType.Type.NONE);
                                }
                                throw new Exception("Variable in for-in is not an array");
                            }
                            throw new Exception("Array in for-in is not a variable");
                        }
                        throw new Exception("No right side present on IN statement");
                    }
                    throw new Exception("Key is not a valid variable in for-in");
                }
                throw new Exception("Invalid operation type inside of for");
            }
            throw new Exception("Invalid type inside of for");
        }

        // =============IF=============
        // Walks through the linked list of if nodes, checking if the condition
        // is true, if it is, then run said block and return.
        else if (statement.getClass() == IfNode.class) {

            IfNode ifNode = (IfNode) statement;
            while (ifNode != null) {

                if (getIDT(ifNode.getCondition(), localVars).get().equals("1")) {

                    ReturnType type = interpStatementList(ifNode.getBlock().getStatements(), localVars);
                    if (!type.getType().equals(ReturnType.Type.NONE)) {
                        return type;
                    }
                    break;
                }
                // When it's an else block...
                else if (ifNode.getNext() != null && ifNode.getNext().getClass() == BlockNode.class) {
                    BlockNode block = (BlockNode) ifNode.getNext();
                    ReturnType type = interpStatementList(block.getStatements(), localVars);
                    if (!type.getType().equals(ReturnType.Type.NONE)) {
                        return type;
                    }
                    break;
                }
                ifNode = (IfNode) ifNode.getNext();
            }
            return new ReturnType(ReturnType.Type.NONE);
        }

        // =============RETURN=============
        else if (statement.getClass() == ReturnNode.class) {
            ReturnNode rNode = (ReturnNode) statement;
            if (rNode.getValue() != null) {
                return new ReturnType(ReturnType.Type.RETURN, getIDT(rNode.getValue(), localVars).get());
            }
        }

        // =============WHILE=============
        // Same functionality as the do-while loop, except with a while loop
        else if (statement.getClass() == WhileNode.class) {
            WhileNode wNode = (WhileNode) statement;

            while (getIDT(wNode.getCondition(), localVars).get().equals("1")) {
                ReturnType type = interpStatementList(wNode.getBlock().getStatements(), localVars);
                if (type.getType() == ReturnType.Type.BREAK) {
                    break;
                } else if (type.getType() == ReturnType.Type.RETURN) {
                    return type;
                }

            }
        }

        else if (getIDT(statement, localVars) == null)
            throw new Exception("Not a valid statement");
        return new ReturnType(ReturnType.Type.NONE);
    }

    // Takes a linked list of statements, representing a block, and processes
    // each one and checks to ensure we are not returning from the block.
    public ReturnType interpStatementList(LinkedList<Node> statements, HashMap<String, InterpreterDataType> localVars)
            throws Exception {
        for (Node node : statements) {
            ReturnType type = processStatement(node, localVars);
            if (type.getType() != ReturnType.Type.NONE) {
                return type;
            }
        }
        return new ReturnType(ReturnType.Type.NONE);
    }

    // Called whenever we encounter a function call in in getIDT(), this method has
    // two states: when a method is non-variadic or variadic. It then assigns the
    // parameters in the function definition to the evaluated values from the
    // function call. It then decides whether to run the function call as a built-in
    // or a custom function, in which case it calls interpStatementList on ever
    // element. When it is variadic, we skip this step and simply call the built-in
    // functionality.
    private String runFunctionCall(FunctionCallNode function, HashMap<String, InterpreterDataType> localVars)
            throws Exception {
        FunctionDefinitionNode funcDef = functions.get(function.getName());

        HashMap<String, InterpreterDataType> parameters = new HashMap<>();

        if (!funcDef.isVariadic()) {
            int i = 0;

            // Loops through all parameters and evaluates the values to assign to the
            // variables in the function definition.
            for (Node parameter : funcDef.getParams()) {
                if (parameter.getClass() == VariableReferenceNode.class) {
                    VariableReferenceNode vNode = (VariableReferenceNode) parameter;

                    if (function.getParams().size() <= i) {
                        break;
                    }

                    if (vNode.getName().equals("array")) {
                        parameters.put(vNode.getName(),
                                new InterpreterDataType(function.getParams().get(i).toString()));
                    } else
                        parameters.put(vNode.getName(), getIDT(function.getParams().get(i), localVars));
                } else {
                    throw new Exception("Parameter in function definition not a variable");
                }
                i++;
            }

            if (i != function.getParams().size()) {
                throw new Exception("Invalid number of parameters passed to function: " + function.getName());
            }

            if (funcDef.getClass() == BuiltInFunctionDefinitionNode.class) {
                return ((BuiltInFunctionDefinitionNode) funcDef).execute.apply(parameters);
            } else
                return interpStatementList(funcDef.getStatements(), parameters).getValue();
        }

        else {
            int index = 0;

            // Same as above, except it stops just before the variadic variable and runs the
            // loop below on said variable.
            for (int i = 0; i < funcDef.getParams().size() - 1; i++) {
                Node parameter = funcDef.getParams().get(i);
                if (parameter.getClass() == VariableReferenceNode.class) {
                    parameters.put(((VariableReferenceNode) parameter).getName(),
                            getIDT(function.getParams().get(i), localVars));
                } else {
                    throw new Exception("Parameter in function definition not a variable");
                }
                index = i;
            }

            // Assigns the variadic variable values.
            InterpreterDataType idt;
            for (int i = index; i < function.getParams().size(); i++) {
                if ((idt = getIDT(function.getParams().get(i), localVars)) == null) {
                    idt = new InterpreterDataType("");
                }
                parameters.put(i + "", idt);
            }

            // Variadic functions are only built-in, therefore we can assume this will work.
            return ((BuiltInFunctionDefinitionNode) funcDef).execute.apply(parameters);
        }
    }

    // Prints out all the parameters passed - this is variadic, so their can be any
    // number
    public String printImplementation(HashMap<String, InterpreterDataType> params) {
        for (int i = 0; i < params.size(); i++) {
            System.out.print(params.get(i + "").get());
        }
        System.out.println();
        return "";
    }

    // Prints out all the parameters passed, with format specifiers - this is
    // variadic, so their can be any number
    public String printfImplementation(HashMap<String, InterpreterDataType> params) {
        String text = params.get("0").get();
        params.remove("0");
        String temp = params.values().toString();
        String[] values = (temp.substring(1, temp.length() - 1)).split(", ");
        System.out.printf(text, (Object[]) values);
        System.out.println();
        return "";
    }

    public String getlineImplementation(HashMap<String, InterpreterDataType> params) {
        if (handler.splitAssign())
            return "1";
        return "0";
    }

    // Increments NR to the next line by calling splitAssign()
    public String nextImplementation(HashMap<String, InterpreterDataType> params) {
        handler.splitAssign();
        return "";
    }

    // "Global" substitution, this will replace all instances of the parameter
    // "regexp" with "replacement" in target. If target is not specified, then it
    // simply uses the whole line.
    public String gsubImplementation(HashMap<String, InterpreterDataType> params) {
        int count = 0;
        Pattern regexp = Pattern.compile(params.get("regexp").get());
        Matcher matcher;

        if (params.containsKey("target"))
            matcher = regexp.matcher(params.get("target").get());
        else
            matcher = regexp.matcher(variables.get("$0").get());

        while (matcher.find())
            count++;

        variables.put("$0", new InterpreterDataType(matcher.replaceAll(params.get("replacement").get())));
        return count + "";
    }

    // Finds the first index in "string" that matches the given parameter "regexp"
    public String matchImplementation(HashMap<String, InterpreterDataType> params) {
        Pattern regexp = Pattern.compile(params.get("regexp").get());
        Matcher matcher = regexp.matcher(params.get("string").get());
        if (matcher.find())
            return matcher.start() + 1 + "";
        return "0";
    }

    // Substitutes the first match of "regexp" in the "target" string with
    // "replacement". Uses the whole line if target isn't specified. In contrast
    // with gsub, this only replaces one match.
    public String subImplementation(HashMap<String, InterpreterDataType> params) {
        int count = 0;
        Pattern regexp = Pattern.compile(params.get("regexp").get());
        Matcher matcher;

        if (params.containsKey("target"))
            matcher = regexp.matcher(params.get("target").get());
        else
            matcher = regexp.matcher(params.get("$0").get());

        while (matcher.find())
            count++;
        variables.put("$0", new InterpreterDataType(matcher.replaceFirst(params.get("replacement").get())));
        return count + "";
    }

    // Returns the first index of "find" in the string "in", or returns "0" to
    // indicate it did not find a match.
    public String indexImplementation(HashMap<String, InterpreterDataType> params) {
        int i = params.get("in").get().indexOf(params.get("find").get());
        if (i == -1) {
            return "0";
        }
        return i + "";
    }

    // Returns the length of the given string.
    public String lengthImplementation(HashMap<String, InterpreterDataType> params) {
        return params.get("string").get().length() + "";
    }

    // Splits a string into an array using either the optional parameter "fieldsep"
    // or the default "FS" variable. The result is stored in "array", and the
    // function returns the length of said array. Also, if given parameter "seps",
    // the seperators will stored in it.
    public String splitImplementation(HashMap<String, InterpreterDataType> params) {
        String[] array;
        LinkedList<String> seps = new LinkedList<>();
        String string = params.get("string").get();

        if (params.containsKey("fieldsep")) {

            // if there is a seps parameter, loop through the string and save the seperators
            // in an array and store it in params
            if (params.containsKey("seps")) {

                for (int i = 0; i < string.length(); i++) {
                    if ((string.charAt(i) + "").matches(params.get("fieldsep").get())) {
                        seps.add(string.charAt(i) + "");
                    }
                }

                params.put("seps", new InterpreterArrayDataType(seps.toArray(new String[seps.size()])));
            }

            array = string.split(params.get("fieldsep").get());
            variables.put(params.get("array").get(), new InterpreterArrayDataType(array));

        }

        // Default case, fieldsep isn't passed
        else {
            array = string.split(variables.get("FS").get());
            variables.put(params.get("array").get(), new InterpreterArrayDataType(array));
            // params.put("array", new InterpreterArrayDataType(array));
        }

        return array.length + "";
    }

    // Returns a substring from "start" to optional parameter "length"; if length
    // isn't passed, then go to the end of the string.
    public String substrImplementation(HashMap<String, InterpreterDataType> params) {

        if (params.containsKey("length")) {

            return params.get("string").get().substring(Integer.parseInt(params.get("start").get()),
                    Integer.parseInt(params.get("length").get()));
        }

        return params.get("string").get().substring(Integer.parseInt(params.get("start").get()));
    }

    // Converts all characters in "string" to lower case
    public String tolowerImplementation(HashMap<String, InterpreterDataType> params) {
        return params.get("string").get().toLowerCase();
    }

    // Converts all characters in "string" to upper case
    public String toupperImplementation(HashMap<String, InterpreterDataType> params) {
        return params.get("string").get().toUpperCase();
    }

}
