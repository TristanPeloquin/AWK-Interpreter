/*
 * import org.junit.*;
 * 
 * import static org.junit.Assert.assertEquals;
 * import static org.junit.Assert.assertTrue;
 * import java.util.LinkedList;
 * 
 * public class UnitTests1 {
 * 
 * private Parser parser;
 * private TokenHandler handler;
 * 
 * public void init(String content) throws Exception {
 * Lexer lexer = new Lexer(content);
 * LinkedList<Token> tokens = lexer.lex();
 * parser = new Parser(tokens);
 * handler = new TokenHandler(tokens);
 * }
 * 
 * @Test
 * public void ternaryTest() throws Exception {
 * init("a ? b ? d : e ? x : y : c ? f : g");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void expressionTest() throws Exception {
 * init("1 + 2 + 3 + 4 + 5");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void termTest() throws Exception {
 * init("(1*2)/3*4/5*6");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void postOpTest() throws Exception {
 * init("x++");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void concatTest() throws Exception {
 * init("\"helloworld\" 5 \"test\"");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void bottomLevelTest() throws Exception {
 * init("++hello");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void exponentTest() throws Exception {
 * init("x^y^z^w");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void assignmentTest() throws Exception {
 * init("x += a += b += c");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void booleanOpTest() throws Exception {
 * init("(1 || x) || (2 && 3 && 4) || 5");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void arrayTest() throws Exception {
 * init("a in b in c in d");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void matchTest() throws Exception {
 * init("hello ~ world thisisa !~ test");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void fieldArrayTest() throws Exception {
 * init("a[2+2] $6");
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * @Test
 * public void stressTest() throws Exception {
 * init("a ~ b * 5 (6^3)^25 && 32 + !x % 123 <= 12345 - (5*3) x+=a%=urmom `helloworld` * 5 in array + a[7*2+100] ? 1 : 2"
 * );
 * System.out.println(parser.parseAssignment().get().toString());
 * }
 * 
 * }
 */