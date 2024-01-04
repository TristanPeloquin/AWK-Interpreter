import java.util.LinkedList;
import java.util.Optional;

//Represents a parser, which will recognize key words, blocks, statements, etc. from a list of tokens and assign 
//meaning to them, falling under the umbrella of being either a function or an action in AWK, and creates an AST
//from said blocks and functions.
public class Parser {

    private TokenHandler handler;
    private ProgramNode tree;

    public Parser(LinkedList<Token> tokens) {
        handler = new TokenHandler(tokens);
        tree = new ProgramNode();
    }

    // Accepts new lines and semi-colons until there are neither of each
    private boolean acceptSeperators() {
        boolean retVal = false;
        while (handler.matchAndRemove(TokenType.SEPERATOR).isPresent()) {
            retVal = true;
        }
        return retVal;
    }

    // The main method of Parser, this will parse the entirety of the token list
    // into either
    // a function or an action
    public ProgramNode parse() throws Exception {
        // Loops until their are no more tokens in the list
        while (handler.moreTokens()) {
            acceptSeperators();
            if (parseFunction() || parseAction()) {
                acceptSeperators();
                continue;
            }
            throw new Exception("Not a function or action");
        }
        return tree;
    }

    // Parses through a function declaration; returns true if it is a function
    private boolean parseFunction() throws Exception {
        FunctionDefinitionNode funcDefNode;

        // If the function keyword is present
        if (handler.matchAndRemove(TokenType.FUNCTION).isPresent()) {

            // Saves the name of the function
            Optional<Token> name = handler.matchAndRemove(TokenType.WORD);

            if (name.isPresent()) {
                // Creates the Node, calling helper method parseParameters and parseBlock in
                // order to handle the more complex parsing
                funcDefNode = new FunctionDefinitionNode(name.get().getValue(), parseParameters(), parseBlock());
                tree.funcDefNodes.add(funcDefNode);
                return true;
            }
            throw new Exception("Syntax error: missing function name");
        }
        return false;
    }

    // Helper method for parseFunction which parses through the list of parameters
    // given in a function declaration
    private LinkedList<Node> parseParameters() throws Exception {
        LinkedList<Node> paramNames = new LinkedList<Node>();

        // if there is a start parenthesis, parse, else throw an exception
        if (handler.matchAndRemove(TokenType.PARENTHESISSTART).isPresent()) {

            // Loops until it finds an end parenthesis
            while (!handler.matchAndRemove(TokenType.PARENTHESISEND).isPresent()) {
                Optional<Node> expression = parseOperation();

                if (expression.isPresent()) {
                    paramNames.add(expression.get());
                }

                // if there is a comma token and the next token is an end parenthesis,
                // throw an exception
                if (handler.matchAndRemove(TokenType.COMMA).isPresent()
                        && handler.peek(0).get().getType() == TokenType.PARENTHESISEND) {
                    throw new Exception("Syntax error: invalid comma token");
                }
                acceptSeperators();
            }

            return paramNames;
        }
        throw new Exception("Syntax error: missing parenthesis");
    }

    // Parses through an action, categorizing it as either a begin block, end block
    // or other block
    private boolean parseAction() throws Exception {
        // BEGIN blocks
        if (handler.matchAndRemove(TokenType.BEGIN).isPresent()) {
            tree.beginBlocks.add(parseBlock());
            return true;
        }
        // END blocks
        else if (handler.matchAndRemove(TokenType.END).isPresent()) {
            tree.endBlocks.add(parseBlock());
            return true;
        }
        // other blocks
        else {
            Optional<Node> condition = parseOperation();
            tree.blocks.add(new BlockNode(parseBlock().getStatements(), condition));
            return true;
        }
    }

