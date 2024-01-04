/*
 * import org.junit.*;
 * 
 * import static org.junit.Assert.assertEquals;
 * import java.util.LinkedList;
 * 
 * public class UnitTests2 {
 * 
 * private Parser parser;
 * 
 * public void init(String content) throws Exception {
 * Lexer lexer = new Lexer(content);
 * LinkedList<Token> tokens = lexer.lex();
 * parser = new Parser(tokens);
 * }
 * 
 * @Test
 * public void parseBlockTest() throws Exception {
 * init("if(1)\na=1\nelse if(1)\nb=1\nelse\nc=1");
 * assertEquals("if ( 1 ){\n\t[a = 1]\n\t}\n\telse if ( 1 ){\n\t[b = 1]\n\t}\n\telse {\n\t[c = 1]\n\t}\n"
 * ,
 * parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseForTest() throws Exception {
 * init("for(i = 0; i<10; i++) a++");
 * assertEquals("for ( i = 0; i LT 10; POSTINC i) {\n\t[POSTINC a]\n\t}\n",
 * parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseDeleteTest() throws Exception {
 * init("delete a[0]");
 * assertEquals("delete a[0]", parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseWhileTest() throws Exception {
 * init("while(y){x++}");
 * assertEquals("while(y){\n\t[POSTINC x]\n\t}\n",
 * parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseDoWhileTest() throws Exception {
 * init("do{++y}while(hello)}");
 * assertEquals("do{\n\t[PREINC y]\n\t}\n\twhile(hello)",
 * parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseFunctionCallTest() throws Exception {
 * init("a(b,c)");
 * assertEquals("a([b, c])", parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseForInTest() throws Exception {
 * init("for(a in b){i ^= x}");
 * assertEquals("for (a IN b){\n\t[i = i EXPONENT x]\n\t}\n",
 * parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseContinueTest() throws Exception {
 * init("continue");
 * assertEquals("continue", parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseBreakTest() throws Exception {
 * init("break");
 * assertEquals("break", parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseReturnTest() throws Exception {
 * init("return");
 * assertEquals("return", parser.parseStatement().get().toString());
 * }
 * 
 * @Test
 * public void parseBeginBlockTest() throws Exception {
 * init("BEGIN{a=1}");
 * assertEquals(1, parser.parse().beginBlocks.size());
 * }
 * 
 * @Test
 * public void parseEndBlockTest() throws Exception {
 * init("END{b=1}");
 * assertEquals(1, parser.parse().endBlocks.size());
 * }
 * 
 * @Test
 * public void parseBeginBlock() throws Exception {
 * init("(x==5){c=1}");
 * assertEquals(1, parser.parse().blocks.size());
 * }
 * 
 * @Test
 * public void parseFunction() throws Exception {
 * init("function doStuff(a,b){c=1}");
 * assertEquals(1, parser.parse().funcDefNodes.size());
 * }
 * 
 * @Test(expected = Exception.class)
 * public void syntaxErrorTest1() throws Exception {
 * init("BEGIN{a=1");
 * parser.parse();
 * }
 * 
 * @Test(expected = Exception.class)
 * public void syntaxErrorTest2() throws Exception {
 * init("{if()a=1}");
 * parser.parse();
 * }
 * 
 * @Test(expected = Exception.class)
 * public void syntaxErrorTest3() throws Exception {
 * init("{a(b,c,#)}");
 * parser.parse();
 * }
 * 
 * }
 */
