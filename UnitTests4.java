import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import org.junit.Test;

public class UnitTests4 {

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

    

}