    // Parses through blocks, signified by curly braces, used primaryly in
    // parseAction(), parseFunction(), and statements such as if and while
    // Throws an exception if a single statement block has no statement
    private BlockNode parseBlock() throws Exception {

        LinkedList<Node> statements = new LinkedList<>();

        // if there is a curly brace, it has multiple statements,
        // else it is just a one line block e.g if(true) x++;
        if (handler.matchAndRemove(TokenType.BRACESTART).isPresent()) {
            acceptSeperators();
            Optional<Node> statement;

            // Adds all statements into a list to return as a BlockNode
            while (!handler.matchAndRemove(TokenType.BRACEEND).isPresent()) {
                statement = parseStatement();
                statements.add(statement.get());
                acceptSeperators();
            }
            return new BlockNode(statements, Optional.empty());
        }

        else {
            acceptSeperators();
            Optional<Node> statement = parseStatement();
            if (statement.isPresent()) {
                statements.add(statement.get());
                acceptSeperators();
                return new BlockNode(statements, Optional.empty());
            }
            throw new Exception("Syntax error: missing statement in block");
        }

    }

    // Parses through all statements types, including continue, break,
    // if, for, delete, while, do-while, return, and operations.
    public Optional<Node> parseStatement() throws Exception {
        Optional<Node> statement = parseContinue();
        if (statement.isPresent())
            return statement;
        statement = parseBreak();
        if (statement.isPresent())
            return statement;
        statement = parseIf();
        if (statement.isPresent())
            return statement;
        statement = parseFor();
        if (statement.isPresent())
            return statement;
        statement = parseDelete();
        if (statement.isPresent())
            return statement;
        statement = parseWhile();
        if (statement.isPresent())
            return statement;
        statement = parseDoWhile();
        if (statement.isPresent())
            return statement;
        statement = parseReturn();
        if (statement.isPresent())
            return statement;
        statement = parseOperation();
        return statement;
    }

    // Parses the continue keyword
    private Optional<Node> parseContinue() {

        if (handler.matchAndRemove(TokenType.CONTINUE).isPresent()) {
            return Optional.of(new ContinueNode());
        }

        return Optional.empty();
    }

    // Parses the break keyword
    private Optional<Node> parseBreak() {

        if (handler.matchAndRemove(TokenType.BREAK).isPresent()) {
            return Optional.of(new BreakNode());
        }

        return Optional.empty();
    }

    // Parses an if statement, which has three variations - if, else if, else.
    // Returns either a new IfNode pointing to the next else if, an IfNode
    // containing the else block, a basic IfNode without else, or empty.
    // Throws an exception if it is missing an expression in the parentheses
    // or missing a parenthesis
    private Optional<Node> parseIf() throws Exception {

        if (handler.matchAndRemove(TokenType.IF).isPresent()) {
            if (handler.matchAndRemove(TokenType.PARENTHESISSTART).isPresent()) {

                Optional<Node> condition = parseOperation();
                if (condition.isPresent()) {

                    if (handler.matchAndRemove(TokenType.PARENTHESISEND).isPresent()) {

                        BlockNode block = parseBlock();
                        acceptSeperators();
                        // if there is an else token, parses for else if or else,
                        // else return a regular IfNode
                        if (handler.matchAndRemove(TokenType.ELSE).isPresent()) {

                            // Calls parseIf recursively to create a chain of if - else if
                            Optional<Node> nextIf = parseIf();
                            if (nextIf.isPresent()) {
                                return Optional.of(new IfNode(condition, block, nextIf));
                            }
                            // Returns an IfNode with the block for else contained inside
                            return Optional.of(new IfNode(condition, block,
                                    Optional.of(parseBlock())));
                        }
                        return Optional.of(new IfNode(condition, block, Optional.empty()));

                    }
                    throw new Exception("Syntax error: missing end parenthesis on if statement");
                }
                throw new Exception("Syntax error: missing expression after parenthesis");
            }
            throw new Exception("Syntax error: missing start parenthesis on if statement");
        }

        return Optional.empty();
    }

