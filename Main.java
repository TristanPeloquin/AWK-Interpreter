
import java.nio.file.Files;
import java.nio.file.Paths;

//This class simply reads in the document, runs the lexer,
//and prints the list of tokens it creates
public class Main {
    public static void main(String args[]) throws Exception {
        String code = "";
        String text = "";
        // This condition allows for simple input of a String without
        // the need for a file, intended for debugging
        if (args[0].equals("debug")) {
            code = args[1];
            text = args[2];
        } else {
            code = new String(Files.readAllBytes(Paths.get(args[0])));
            text = Paths.get(args[1]).toString();
        }
        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer.lex());
        Interpreter interpreter = new Interpreter(parser.parse(), text);
        interpreter.interpretProgram();
    }
}
