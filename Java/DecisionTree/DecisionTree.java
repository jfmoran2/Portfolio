import java.io.*;
import java.util.*;

public class DecisionTree {
    //MAX_DEPTH = 10 and MIN_LABELS = 6 look good values
    private static Integer MAX_DEPTH = 10;
    private static Integer MIN_LABELS = 5;

    private static String trainingFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//DecisionTree//training.txt";
    private static String testingFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//DecisionTree//testing.txt";

    private static String outputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//DecisionTree//result.txt";

    private ArrayList<Integer> uniqueClassLabels;
    private Integer[] labelCount;
    private ArrayList<DataRow> trainingData;
    private ArrayList<DataRow> testingData;
    private ArrayList<Attribute> attributes;
    private Double setImpurity;

    private Integer TEMPSUM = 0;
    private Integer TEMPLEAF = 0;
    private Integer LEAFCOUNT = 0;

    public DecisionTree() {
        this.trainingData = new ArrayList<>();
        this.testingData = new ArrayList<>();
        this.uniqueClassLabels = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    private void buildAttributeStructure() {
        for (int i=0; i < trainingData.get(0).getColumns().size(); i++) {
            Attribute a = new Attribute();
            attributes.add(a);
        }
    }

    private Double calculateGINI(int[] valueCount, int[][] labelCount) {
        double GINI=0.0;
        int numberOfRows = 0;

        // index from 1 since first element not used
        for (int i=1; i < valueCount.length; i++) {
            numberOfRows += valueCount[i];
        }

       // double[] giniSub = new double[valueCount.length];

        for (int i=1; i < valueCount.length; i++) {
            double summation = 0.0;
            for (int j=1; j < labelCount[i].length; j++) {
                double labelFactor = Math.pow((double) labelCount[i][j] / (double)valueCount[i], 2);
                summation +=  labelFactor;
            }
            double giniSub = 1.0 - summation;

            GINI += ((double) valueCount[i]/(double) numberOfRows) * giniSub;
        }

        return GINI;
    }

    private ArrayList<Integer> getNewDataIndices(ArrayList<Integer> origIndices, Integer
            attributeToSplitOn, Integer valueToSplitOn) {

        ArrayList<Integer> newIndices = new ArrayList<>();

        for (int i = 0; i < origIndices.size(); i++) {
            Integer value = trainingData.get(origIndices.get(i)).getColumnValue(attributeToSplitOn);
            if (value == valueToSplitOn) {
                newIndices.add(origIndices.get(i));
            }
        }

        return newIndices;
    }

    private ArrayList<TreeNode> splitDataOnAttribute(TreeNode nodeToSplit, Integer attributeToSplitOn) {
        ArrayList<TreeNode> children = new ArrayList<>();

        ArrayList<Integer> splitOnValues = attributes.get(attributeToSplitOn).getUniqueAttributes();

        for (int i = 0; i < splitOnValues.size(); i++) {
            TreeNode childNode = new TreeNode();

            //------------------------------------------------------------------------------
            // first set attributesUsed for each child node, initialize with current list of
            // used attributes
            //------------------------------------------------------------------------------
            ArrayList<Integer> attributesUsed = new ArrayList<>(nodeToSplit.getAttributesUsed());
            attributesUsed.add(attributeToSplitOn);
            childNode.setAttributesUsed(attributesUsed);

            //--------------------------------------------------------------------------------------------
            // next create a new set of data indices that contain this value for the attribute to split on
            //--------------------------------------------------------------------------------------------
            ArrayList<Integer> newDataIndices = getNewDataIndices(nodeToSplit.getDataRowIndices(),
                        attributeToSplitOn, splitOnValues.get(i));

            childNode.setDataRowIndices(newDataIndices);

            // set the attribute and value this node was split on
            childNode.setAttributeSplitOn(attributeToSplitOn);
            childNode.setValueSplitOn(splitOnValues.get(i));

            // add the childNode to the children ArrayList
            children.add(childNode);
        }

        return children;
    }

    private void splitTree(TreeNode nodeToSplit, Integer maxDepth, Integer currentDepth, Integer minLabels) {

        // Integer numberOfLabels = nodeToSplit.getDataRowIndices().size();

        Integer attributeToSplitOn = calculateBestSplit(nodeToSplit.getDataRowIndices(),
                nodeToSplit.getAttributesUsed());

        ArrayList<TreeNode> children = splitDataOnAttribute(nodeToSplit, attributeToSplitOn);

        // find smallest number of labels
        Integer smallestNumberOfRows = trainingData.size();
        for (int i = 0; i < children.size(); i++) {
            Integer temp = children.get(i).getDataRowIndices().size();
            if (temp < smallestNumberOfRows) {
                smallestNumberOfRows = temp;
            }
        }

        if (currentDepth < maxDepth && smallestNumberOfRows > minLabels) {

            //Integer attributeToSplitOn = calculateBestSplit(nodeToSplit.getDataRowIndices(),
            //        nodeToSplit.getAttributesUsed());

            // ArrayList<TreeNode> children = splitDataOnAttribute(nodeToSplit, attributeToSplitOn);

            nodeToSplit.setChildren(children);

            for (int i = 0; i < children.size(); i++) {
                splitTree(children.get(i), maxDepth, currentDepth + 1, minLabels);
            }
        } else {


            // predict label and log it here:
            ArrayList<Integer> labelCandidates = new ArrayList<>();

            for (Integer index: nodeToSplit.getDataRowIndices()) {
                labelCandidates.add(trainingData.get(index).getClassLabel());
            }

            Integer predictedLabel = findMostCommon(labelCandidates);
            nodeToSplit.setPredictedLabel(predictedLabel);

            nodeToSplit.setIsLeaf(true);

            TEMPSUM += smallestNumberOfRows;
            TEMPLEAF += 1;
            System.out.println("Done: nLabels = " + smallestNumberOfRows + " SUM:" + TEMPSUM +
                     " candidates: " + labelCandidates + "label: " + predictedLabel + " Leaf count:" + TEMPLEAF);
        }
    }

    public TreeNode buildTree(Integer maxDepth, Integer minLabels) {
        ArrayList<Integer> dataRowIndices = new ArrayList<>();
        ArrayList<Integer> attributesUsed = new ArrayList<>();

        TreeNode rootNode = new TreeNode();

        for (int i=0; i < trainingData.size(); i++) {
            dataRowIndices.add(i);
        }

        // in beginning all data rows and no attributes used
        rootNode.setAttributesUsed(attributesUsed);
        rootNode.setDataRowIndices(dataRowIndices);
        Integer depth = 1;

        splitTree(rootNode, maxDepth, depth, minLabels);

        return (rootNode);
    }

    private Integer calculateBestSplit(ArrayList<Integer> dataRowIndices, ArrayList<Integer> attributesUsed) {
        Double lowestGINI = 1.0; // Max of GINI is 1.0
        Integer bestGINIattributeIndex = -1;

        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            if (!attributesUsed.contains(attributeIndex)) {
                ArrayList<Integer> uniqueValues = attributes.get(attributeIndex).getUniqueAttributes();
                int[] valueCount = new int[uniqueValues.size() + 1]; // USE COLLECTIONS MAX
                int[][] labelCount = new int[uniqueValues.size() + 1][uniqueClassLabels.size() + 1];


                for (int dataRowIndex = 0; dataRowIndex < dataRowIndices.size(); dataRowIndex++) {

                    int value = trainingData.get(dataRowIndices.get(dataRowIndex)).getColumnValue(attributeIndex);
                    int label = trainingData.get(dataRowIndices.get(dataRowIndex)).getClassLabel();
                    valueCount[value] += 1;
                    labelCount[value][label] += 1;
                }

                Double GINI = calculateGINI(valueCount, labelCount);
                if (GINI < lowestGINI) {
                    bestGINIattributeIndex = attributeIndex;

                    lowestGINI = GINI;
                }
            }
        }

        return bestGINIattributeIndex;
    }