    // Parses a for statement for its three expression, formatted as:
    // for(expression1; expression2; expression3){} OR as a for in
    // Returns either a ForInNode or a ForNode with their expressions
    // Throws an exception for missing parentheses, seperators, and
    // a missing expression in a for in
    private Optional<Node> parseFor() throws Exception {

        if (handler.matchAndRemove(TokenType.FOR).isPresent()) {
            if (handler.matchAndRemove(TokenType.PARENTHESISSTART).isPresent()) {

                // Peeks for an in keyword, indicating its for in loop, then parses its
                // condition
                if (handler.peek(1).get().getType().equals(TokenType.IN)) {
                    Optional<Node> condition = parseOperation();
                    if (condition.isPresent()) {
                        if (handler.matchAndRemove(TokenType.PARENTHESISEND).isPresent()) {
                            return Optional.of(new ForInNode(condition.get(), parseBlock()));
                        }
                        throw new Exception("Syntax error: missing end parenthesis on for in");
                    }
                    throw new Exception("Syntax error: missing condition on for in");
                }

                // Parses for a regular for loop by parsing the operation and the seperators in
                // between. Each expression is optional - for(;;) is valid.
                Optional<Node> expression1 = parseOperation();
                if (handler.matchAndRemove(TokenType.SEPERATOR).isPresent()) {
                    Optional<Node> expression2 = parseOperation();
                    if (handler.matchAndRemove(TokenType.SEPERATOR).isPresent()) {
                        Optional<Node> expression3 = parseOperation();
                        if (handler.matchAndRemove(TokenType.PARENTHESISEND).isPresent()) {
                            return Optional.of(new ForNode(expression1, expression2, expression3,
                                    parseBlock()));
                        }
                        throw new Exception("Syntax error: missing end parenthesis on for");
                    }
                    throw new Exception("Syntax error: no seperator after expression");
                }
                throw new Exception("Syntax error: no seperator after expression");
            }
            throw new Exception("Syntax error: invalid for statement");
        }

        return Optional.empty();

    }

    // Parses for the delete keyword and its argument
    // Returns a DeleteNode with its expression or empty
    // Throws an exception if there's no expression
    private Optional<Node> parseDelete() throws Exception {

        if (handler.matchAndRemove(TokenType.DELETE).isPresent()) {
            Optional<Node> expression = parseOperation();
            if (expression.isPresent()) {
                return Optional.of(new DeleteNode(expression.get()));
            }
            throw new Exception("Syntax error: missing expression on delete");
        }

        return Optional.empty();
    }

    // Parses for the while keyword and its condition inside (considered an
    // operation)
    // Returns WhileNode with its condition and and block, or empty
    // Throws an exception if its missing parentheses or its condition
    private Optional<Node> parseWhile() throws Exception {

        if (handler.matchAndRemove(TokenType.WHILE).isPresent()) {
            if (handler.matchAndRemove(TokenType.PARENTHESISSTART).isPresent()) {

                // Parses the operation inside the parentheses
                Optional<Node> expression = parseOperation();
                if (expression.isPresent()) {

                    if (handler.matchAndRemove(TokenType.PARENTHESISEND).isPresent()) {
                        return Optional.of(new WhileNode(expression.get(), parseBlock()));
                    }

                    throw new Exception("Syntax error: missing end parenthesis on while statement");
                }

                throw new Exception("Syntax error: condition on while statement");
            }
            throw new Exception("Syntax error: missing start parenthesis on while statement");
        }

        return Optional.empty();
    }

    // Parses do-while; almost identical to parseWhile() except it also parses for
    // the do keyword before returning a DoWhileNode
    // Returns either a DoWhileNode with its condition and block or empty
    // Throws exception if thre is a missing parenthesis, condition, or while
    // statement
    private Optional<Node> parseDoWhile() throws Exception {

        if (handler.matchAndRemove(TokenType.DO).isPresent()) {

            BlockNode block = parseBlock();
            if (handler.matchAndRemove(TokenType.WHILE).isPresent()) {

                if (handler.matchAndRemove(TokenType.PARENTHESISSTART).isPresent()) {

                    // Parses the operation inside the parentheses
                    Optional<Node> expression = parseOperation();
                    if (expression.isPresent()) {

                        if (handler.matchAndRemove(TokenType.PARENTHESISEND).isPresent()) {
                            return Optional.of(new DoWhileNode(expression.get(), block));
                        }

                        throw new Exception("Syntax error: missing end parenthesis on do-while statement");
                    }
                    throw new Exception("Syntax error: condition on do-while statement");
                }
                throw new Exception("Syntax error: missing start parenthesis on do-while statement");
            }
            throw new Exception("Syntax error: missing while statement on do-while");
        }

        return Optional.empty();
    }

