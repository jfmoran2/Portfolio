import java.util.*;

public class DataRow {
    Integer classLabel;
    ArrayList<Integer> columns;

    public DataRow() {
        this.classLabel = -1;
        columns = new ArrayList<>();
    }

    public void setClassLabel(Integer label) {
        this.classLabel = label;
    }

    public Integer getClassLabel() {
        return (this.classLabel);
    }

    public void addColumnValue(Integer attributeValue) {
        this.columns.add(attributeValue);
    }

    public Integer getColumnValue(Integer index) {
        return (this.columns.get(index));
    }

    public ArrayList<Integer> getColumns() {
        return (this.columns);
    }
}
