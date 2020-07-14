import java.util.ArrayList;

public class CategorySet {
    private ArrayList<String> categories;
    private int support;

    public CategorySet() {
        this.categories = new ArrayList<>();
        support = 0;
    }

    public void add(String field) {
        this.categories.add(field);
    }

    public String getCategory(int i) {
        return this.categories.get(i);
    }

    public int getSupport() {
        return this.support;
    }

    public int size() {
        return this.categories.size();
    }

    public void incrementSupport() {
        this.support++;
    }

}