    // Parses the return key word
    private Optional<Node> parseReturn() throws Exception {

        if (handler.matchAndRemove(TokenType.RETURN).isPresent()) {
            return Optional.of(new ReturnNode(parseOperation()));
        }

        return Optional.empty();
    }

    // Parses a function call (e.g doSomething(a,b)) and saves its name token and
    // returns a new function call node
    private Optional<Node> parseFunctionCall() throws Exception {

        if (handler.matchAndRemove(TokenType.GETLINE).isPresent()) {
            return Optional.of(new FunctionCallNode("getline", Optional.empty()));
        }

        else if (handler.matchAndRemove(TokenType.PRINT).isPresent()) {
            if (handler.peek(0).get().getType().equals(TokenType.PARENTHESISSTART))
                return Optional.of(new FunctionCallNode("print", parseParameters()));
            return Optional.of(new FunctionCallNode("print", parseCommas()));
        }

        else if (handler.matchAndRemove(TokenType.PRINTF).isPresent()) {
            if (handler.peek(0).get().getType().equals(TokenType.PARENTHESISSTART))
                return Optional.of(new FunctionCallNode("printf", parseParameters()));
            return Optional.of(new FunctionCallNode("printf", parseCommas()));
        }

        else if (handler.matchAndRemove(TokenType.EXIT).isPresent()) {
            return Optional.of(new FunctionCallNode("exit", parseOperation()));
        }

        else if (handler.matchAndRemove(TokenType.NEXTFILE).isPresent()) {
            return Optional.of(new FunctionCallNode("nextfile", Optional.empty()));
        }

        else if (handler.matchAndRemove(TokenType.NEXT).isPresent()) {
            return Optional.of(new FunctionCallNode("next", Optional.empty()));
        }

        // Peeks ahead to check if there is a word and a parenthesis; checks if they
        // exist before checking what they are
        else if (handler.peek(0).isPresent() && handler.peek(0).get().getType().equals(TokenType.WORD)
                && handler.peek(1).isPresent() && handler.peek(1).get().getType().equals(TokenType.PARENTHESISSTART)) {
            return Optional.of(
                    new FunctionCallNode(handler.matchAndRemove(TokenType.WORD).get().getValue(), parseParameters()));
        }

        return Optional.empty();
    }

    private LinkedList<Node> parseCommas() throws Exception {

        Optional<Node> expression;
        LinkedList<Node> list = new LinkedList<Node>();

        while ((expression = parseOperation()).isPresent()) {

            list.add(expression.get());

            if (handler.matchAndRemove(TokenType.COMMA).isPresent()) {
                acceptSeperators();
            } else if (handler.matchAndRemove(TokenType.SEPERATOR).isPresent()) {
                return list;
            }

        }

        return list;
    }

    private Optional<Node> parseOperation() throws Exception {
        return parseAssignment();
    }

    // ------------------------------------------------------------------------
    // The following methods follow an order of precedence going from
    // parseAssignment -> parseBottomLevel
    // This allows for the parsing of all types of expressions within an AWK program

    // Parses the left value of an operation - used as a helper in
    // parseBottomLevel()
    private Optional<Node> parseLValue() throws Exception {

        // if there is a dollar sign, remove it and return an operation containing the
        // result of parseBottomLevel() and the dollar sign (e.g $777)
        if (handler.matchAndRemove(TokenType.DOLLAR).isPresent()) {
            return Optional.of(new OperationNode(parseBottomLevel().get(), OperationNode.Operations.DOLLAR));
        }

        Optional<Token> name = handler.matchAndRemove(TokenType.WORD);

        // if there is a name for a variable
        if (name.isPresent()) {

            // if there is a start bracket for an array, create a new variable reference and
            // pass any possible operations within the brackets (e.g array[2+2])
            if (handler.matchAndRemove(TokenType.BRACKETSTART).isPresent()) {
                Optional<Node> array = Optional.of(new VariableReferenceNode(name.get().getValue(), parseAssignment()));

                // if there is a missing bracket, throw exception
                if (!handler.matchAndRemove(TokenType.BRACKETEND).isPresent()) {
                    throw new Exception("Syntax error: no end bracket on array");
                }

                return array;
            }

            // if this is not an array, simply return a new variable reference with its name
            return Optional.of(new VariableReferenceNode(name.get().getValue(), Optional.empty()));
        }

        return Optional.empty();
    }

