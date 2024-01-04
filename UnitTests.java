/*
 * import org.junit.*;
 * 
 * import static org.junit.Assert.assertEquals;
 * import static org.junit.Assert.assertTrue;
 * import java.util.LinkedList;
 * 
 * public class UnitTests {
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
 * public void peekTest() throws Exception {
 * init("BEGIN\nfunction helloWorld(test1, test2)");
 * assertEquals(handler.peek(0).get().getType(), TokenType.BEGIN);
 * assertEquals(handler.peek(1).get().getType(), TokenType.SEPERATOR);
 * assertEquals(handler.peek(2).get().getType(), TokenType.FUNCTION);
 * }
 * 
 * @Test
 * public void moreTokensTest() throws Exception {
 * init("BEGIN\nfunction helloWorld(test1, test2)");
 * assertTrue(handler.moreTokens());
 * }
 * 
 * @Test
 * public void matchAndRemoveTest() throws Exception {
 * init("BEGIN\nfunction helloWorld(test1, test2)");
 * assertEquals(handler.matchAndRemove(TokenType.BEGIN).get().getType(),
 * TokenType.BEGIN);
 * assertEquals(handler.matchAndRemove(TokenType.SEPERATOR).get().getType(),
 * TokenType.SEPERATOR);
 * }
 * 
 * @Test
 * public void parseTest() throws Exception {
 * init("BEGIN\nfunction helloWorld(test1, test2)");
 * ProgramNode programNode = parser.parse();
 * System.out.println(programNode);
 * assertEquals(programNode.beginBlocks.size(), 1);
 * assertEquals(programNode.funcDefNodes.size(), 1);
 * }
 * 
 * @Test
 * public void parseOperationTest() throws Exception {
 * init("++x");
 * assertEquals("PREINCx", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parseStringLiteralTest() throws Exception {
 * init("\"hello world\"");
 * assertEquals("hello world", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parseNumberTest() throws Exception {
 * init("123");
 * assertEquals("123", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parsePatternTest() throws Exception {
 * init("`iampattern`");
 * assertEquals("iampattern", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parseParenthesesTest() throws Exception {
 * init("(theseareparentheses)");
 * assertEquals("theseareparentheses",
 * parser.parseOperation().get().toString());
 * }
 * 
 * @Test(expected = Exception.class)
 * public void parseParenthesesExceptionTest() throws Exception {
 * init("(");
 * parser.parseOperation();
 * }
 * 
 * @Test
 * public void parseNotTest() throws Exception {
 * init("!variable");
 * assertEquals("NOTvariable", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parseMinusTest() throws Exception {
 * init("-helloworld");
 * assertEquals("UNARYNEGhelloworld", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parsePlusTest() throws Exception {
 * init("+aaaa");
 * assertEquals("UNARYPOSaaaa", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parseDecrementTest() throws Exception {
 * init("--abc");
 * assertEquals("PREDECabc", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parseLValueTest() throws Exception {
 * init("$666");
 * assertEquals("DOLLAR666", parser.parseLValue().get().toString());
 * }
 * 
 * @Test
 * public void parseArrayTest() throws Exception {
 * init("x[++a]");
 * assertEquals("x[PREINCa]", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parsePatternArrayTest() throws Exception {
 * init("`[abc]`");
 * assertEquals("[abc]", parser.parseOperation().get().toString());
 * }
 * 
 * @Test
 * public void parseFieldIncrement() throws Exception {
 * init("++$6");
 * assertEquals("PREINCDOLLAR6", parser.parseOperation().get().toString());
 * }
 * 
 * }
 */