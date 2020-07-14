import java.util.ArrayList;

public class TreeNode {

    private ArrayList<Integer> attributesUsed;
    private ArrayList<Integer> dataRowIndices;

    private ArrayList<TreeNode> children;

    private Integer attributeSplitOn;
    private Integer valueSplitOn;


    private Integer predictedLabel;



    private Boolean isLeaf;


    public TreeNode() {
        this.children = new ArrayList<>();
        this.attributesUsed = new ArrayList<>();
        this.dataRowIndices = new ArrayList<>();
        this.valueSplitOn = -1;
        this.attributeSplitOn = -1;
        this.predictedLabel = -1;
        this.isLeaf = false;

    }

    public void addChild(TreeNode node) {
        children.add(node);
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<TreeNode> children) {
        this.children = children;
    }

    public ArrayList<Integer> getAttributesUsed() {
        return attributesUsed;
    }

    public void setAttributesUsed(ArrayList<Integer> attributesUsed) {
        this.attributesUsed = attributesUsed;
    }

    public ArrayList<Integer> getDataRowIndices() {
        return dataRowIndices;
    }

    public void setDataRowIndices(ArrayList<Integer> dataRowIndices) {
        this.dataRowIndices = dataRowIndices;
    }

    public Integer getAttributeSplitOn() {
        return attributeSplitOn;
    }

    public void setAttributeSplitOn(Integer attributeSplitOn) {
        this.attributeSplitOn = attributeSplitOn;
    }

    public Integer getValueSplitOn() {
        return valueSplitOn;
    }

    public void setValueSplitOn(Integer valueSplitOn) {
        this.valueSplitOn = valueSplitOn;
    }

    public Integer getPredictedLabel() {
        return predictedLabel;
    }

    public void setPredictedLabel(Integer predictedLabel) {
        this.predictedLabel = predictedLabel;
    }

    public Boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Boolean leaf) {
        isLeaf = leaf;
    }

}