    // Parses the lowest level of the AST, meaning the most basic operations,
    // such as "++a", "5", or "(!x)" to build the foundation for the AST
    private Optional<Node> parseBottomLevel() throws Exception {
        Optional<Token> value;
        Optional<Node> retVal;

        // if there is a string literal, return a constant node with its value
        if ((value = handler.matchAndRemove(TokenType.STRINGLITERAL)).isPresent()) {
            return Optional.of(new ConstantNode(value.get().getValue()));
        }

        // if there is a number, return a constant node with its value
        else if ((value = handler.matchAndRemove(TokenType.NUMBER)).isPresent()) {
            return Optional.of(new ConstantNode(value.get().getValue()));
        }

        // if there is a pattern, return pattern node with its value
        else if ((value = handler.matchAndRemove(TokenType.PATTERN)).isPresent()) {
            return Optional.of(new PatternNode(value.get().getValue()));
        }

        // if there is a parenthesis parse the operation inside of it and return
        // the value unless there is a missing parenthesis, then throw exception
        else if (handler.matchAndRemove(TokenType.PARENTHESISSTART).isPresent()) {
            retVal = parseAssignment();
            if (!handler.matchAndRemove(TokenType.PARENTHESISEND).isPresent())
                throw new Exception("Syntax error: missing end parenthesis on operation");
            return retVal;
        }

        // The following conditions follow the same format:
        // if there is a TOKEN then return a new operation node with the result of the
        // next operation and the corresponding operation to TOKEN (e.g !x)
        else if (handler.matchAndRemove(TokenType.NOT).isPresent()) {
            return Optional.of(new OperationNode(parseAssignment().get(), OperationNode.Operations.NOT));
        }

        else if (handler.matchAndRemove(TokenType.MINUS).isPresent()) {
            return Optional.of(new OperationNode(parseAssignment().get(), OperationNode.Operations.UNARYNEG));
        }

        else if (handler.matchAndRemove(TokenType.PLUS).isPresent()) {
            return Optional.of(new OperationNode(parseAssignment().get(), OperationNode.Operations.UNARYPOS));
        }

        else if (handler.matchAndRemove(TokenType.INCREMENT).isPresent()) {
            return Optional.of(new OperationNode(parseAssignment().get(), OperationNode.Operations.PREINC));
        }

        else if (handler.matchAndRemove(TokenType.DECREMENT).isPresent()) {
            return Optional.of(new OperationNode(parseAssignment().get(), OperationNode.Operations.PREDEC));
        }

        // Peeks ahead for post increment, checks if the token exists before checking
        // what it is
        else if (handler.peek(0).isPresent() && handler.peek(0).get().getType().equals(TokenType.WORD)
                && handler.peek(1).isPresent()
                && handler.peek(1).get().getType().equals(TokenType.INCREMENT)) {
            Optional<Node> lvalue = parseLValue();
            handler.matchAndRemove(TokenType.INCREMENT);
            return Optional.of(new OperationNode(lvalue.get(),
                    OperationNode.Operations.POSTINC));
        }

        // Peeks ahead for post increment, checks if the token exists before checking
        // what it is
        else if (handler.peek(0).isPresent() && handler.peek(0).get().getType().equals(TokenType.WORD)
                && handler.peek(1).isPresent()
                && handler.peek(1).get().getType().equals(TokenType.DECREMENT)) {
            Optional<Node> lvalue = parseLValue();
            handler.matchAndRemove(TokenType.DECREMENT);
            return Optional.of(new OperationNode(lvalue.get(),
                    OperationNode.Operations.POSTDEC));
        }

        // Parses for a function call before returning LValue
        Optional<Node> call = parseFunctionCall();
        if (call.isPresent()) {
            return call;
        }

        // if none of the above were true, parse for a left value
        return parseLValue();
    }

    // Parses exponents with right associativity using recursion
    // Returns a new operation node if syntax is correct, otherwise either throws
    // a new exception or returns the left side of the expression if there is no
    // exponent sign
    private Optional<Node> parseExponent() throws Exception {

        Optional<Node> left = parseBottomLevel();

        if (handler.matchAndRemove(TokenType.EXPONENT).isPresent()) {
            Optional<Node> right = parseExponent();
            if (right.isPresent()) {
                return Optional.of(new OperationNode(left.get(), OperationNode.Operations.EXPONENT, right));
            }
            throw new Exception("Syntax error: missing expression after exponent");
        }
        return left;

    }

