import java.util.ArrayList;
import java.util.Iterator;

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

    public String getItemsAsString(String separator)
    {
        StringBuilder sb = new StringBuilder();
        Integer i = 0;
        for (String s : this.items)
        {
            sb.append(s);
            if (i < (this.items.size() - 1)) {
                sb.append(separator);
            }
            i++;
        }

        return sb.toString();
    }


}

