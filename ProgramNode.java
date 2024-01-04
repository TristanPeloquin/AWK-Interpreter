import java.util.LinkedList;

//The primary node of the AST, this holds all the blocks in an AWK program,
//including BEGIN, END, functions, and other conditional blocks
public class ProgramNode extends Node {

    protected LinkedList<BlockNode> beginBlocks;
    protected LinkedList<BlockNode> blocks;
    protected LinkedList<BlockNode> endBlocks;
    protected LinkedList<FunctionDefinitionNode> funcDefNodes;

    public ProgramNode() {
        beginBlocks = new LinkedList<BlockNode>();
        blocks = new LinkedList<BlockNode>();
        endBlocks = new LinkedList<BlockNode>();
        funcDefNodes = new LinkedList<FunctionDefinitionNode>();
    }

    public String toString() {
        return "Program: \n\nBegin Blocks: " + beginBlocks + "\n\nBlocks: " + blocks + "\n\nEnd Blocks: " + endBlocks
                + "\n\nFunctions: " + funcDefNodes;
    }

}