    // Parses terms, which can have * or / or % operators; left associative
    // Returns a new operation node with one of the listed operations or
    // the left expression. Throws exception if right expression is missing.
    private Optional<Node> parseTerm() throws Exception {
        Optional<Node> left;
        Optional<Node> right;
        Optional<Token> operation;
        OperationNode.Operations opType;

        left = parseExponent();
        while (left.isPresent()) {
            operation = handler.matchAndRemove(TokenType.TIMES);
            opType = OperationNode.Operations.MULTIPLY;

            if (!operation.isPresent()) {
                operation = handler.matchAndRemove(TokenType.DIVIDE);
                opType = OperationNode.Operations.DIVIDE;
            }
            if (!operation.isPresent()) {
                operation = handler.matchAndRemove(TokenType.MODULO);
                opType = OperationNode.Operations.MODULO;
            }
            if (!operation.isPresent()) {
                return left;
            }

            right = parseExponent();

            if (!right.isPresent()) {
                throw new Exception("Syntax error: missing right factor in term");
            }

            left = Optional.of(new OperationNode(left.get(), opType, right));
        }
        return left;
    }

    // Parses expressions, which can have + or - operators; left associative.
    // Returns a new operation node with one of the listed operations or the
    // left expression. Throws exception if right expression is missing.
    private Optional<Node> parseExpression() throws Exception {
        Optional<Node> left;
        Optional<Node> right;
        Optional<Token> operation;
        OperationNode.Operations opType;

        left = parseTerm();
        while (left.isPresent()) {

            operation = handler.matchAndRemove(TokenType.PLUS);
            opType = OperationNode.Operations.ADD;

            if (!operation.isPresent()) {
                operation = handler.matchAndRemove(TokenType.MINUS);
                opType = OperationNode.Operations.SUBTRACT;
            }
            if (!operation.isPresent()) {
                return left;
            }

            right = parseTerm();

            if (!right.isPresent()) {
                throw new Exception("Syntax error: missing right term in expression");
            }

            left = Optional.of(new OperationNode(left.get(), opType, right));

        }
        return left;
    }

    // Parses concatenation of strings as long as their is at least one
    // string literal present in the concatenation.
    // Returns a new operation node of the left and right expression were
    // concatenating or returns the left expression.
    private Optional<Node> parseConcat() throws Exception {
        Optional<Node> left = parseExpression();

        while (left.isPresent() && handler.moreTokens()) {
            Optional<Node> right = parseExpression();
            if (right.isPresent()) {
                left = Optional.of(new OperationNode(left.get(), OperationNode.Operations.CONCATENATION, right));
            } else
                return left;
        }

        return left;
    }

