import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Attribute {
    ArrayList<Integer> attributes;

    public Attribute() {
        attributes = new ArrayList<>();
    }

    public void addAttribute(Integer attributeValue) {
        this.attributes.add(attributeValue);
    }

    public Integer getAttribute(Integer index) {
        return (this.attributes.get(index));
    }

    public ArrayList<Integer> getAttributes() {
        return (this.attributes);
    }

    public ArrayList<Integer> getUniqueAttributes() {
        Set<Integer> attributeSet = new HashSet<>(this.attributes);
        ArrayList<Integer> uniqueVals = new ArrayList(attributeSet);
        return (uniqueVals);
    }
}
