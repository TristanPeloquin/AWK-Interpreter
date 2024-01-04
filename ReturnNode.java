import java.util.Optional;

public class ReturnNode extends StatementNode {

    private Optional<Node> retVal;

    public ReturnNode(Optional<Node> retVal) {
        this.retVal = retVal;
    }

    public Node getValue(){
        if(retVal.isPresent()){
            return retVal.get();
        }
        return null;
    }

    public String toString() {
        if (retVal.isPresent())
            return "return " + retVal.get();
        return "return";
    }

}