    // Parses comparisons (e.g <=, >, ==, etc.)
    // Returns a new operation node if it correctly parses the left and right
    // expressions, containing the comparator
    // Throws new exception if either the left or right expression aren't present.
    private Optional<Node> parseCompare() throws Exception {

        Optional<Node> left = parseConcat();
        Optional<Node> right;

        // if (!left.isPresent()) {
        // throw new Exception("Syntax error: left expression could not be parsed");
        // }

        Optional<Token> comparator = handler.matchAndRemove(TokenType.LESSTHAN);
        if (comparator.isPresent()) {
            right = parseConcat();
            if (right.isPresent()) {
                return Optional.of(new OperationNode(left.get(), OperationNode.Operations.LT, right));
            }
            throw new Exception("Syntax error: right expression could not be parsed");
        }
        comparator = handler.matchAndRemove(TokenType.LESSEQUAL);
        if (comparator.isPresent()) {
            right = parseConcat();
            if (right.isPresent()) {
                return Optional.of(new OperationNode(left.get(), OperationNode.Operations.LE, right));
            }
            throw new Exception("Syntax error: right expression could not be parsed");
        }
        comparator = handler.matchAndRemove(TokenType.NOTEQUAL);
        if (comparator.isPresent()) {
            right = parseConcat();
            if (right.isPresent()) {
                return Optional.of(new OperationNode(left.get(), OperationNode.Operations.NE, right));
            }
            throw new Exception("Syntax error: right expression could not be parsed");
        }
        comparator = handler.matchAndRemove(TokenType.EQUALEQUAL);
        if (comparator.isPresent()) {
            right = parseConcat();
            if (right.isPresent()) {
                return Optional.of(new OperationNode(left.get(), OperationNode.Operations.EQ, right));
            }
            throw new Exception("Syntax error: right expression could not be parsed");
        }
        comparator = handler.matchAndRemove(TokenType.GREATERTHAN);
        if (comparator.isPresent()) {
            right = parseConcat();
            if (right.isPresent()) {
                return Optional.of(new OperationNode(left.get(), OperationNode.Operations.GT, right));
            }
            throw new Exception("Syntax error: right expression could not be parsed");
        }
        comparator = handler.matchAndRemove(TokenType.GREATEREQUAL);
        if (comparator.isPresent()) {
            right = parseConcat();
            if (right.isPresent()) {
                return Optional.of(new OperationNode(left.get(), OperationNode.Operations.GE, right));
            }
            throw new Exception("Syntax error: right expression could not be parsed");
        }

        return left;
    }

    // Parses reg ex matches (e.g ~ and !~) without associativity
    // Returns a new operation node if it matches with the ~ or !~ tokens
    // Throws new exception if the right expression is missing
    private Optional<Node> parseMatch() throws Exception {

        Optional<Node> left = parseCompare();

        if (left.isPresent()) {
            if (handler.matchAndRemove(TokenType.MATCH).isPresent()) {
                Optional<Node> right = parseCompare();
                if (right.isPresent()) {
                    left = Optional.of(new OperationNode(left.get(), OperationNode.Operations.MATCH, right));
                } else
                    throw new Exception("Syntax error: missing right expression on match");
            }
            if (handler.matchAndRemove(TokenType.NOTMATCH).isPresent()) {
                Optional<Node> right = parseCompare();
                if (right.isPresent()) {
                    left = Optional.of(new OperationNode(left.get(), OperationNode.Operations.NOTMATCH, right));
                } else
                    throw new Exception("Syntax error: missing right expression on match");
            }
        }

        return left;
    }

    // Parses the AWK syntax for arrays "index in array", which
    // returns true or false if the index exists
    // Returns new operation node if the syntax matches, with operation IN
    // Throws new exception if the array name is missing after "in"
    private Optional<Node> parseArray() throws Exception {

        Optional<Node> left = parseMatch();

        if (left.isPresent()) {
            while (handler.matchAndRemove(TokenType.IN).isPresent()) {
                Optional<Node> right = parseBottomLevel();
                if (right.isPresent()) {
                    left = Optional.of(new OperationNode(left.get(), OperationNode.Operations.IN, right));
                } else
                    throw new Exception("Syntax error: missing array name");
            }
        }
        return left;
    }

    // Parses the && boolean operator with left associativity
    // Returns new operation node if it matches the && token
    // Throws new exception if there is no right expression after the &&
    private Optional<Node> parseAND() throws Exception {
        Optional<Node> left = parseArray();

        if (left.isPresent()) {
            while (handler.matchAndRemove(TokenType.AND).isPresent()) {
                Optional<Node> right = parseArray();
                if (right.isPresent()) {
                    left = Optional.of(new OperationNode(left.get(), OperationNode.Operations.AND, right));
                } else
                    throw new Exception("Syntax error: no right expression on booolean AND");
            }
        }

        return left;
    }

    // Parses the || boolean operator with left associativity
    // Returns new operation node if it matches the || token
    // Throws new exception if there is no right expression after the ||
    private Optional<Node> parseOR() throws Exception {
        Optional<Node> left = parseAND();

        if (left.isPresent()) {

            while (handler.matchAndRemove(TokenType.OROR).isPresent()) {
                Optional<Node> right = parseAND();
                if (right.isPresent()) {
                    left = Optional.of(new OperationNode(left.get(), OperationNode.Operations.OR, right));
                } else
                    throw new Exception("Syntax error: no right expression on booolean OR");
            }

        }
        return left;
    }

