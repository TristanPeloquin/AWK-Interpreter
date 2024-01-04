import java.io.IOException;
import java.util.LinkedList;
import java.util.HashMap;

//This class analyzes the String that gets passed to it and breaks it down into
//a list of Tokens (see more in the Token class), and throws an exception if 
//it encounters a an unknown character or incorrect number format
public class Lexer {

    // #region VARIABLES
    private StringHandler handler;
    private int lineNum;
    private int charPos;

    // These hash maps allow for quick look ups to see whether a key word
    // or symbol has a defined type
    private static HashMap<String, TokenType> keyWords;
    private static HashMap<String, TokenType> oneSymbols;
    private static HashMap<String, TokenType> twoSymbols;
    // #endregion

    public Lexer(String content) throws IOException {
        lineNum = 1;
        charPos = 1;
        handler = new StringHandler(content);
        keyWords = new HashMap<String, TokenType>();
        oneSymbols = new HashMap<String, TokenType>();
        twoSymbols = new HashMap<String, TokenType>();
        populateHashMaps();
    }

    // Associates all the keys and values for the hash maps,
    // used in processWords() and processSymbols
    private static void populateHashMaps() {
        keyWords.put("while", TokenType.WHILE);
        keyWords.put("if", TokenType.IF);
        keyWords.put("do", TokenType.DO);
        keyWords.put("for", TokenType.FOR);
        keyWords.put("break", TokenType.BREAK);
        keyWords.put("continue", TokenType.CONTINUE);
        keyWords.put("else", TokenType.ELSE);
        keyWords.put("return", TokenType.RETURN);
        keyWords.put("BEGIN", TokenType.BEGIN);
        keyWords.put("END", TokenType.END);
        keyWords.put("print", TokenType.PRINT);
        keyWords.put("printf", TokenType.PRINTF);
        keyWords.put("next", TokenType.NEXT);
        keyWords.put("in", TokenType.IN);
        keyWords.put("in", TokenType.IN);
        keyWords.put("delete", TokenType.DELETE);
        keyWords.put("getline", TokenType.GETLINE);
        keyWords.put("exit", TokenType.EXIT);
        keyWords.put("nextfile", TokenType.NEXTFILE);
        keyWords.put("function", TokenType.FUNCTION);

        oneSymbols.put("{", TokenType.BRACESTART);
        oneSymbols.put("}", TokenType.BRACEEND);
        oneSymbols.put("[", TokenType.BRACKETSTART);
        oneSymbols.put("]", TokenType.BRACKETEND);
        oneSymbols.put("(", TokenType.PARENTHESISSTART);
        oneSymbols.put(")", TokenType.PARENTHESISEND);
        oneSymbols.put("$", TokenType.DOLLAR);
        oneSymbols.put("~", TokenType.MATCH);
        oneSymbols.put("=", TokenType.EQUAL);
        oneSymbols.put("<", TokenType.LESSTHAN);
        oneSymbols.put(">", TokenType.GREATERTHAN);
        oneSymbols.put("!", TokenType.NOT);
        oneSymbols.put("+", TokenType.PLUS);
        oneSymbols.put("^", TokenType.EXPONENT);
        oneSymbols.put("-", TokenType.MINUS);
        oneSymbols.put("?", TokenType.TERNARY);
        oneSymbols.put(":", TokenType.COLON);
        oneSymbols.put("*", TokenType.TIMES);
        oneSymbols.put("/", TokenType.DIVIDE);
        oneSymbols.put("%", TokenType.MODULO);
        oneSymbols.put(";", TokenType.SEPERATOR);
        oneSymbols.put("\n", TokenType.SEPERATOR);
        oneSymbols.put("|", TokenType.OR);
        oneSymbols.put(",", TokenType.COMMA);

        twoSymbols.put(">=", TokenType.GREATEREQUAL);
        twoSymbols.put("++", TokenType.INCREMENT);
        twoSymbols.put("--", TokenType.DECREMENT);
        twoSymbols.put("<=", TokenType.LESSEQUAL);
        twoSymbols.put("==", TokenType.EQUALEQUAL);
        twoSymbols.put("!=", TokenType.NOTEQUAL);
        twoSymbols.put("^=", TokenType.EXPONENTEQUAL);
        twoSymbols.put("%=", TokenType.MODEQUAL);
        twoSymbols.put("*=", TokenType.TIMESEQUAL);
        twoSymbols.put("/=", TokenType.DIVIDEEQUAL);
        twoSymbols.put("+=", TokenType.PLUSEQUAL);
        twoSymbols.put("-=", TokenType.MINUSEQUAL);
        twoSymbols.put("!~", TokenType.NOTMATCH);
        twoSymbols.put("&&", TokenType.AND);
        twoSymbols.put(">>", TokenType.APPEND);
        twoSymbols.put("||", TokenType.OROR);

    }

