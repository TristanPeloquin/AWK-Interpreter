import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import org.junit.Test;

public class UnitTests3 {

    private Parser parser;
    private Interpreter interpreter;
    private Interpreter.LineHandler handler;
    private HashMap<String, InterpreterDataType> params;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    public void init(String content) throws Exception {
        Lexer lexer = new Lexer(content);
        LinkedList<Token> tokens = lexer.lex();
        parser = new Parser(tokens);
        interpreter = new Interpreter(new ProgramNode(), "");
        handler = interpreter.new LineHandler(new LinkedList<String>());
        params = new HashMap<>();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void parseGetlineTest() throws Exception {
        init("getline");
        assertEquals("GETLINE", parser.parseStatement().get().toString());
    }

    @Test
    public void parsePrintTest() throws Exception {
        init("print (\"hello\")");
        assertEquals("PRINT [hello]", parser.parseStatement().get().toString());
    }

    @Test
    public void parsePrintfTest() throws Exception {
        init("printf (\"%i\", 12)");
        assertEquals("PRINTF [%i, 12]", parser.parseStatement().get().toString());
    }

    @Test
    public void parseExitTest() throws Exception {
        init("exit 1");
        assertEquals("EXIT [1]", parser.parseStatement().get().toString());
    }

    @Test
    public void parseNextfileTest() throws Exception {
        init("nextfile");
        assertEquals("NEXTFILE", parser.parseStatement().get().toString());
    }

    @Test
    public void parseNextTest() throws Exception {
        init("next");
        assertEquals("NEXT", parser.parseStatement().get().toString());
    }

    @Test
    public void lineHandlerTest() throws Exception {
        init("");
        assertEquals(false, handler.splitAssign());
    }

    @Test
    public void printTest() throws Exception {
        init("");
        InterpreterArrayDataType arrayParams = new InterpreterArrayDataType("hello", " world");
        interpreter.printImplementation(arrayParams.getArray());
        assertEquals("hello world", outputStreamCaptor.toString().trim());
    }

    @Test
    public void printfTest() throws Exception {
        init("");
        InterpreterArrayDataType arrayParams = new InterpreterArrayDataType("%s %s test", "hello", "world");
        interpreter.printfImplementation(arrayParams.getArray());
        assertEquals("hello world test", outputStreamCaptor.toString().trim());
    }

    @Test
    public void getlineTest() throws Exception {
        init("");
        assertEquals("0", interpreter.getlineImplementation(params));
    }

    @Test
    public void nextTest() throws Exception {
        init("");
        assertEquals("", interpreter.nextImplementation(params));
    }

    @Test
    public void gsubTest() throws Exception {
        init("");
        params.put("regexp", new InterpreterDataType("hello"));
        params.put("replacement", new InterpreterDataType("world"));
        params.put("target", new InterpreterDataType("helloworld"));
        assertEquals("1", interpreter.gsubImplementation(params));
    }

    @Test
    public void matchTest() throws Exception {
        init("");
        params.put("regexp", new InterpreterDataType("hello"));
        params.put("string", new InterpreterDataType("worldhello"));
        assertEquals("5", interpreter.matchImplementation(params));
    }

    @Test
    public void subTest() throws Exception {
        init("");
        params.put("regexp", new InterpreterDataType("hello"));
        params.put("replacement", new InterpreterDataType("world"));
        params.put("target", new InterpreterDataType("helloworld"));
        assertEquals("1", interpreter.gsubImplementation(params));
    }

    @Test
    public void indexTest() throws Exception {
        init("");
        params.put("in", new InterpreterDataType("helloworld"));
        params.put("find", new InterpreterDataType("world"));
        assertEquals("5", interpreter.indexImplementation(params));
    }

    @Test
    public void lengthTest() throws Exception {
        init("");
        params.put("string", new InterpreterDataType("helloworld"));
        assertEquals("10", interpreter.lengthImplementation(params));
    }

    @Test
    public void splitTest1() throws Exception {
        init("");
        params.put("string", new InterpreterDataType("hello world"));
        params.put("array", new InterpreterArrayDataType());
        params.put("fieldsep", new InterpreterDataType(" "));
        params.put("sep", new InterpreterArrayDataType());
        assertEquals("2", interpreter.splitImplementation(params));
    }

    @Test
    public void splitTest2() throws Exception {
        init("");
        params.put("string", new InterpreterDataType("hello-world"));
        params.put("array", new InterpreterArrayDataType());
        params.put("fieldsep", new InterpreterDataType("-"));
        params.put("sep", new InterpreterArrayDataType());
        assertEquals("2", interpreter.splitImplementation(params));
    }

    @Test
    public void substrTest() throws Exception {
        init("");
        params.put("string", new InterpreterDataType("hello world"));
        params.put("start", new InterpreterDataType("6"));
        assertEquals("world", interpreter.substrImplementation(params));
    }

    @Test
    public void toupperTest() throws Exception {
        init("");
        params.put("string", new InterpreterDataType("hello world"));
        assertEquals("HELLO WORLD", interpreter.toupperImplementation(params));
    }

    @Test
    public void tolowerTest() throws Exception {
        init("");
        params.put("string", new InterpreterDataType("Hello World"));
        assertEquals("hello world", interpreter.tolowerImplementation(params));
    }

    @Test
    public void assignmentTest() throws Exception {
        init("");
        AssignmentNode node = new AssignmentNode(new VariableReferenceNode("a", Optional.empty()),
                new ConstantNode("15"));
        interpreter.getIDT(node, null);
        assertEquals("15", interpreter.variables.get("a").get());
    }

    @Test
    public void constantTest() throws Exception {
        init("");
        ConstantNode node = new ConstantNode("9");
        assertEquals("9", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void ternaryTest1() throws Exception {
        init("");
        TernaryNode node = new TernaryNode(
                new OperationNode(new ConstantNode("0"), OperationNode.Operations.GT,
                        Optional.of(new ConstantNode("1"))),
                new AssignmentNode(new VariableReferenceNode("a", Optional.empty()), new ConstantNode("1")),
                new AssignmentNode(new VariableReferenceNode("b", Optional.empty()), new ConstantNode("0")));
        interpreter.getIDT(node, null);
        assertEquals("0", interpreter.variables.get("b").get());
    }

    @Test
    public void ternaryTest2() throws Exception {
        init("");
        TernaryNode node = new TernaryNode(
                new OperationNode(new ConstantNode("0"), OperationNode.Operations.LE,
                        Optional.of(new ConstantNode("1"))),
                new AssignmentNode(new VariableReferenceNode("a", Optional.empty()), new ConstantNode("1")),
                new AssignmentNode(new VariableReferenceNode("b", Optional.empty()), new ConstantNode("0")));
        interpreter.getIDT(node, null);
        assertEquals("1", interpreter.variables.get("a").get());
    }

    @Test
    public void arrayTest() throws Exception {
        init("");
        AssignmentNode node1 = new AssignmentNode(new VariableReferenceNode("a",
                Optional.of(new ConstantNode("1"))),
                new ConstantNode("15"));
        AssignmentNode node2 = new AssignmentNode(new VariableReferenceNode("a",
                Optional.of(new ConstantNode("2"))),
                new ConstantNode("30"));
        interpreter.getIDT(node1, null);
        interpreter.getIDT(node2, null);
        VariableReferenceNode node3 = new VariableReferenceNode("a",
                Optional.of(new ConstantNode("1")));
        assertEquals("15", interpreter.getIDT(node3, null).toString());
        VariableReferenceNode node4 = new VariableReferenceNode("a",
                Optional.of(new ConstantNode("2")));
        assertEquals("30", interpreter.getIDT(node4, null).toString());
    }

    @Test
    public void addTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.ADD,
                Optional.of(new ConstantNode("1")));
        assertEquals("2.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void subtractTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.SUBTRACT,
                Optional.of(new ConstantNode("1")));
        assertEquals("0.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void multiplyTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("2"), OperationNode.Operations.MULTIPLY,
                Optional.of(new ConstantNode("3")));
        assertEquals("6.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void divideTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("6"), OperationNode.Operations.DIVIDE,
                Optional.of(new ConstantNode("3")));
        assertEquals("2.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void moduloTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("6"), OperationNode.Operations.MODULO,
                Optional.of(new ConstantNode("3")));
        assertEquals("0.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void matchesTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("hello"), OperationNode.Operations.MATCH,
                Optional.of(new PatternNode("hello")));
        assertEquals("1", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void andTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.AND,
                Optional.of(new ConstantNode("0")));
        assertEquals("0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void orTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.OR,
                Optional.of(new ConstantNode("0")));
        assertEquals("1", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void notTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.NOT);
        assertEquals("0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void equalsTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.EQ,
                Optional.of(new ConstantNode("1")));
        assertEquals("1", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void notEqualsTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.NE,
                Optional.of(new ConstantNode("0")));
        assertEquals("1", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void lessThanTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.LT,
                Optional.of(new ConstantNode("0")));
        assertEquals("0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void lessEqualTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1.5"), OperationNode.Operations.LE,
                Optional.of(new ConstantNode("1.5")));
        assertEquals("1", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void greaterThanTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("5"), OperationNode.Operations.GT,
                Optional.of(new ConstantNode("2")));
        assertEquals("1", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void greaterEqualTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("5"), OperationNode.Operations.GE,
                Optional.of(new ConstantNode("5")));
        assertEquals("1", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void concatenationTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("hello"), OperationNode.Operations.CONCATENATION,
                Optional.of(new ConstantNode("world")));
        assertEquals("helloworld", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void dollarTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("10"), OperationNode.Operations.DOLLAR);
        assertEquals("", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void preIncTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.PREINC);
        assertEquals("2.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void preDecTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.PREDEC);
        assertEquals("0.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void postIncTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.POSTINC);
        assertEquals("2.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void postDecTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.POSTDEC);
        assertEquals("0.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void unaryPosTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.UNARYPOS);
        assertEquals("1.0", interpreter.getIDT(node, null).toString());
    }

    @Test
    public void unaryNegTest() throws Exception {
        init("");
        OperationNode node = new OperationNode(new ConstantNode("1"), OperationNode.Operations.UNARYNEG);
        assertEquals("-1.0", interpreter.getIDT(node, null).toString());
    }

    @Test(expected = Exception.class)
    public void exceptionTest1() throws Exception {
        init("");
        AssignmentNode node = new AssignmentNode(new ConstantNode("1"), Optional.empty());
        interpreter.getIDT(node, null);
    }

    @Test(expected = Exception.class)
    public void exceptionTest2() throws Exception {
        init("");
        AssignmentNode node = new AssignmentNode(
                new OperationNode(new ConstantNode("1"), OperationNode.Operations.POSTINC), Optional.empty());
        interpreter.getIDT(node, null);
    }

}