    private void assignAttributes() {

        buildAttributeStructure();
        Integer index = 0;

        for (DataRow dr: trainingData) {
           ArrayList<Integer> columns = dr.getColumns();
           for (Integer val: columns) {
               attributes.get(index).addAttribute(val.intValue());
               index++;
           }
           index = 0;
        }
    }

    private void printUniqueAttributes() {
        for (Attribute a: attributes) {
            ArrayList<Integer> uniques = a.getUniqueAttributes();

            System.out.println(uniques);
        }
    }

    private void getUniqueClassLabels() {
        ArrayList<Integer> originalLabels = new ArrayList<>();
        for (DataRow dr: trainingData) {
            Integer classLabel = dr.getClassLabel();
            originalLabels.add(classLabel);
        }

        Set<Integer> labelSet = new HashSet<>(originalLabels);
        uniqueClassLabels = new ArrayList(labelSet);
    }

    private void getLabelCount() {
        //--------------------------------------------------------------------------
        // want to index based on class label, we know it's an integer, so just find
        // max value, add 1 to it, then it is generic for any data
        //--------------------------------------------------------------------------
        Integer maxIndex = Collections.max(uniqueClassLabels) + 1;

        // by Java standard, all new int array elements = 0
        labelCount = new Integer[maxIndex];
        Arrays.fill(labelCount,0);

        for (DataRow dr: trainingData) {
            Integer classLabel = dr.getClassLabel();
            labelCount[classLabel]++;
        }
    }