    // Parses the ternary operator "expr1 ? expr2 : expr3"
    // Returns new ternary node if expression and syntax match
    // Throws new exception if there is either: missing expr1, missing colon,
    // or missing expr3
    private Optional<Node> parseTernary() throws Exception {
        Optional<Node> left = parseOR();

        if (handler.matchAndRemove(TokenType.TERNARY).isPresent()) {
            Optional<Node> right1 = parseTernary();
            if (right1.isPresent()) {
                if (handler.matchAndRemove(TokenType.COLON).isPresent()) {
                    Optional<Node> right2 = parseTernary();
                    if (right2.isPresent()) {
                        return Optional.of(new TernaryNode(left.get(), right1.get(), right2.get()));
                    }
                    throw new Exception("Syntax error: no false expression on ternary");
                }
                throw new Exception("Syntax error: no colon in ternary expression");
            }
            throw new Exception("Syntax error: no true expression on ternary");
        }

        return left;
    }

    // Parse the assignment operators (e.g +=, =, etc.) with right associativity.
    // Recursively called in order to allow for right associativity.
    // Returns new assignment node with the target and a new operation node with
    // the actual operation being performed
    // Throws new exception if its missing a right expression
    private Optional<Node> parseAssignment() throws Exception {
        Optional<Node> left = parseTernary();

        if (left.isPresent()) {

            if (handler.matchAndRemove(TokenType.EXPONENTEQUAL).isPresent()) {

                Optional<Node> right = parseAssignment();
                if (right.isPresent()) {
                    return Optional.of(new AssignmentNode(left.get(), Optional
                            .of(new OperationNode(left.get(), OperationNode.Operations.EXPONENT, right))));
                }
                throw new Exception("Syntax error: missing right expression on assignment");

            }

            else if (handler.matchAndRemove(TokenType.MODEQUAL).isPresent()) {

                Optional<Node> right = parseAssignment();
                if (right.isPresent()) {
                    return Optional.of(new AssignmentNode(left.get(), Optional
                            .of(new OperationNode(left.get(), OperationNode.Operations.MODULO, right))));
                }
                throw new Exception("Syntax error: missing right expression on assignment");

            }

            else if (handler.matchAndRemove(TokenType.TIMESEQUAL).isPresent()) {

                Optional<Node> right = parseAssignment();
                if (right.isPresent()) {
                    return Optional.of(new AssignmentNode(left.get(), Optional
                            .of(new OperationNode(left.get(), OperationNode.Operations.MULTIPLY, right))));
                }
                throw new Exception("Syntax error: missing right expression on assignment");

            }

            else if (handler.matchAndRemove(TokenType.DIVIDEEQUAL).isPresent()) {

                Optional<Node> right = parseAssignment();
                if (right.isPresent()) {
                    return Optional.of(new AssignmentNode(left.get(), Optional
                            .of(new OperationNode(left.get(), OperationNode.Operations.DIVIDE, right))));
                }
                throw new Exception("Syntax error: missing right expression on assignment");

            }

            else if (handler.matchAndRemove(TokenType.PLUSEQUAL).isPresent()) {

                Optional<Node> right = parseAssignment();
                if (right.isPresent()) {
                    return Optional.of(new AssignmentNode(left.get(), Optional
                            .of(new OperationNode(left.get(), OperationNode.Operations.ADD, right))));
                }
                throw new Exception("Syntax error: missing right expression on assignment");

            }

            else if (handler.matchAndRemove(TokenType.MINUSEQUAL).isPresent()) {

                Optional<Node> right = parseAssignment();
                if (right.isPresent()) {
                    return Optional.of(new AssignmentNode(left.get(), Optional
                            .of(new OperationNode(left.get(), OperationNode.Operations.SUBTRACT, right))));
                }
                throw new Exception("Syntax error: missing right expression on assignment");

            }

            else if (handler.matchAndRemove(TokenType.EQUAL).isPresent()) {

                Optional<Node> right = parseAssignment();
                if (right.isPresent()) {
                    return Optional.of(new AssignmentNode(left.get(), right.get()));
                }
                throw new Exception("Syntax error: missing right expression on assignment");

            }
        }

        return left;
    }
}