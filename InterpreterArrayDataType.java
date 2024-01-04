import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType {

    private HashMap<String, InterpreterDataType> values;

    public InterpreterArrayDataType(String... a) {
        values = new HashMap<>();
        int index = 0;
        for (String i : a) {
            values.put(index + "", new InterpreterDataType(i));
            index++;
        }
    }

    public InterpreterArrayDataType(HashMap<String, InterpreterDataType> values) {
        this.values = values;
    }

    public HashMap<String, InterpreterDataType> getArray() {
        return values;
    }

    public void add(String key, InterpreterDataType value) {
        values.put(key, value);
    }

    public void delete(String index) {
        values.remove(index);
    }

    public void delete() {
        values.clear();
    }

}