    private Integer findMostCommon(List<Integer> list) {

        if (list == null || list.isEmpty())
            return null;

        Map<Integer, Integer> counterMap = new HashMap<Integer, Integer>();
        Integer maxValue = 0;

        Integer mostFrequentValue = null;

        for(Integer valueAsKey : list) {
            Integer counter = counterMap.get(valueAsKey);
            counterMap.put(valueAsKey, counter == null ? 1 : counter + 1);
            counter = counterMap.get(valueAsKey);
            if (counter > maxValue) {
                maxValue = counter;
                mostFrequentValue = valueAsKey;
            }
        }
        return mostFrequentValue;
    }

    private void calculateSetImpurity() {

        Double impuritySum = 0.0;
        for (Integer count: labelCount) {
            impuritySum += Math.pow((Double.valueOf(count) / Double.valueOf(trainingData.size())),2);
        }

        setImpurity = 1 - impuritySum;
    }

    private void writeOutput(ArrayList<Integer> predictedLabels) {

        try {
            File file = new File(outputFile);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (Integer label : predictedLabels) {
                System.out.println(label);
                writer.write(label.toString());
                writer.newLine();
            }

            writer.close();

        }  catch (Exception exception) {
        exception.printStackTrace();
    }
    }

    private void readInputFile(boolean training) {

        try {
            String inputFile;
            if (training) {
                inputFile = trainingFile;
            } else {
                inputFile = testingFile;
            }

            File file = new File(inputFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] dataLine = line.split(" ");

                DataRow d = new DataRow();

                Integer startIndex = 0;
                if (training) {
                    Integer classLabel = Integer.parseInt(dataLine[0]);
                    d.setClassLabel(classLabel);
                    startIndex = 1;
                }

                for (int i=startIndex; i < dataLine.length; i++) {
                    String[] element = dataLine[i].split(":");
                    Integer value = Integer.parseInt(element[1]);
                    d.addColumnValue(value);
                }

                if (training) {
                    trainingData.add(d);
                } else {
                    testingData.add(d);
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public TreeNode predictLabel(TreeNode node, DataRow dr) {

        TreeNode nodeToFollow = new TreeNode();

        if (node.getIsLeaf()) {
           // LEAFCOUNT = LEAFCOUNT + 1;
           // System.out.println("Found leaf #" + LEAFCOUNT);

            return node;
        }
        else
        {
            for (TreeNode child: node.getChildren()) {
                Integer attributeToFollow = child.getAttributeSplitOn();
                Integer valueToFollow = child.getValueSplitOn();

                if (dr.getColumnValue(attributeToFollow) == valueToFollow)
                {
                    nodeToFollow = child;
                }
            }

            return predictLabel(nodeToFollow, dr);
        }
    }

    public ArrayList<Integer> getPredictions(TreeNode root) {
        TreeNode foundNode = new TreeNode();
        ArrayList<Integer> foundLabels = new ArrayList<>();

        for (DataRow dr: testingData) {
            foundNode = predictLabel(root, dr);
            foundLabels.add(foundNode.getPredictedLabel());
        }

        return foundLabels;
    }

    public void traverseTree(TreeNode t) {

        if (t.getIsLeaf()) {
            LEAFCOUNT = LEAFCOUNT + 1;
            System.out.println("Found leaf #" + LEAFCOUNT);
            return;
        }
        else
        {
            for (TreeNode child: t.getChildren()) {
                traverseTree(child);
            }
        }
    }

    public static void main(String[] args) {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT. Your class should be named Solution. */
        DecisionTree tree = new DecisionTree();

        tree.readInputFile(true);
        tree.readInputFile(false);
        tree.getUniqueClassLabels();
        tree.getLabelCount();
        tree.calculateSetImpurity();

        tree.assignAttributes();

        tree.printUniqueAttributes();

        TreeNode rootNode = tree.buildTree(MAX_DEPTH, MIN_LABELS);

        ArrayList<Integer> predictions = tree.getPredictions(rootNode);

        tree.writeOutput(predictions);

    }

}