    // The "main" method of the Lexer, this tokenizes the String that gets
    // passed through the constructor and returns the list of tokens;
    // assumes that a word starts with a letter and a number starts with a digit/'.'
    public LinkedList<Token> lex() throws IllegalArgumentException {
        LinkedList<Token> tokens = new LinkedList<Token>();

        // This loop runs until it reaches the end of the document
        while (!handler.isDone()) {

            // nextChar allows us to analyze the char were at without
            // incrementing the handler index by using peek
            char thisChar = handler.peek(0);
            Token symbol;

            if (thisChar == '#') {
                processComment();
            }

            // if this char is a space or a tab, skip and increment the position
            else if (thisChar == ' ' || thisChar == '\t') {
                handler.swallow(1);
                charPos++;
            }

            // if this char is a new line, add a seperator token, increment the line number,
            // reset the position, and finally skip over the character
            else if (thisChar == '\n') {
                tokens.add(new Token(TokenType.SEPERATOR, lineNum, charPos));
                lineNum++;
                charPos = 0;
                handler.swallow(1);
            }

            // if this char is a return carriage, then skip the character
            else if (thisChar == '\r') {
                handler.swallow(1);
            }

            // if this char is a letter, process the word and add it to the list
            else if (Character.isLetter(thisChar)) {
                tokens.add(processWord());
            }

            // if this char is a number, process the number and add it to the list
            else if (Character.isDigit(thisChar)) {
                tokens.add(processNumber());
            }

            else if (thisChar == '`') {
                tokens.add(processPattern());
            }

            else if (thisChar == '"') {
                tokens.add(processStringLiteral());
            }

            else if ((symbol = processSymbol()) != null) {
                tokens.add(symbol);
            }

            // if none of the above were true, this char is an unknown character
            else {
                throw new IllegalArgumentException("Unknown character; Line: " + lineNum + "; Position: " + charPos);
            }
        }
        return tokens;
    }

    // This method increments through a series of characters and
    // adds them to a String in an attempt to form a word token.
    // Assumes the first character is a letter
    private Token processWord() {
        String word = "";
        int position = charPos;

        // Adds a letter to the String if the handler is not at the end of the
        // document, and continues if the character is a digit, letter, or underscore
        while ((!handler.isDone() && (Character.isDigit(handler.peek(0)) || Character.isLetter(handler.peek(0))
                || handler.peek(0) == '_'))) {
            word += handler.getChar();
            charPos++;
        }

        // Checks if the word is actually a key word in AWK
        if (keyWords.containsKey(word)) {
            return new Token(keyWords.get(word), lineNum, position, word);
        }

        return new Token(TokenType.WORD, lineNum, position, word);
    }

    // This method increments through a series of characters and
    // adds them to a String in an attempt to form a number token.
    // Assumes the first character is a digit.
    private Token processNumber() {
        String number = "";
        int position = charPos;
        int decimals = 0;

        // Adds a number or '.' to the String as long as the handler isn't done and the
        // character is either
        // a digit or decimal
        while (!handler.isDone() && (Character.isDigit(handler.peek(0)) || handler.peek(0) == '.')) {
            // if the current character is a decimal, increment the decimal counter
            if (handler.peek(0) == '.') {
                decimals++;
            }
            // if we have more than one decimal in the number, throw a number format
            // exception
            if (decimals > 1) {
                throw new NumberFormatException("Number not valid; Line: " + lineNum + "; Position: " + charPos);
            }

            number += handler.getChar();
            charPos++;
        }

        // This condition checks if after the number is made we have encountered a
        // non valid character in our number (a letter or decimal)
        if (!handler.isDone() && (Character.isLetter(handler.peek(0)) && handler.peek(0) != '.')) {
            throw new NumberFormatException("Number not valid; Line: " + lineNum + "; Position: " + charPos);
        }

        return new Token(TokenType.NUMBER, lineNum, position, number);
    }

    // This method handles a String within AWK by accounting for quotation marks
    // and creating a Token of type STRINGLITERAL
    private Token processStringLiteral() {
        handler.swallow(1);
        String literal = "";
        int position = charPos;

        // This loops until either it reaches the end of the document or it finds
        // another quotation mark
        while (!handler.isDone()) {

            // This condition checks if there is an escaped quotation mark, which
            // if true adds it to the String and updates position
            if (handler.peek(0) == '\\' && handler.peek(1) == '"') {
                handler.swallow(1);
                charPos++;
            }
            // If there is an unescaped quotation mark, break out
            else if (handler.peek(0) == '"') {
                handler.swallow(1);
                break;
            }
            literal += handler.getChar();
            charPos++;
        }
        return new Token(TokenType.STRINGLITERAL, lineNum, position, literal);
    }

    // This method swallows all characters from the position it is called
    // until it meets a new line character
    private void processComment() {
        handler.swallow(0);
        while (!handler.isDone() && handler.peek(0) != '\n') {
            handler.swallow(1);
        }
        return;
    }

    // This method behaves identically as processStringLiteral() except
    // with the designated Pattern symbol, "`", or backtick
    private Token processPattern() {
        handler.swallow(1);
        String pattern = "";
        int position = charPos;

        while (!handler.isDone()) {

            if (handler.peek(0) == '\\' && handler.peek(1) == '`') {
                handler.swallow(1);
                charPos++;
            } else if (handler.peek(0) == '`') {
                handler.swallow(1);
                break;
            }
            pattern += handler.getChar();
            charPos++;
        }
        return new Token(TokenType.PATTERN, lineNum, position, pattern);
    }

    // This method handles symbols within the document, which can either
    // be one or two symbols long
    private Token processSymbol() {
        String symbol = "";
        // This condition checks if the next two characters match a key
        // within the twoSymbols hash map and returns said symbol
        if (twoSymbols.containsKey(handler.peekString(2))) {
            symbol += handler.getChar();
            symbol += handler.getChar();
            charPos += 2;
            return new Token(twoSymbols.get(symbol), lineNum, charPos - 2, symbol);
        }
        // This condition checks if the next character matches a key
        // within the oneSymbols hash map and returns said symbol
        else if (oneSymbols.containsKey(handler.peekString(1))) {
            symbol += handler.getChar();
            charPos++;
            return new Token(oneSymbols.get(symbol), lineNum, charPos - 1, symbol);
        }
        // If it is not a one or two symbol, then it is not a symbol, return null
        else {
            return null;
        }
    }

}
