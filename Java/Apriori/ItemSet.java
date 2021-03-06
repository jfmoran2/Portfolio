import java.util.ArrayList;

public class ItemSet {
    private Integer support;
    private ArrayList<String> items;

    public ItemSet() {
        this.items = new ArrayList<>();
        this.support = 0;
    }

    public void addItem(String item) {
        this.items.add(item);
    }

    public void addItems(ArrayList<String> items) {
        this.items.addAll(items);
    }

    public void setSupport(Integer support) {
        this.support = support;
    }

    public int getSupport() {
        return (this.support);
    }

    public ArrayList<String> getItems() {
        return (this.items);
    }
}

