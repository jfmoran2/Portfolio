import java.io.*;
import java.util.*;

public class AgglomCluster {
    private static final Integer SINGLE_LINK = 0;
    private static final Integer COMPLETE_LINK = 1;
    private static final Integer AVERAGE_LINK = 2;

    private static final double bigNumber = Math.pow(10, 10);
    private static String inputFile = ".//data//input.txt";
    private static String outputFile = ".//data//clusters.txt";
    private Integer numberOfPoints;
    private Integer numberOfClustersGoal;
    private Integer similariyMeasure;


    public AgglomCluster() {
        this.numberOfPoints = 0;
        this.numberOfClustersGoal = 0;
        this.similariyMeasure = SINGLE_LINK;
    }


    //public CalculateSingleLink()
    //public CalculateCompleteLink()
    //public CalculateAverageLink()

    // The first line of the input will be three space separated integers  N, K, M :
    // N The number of data points (lines) following the first line .
    // K The number of output clusters .
    // M The cluster similarity measure  to be used.  for single link,  for complete link,  for average link.
    private ArrayList<DataPoint> dataPoints = new ArrayList<>();
    private ClusterTree masterClusterTree;
    private DistanceMatrix distanceMatrix;
    private int clusterOutput[];

    private void readDataFromFile(String pathname) {

        try {
            File file = new File(pathname);

            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = reader.readLine();
            String[] intsAsStrings = line.split(" ");
            numberOfPoints = Integer.parseInt(intsAsStrings[0]);
            numberOfClustersGoal = Integer.parseInt(intsAsStrings[1]);
            similariyMeasure = Integer.parseInt(intsAsStrings[2]);

            System.out.println("Number of Points: " + numberOfPoints);
            System.out.println("Number of Clusters: " + numberOfClustersGoal);
            System.out.println("Similarity Measure: " + similariyMeasure);

            while ((line = reader.readLine()) != null) {
                String[] doublesAsStrings = line.split(" ");
                double x = Double.parseDouble(doublesAsStrings[0]);
                double y = Double.parseDouble(doublesAsStrings[1]);
                DataPoint d = new DataPoint(x, y);

                dataPoints.add(d);
                System.out.println("x:" + x + " y:" + y);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readDataFromStdIn() {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String line = reader.readLine();
           // reader.skip(1); // skip newline

            String[] intsAsStrings = line.split(" ");
            numberOfPoints = Integer.parseInt(intsAsStrings[0]);
            numberOfClustersGoal = Integer.parseInt(intsAsStrings[1]);
            similariyMeasure = Integer.parseInt(intsAsStrings[2]);

            while ((line = reader.readLine()) != null) {
            //    reader.skip(1); // skip newline
                String[] doublesAsStrings = line.split(" ");
                double x = Double.parseDouble(doublesAsStrings[0]);
                double y = Double.parseDouble(doublesAsStrings[1]);
                DataPoint d = new DataPoint(x, y);

                dataPoints.add(d);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void writeDataFile(String pathname) {
        try {
            File file = new File(pathname);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            //  for (int k = 0; k < op.size(); k++) {
            //      writer.write(k + " " + op.get(k).getCluster());
            //     writer.newLine();
            // }

            writer.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public class DataPoint {
        private double X = 0;
        private double Y = 0;

        public DataPoint() {
        }

        public DataPoint(double x, double y) {
            this.setX(x);
            this.setY(y);
        }

        public void setX(double x) {
            this.X = x;
        }

        public double getX() {
            return this.X;
        }

        public void setY(double y) {
            this.Y = y;
        }

        public double getY() {
            return this.Y;
        }
    }

    public class DistanceMatrix {
        private double[][] distances;
        ArrayList<DataPoint> points;


        public DistanceMatrix(ArrayList<DataPoint> points) {
            distances = new double[points.size()][points.size()];
            this.points = points;
        }

        public void calculateDistanceMatrix() {
            int size = points.size();

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == j) {
                        distances[i][j] = bigNumber;
                    } else {
                        distances[i][j] = calculateEuclideanDistance(points.get(i), points.get(j));
                    }
                }
            }
        }

        public double getDistance(int point1Index, int point2Index) {
            return distances[point1Index][point2Index];
        }

        private double calculateEuclideanDistance(DataPoint p, DataPoint q) {
            double distance = Math.sqrt(Math.pow((p.getX() - q.getX()), 2) + Math.pow((p.getY() - q.getY()), 2));

            return distance;
        }

        public void printDistanceMatrix() {
            int size = points.size();

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == j) {
                        System.out.println("(" + i + "," + j + "): --");
                    } else {
                        System.out.println("(" + i + "," + j + "): " + distances[i][j]);
                    }
                }
            }
        }

    }


    private class Cluster {
        ArrayList<Integer> pointIndices;

        public Cluster() {
            this.pointIndices = new ArrayList<>();
        }

        public ArrayList<Integer> getPointsIndices() {
            return this.pointIndices;
        }

        public void addPointIndex(Integer p) {
            this.pointIndices.add(p);
        }

    }

    private class ClusterTree {
        private ArrayList<Cluster> clusterTree;
        private ArrayList<DataPoint> xyPoints;

        Integer ClusterGoal = 1;

        public ClusterTree(Integer goal, ArrayList<DataPoint> xyPoints) {
            this.clusterTree = new ArrayList<>();
            this.ClusterGoal = goal;
            this.xyPoints = xyPoints;
        }

        private ArrayList<Cluster> getClusterTree() {
            return this.clusterTree;
        }

        public void addCluster(Cluster cluster) {
            this.clusterTree.add(cluster);
        }

        public void mergeClusters(Integer c1, Integer c2) {

            ArrayList<Cluster> newClusterTree = new ArrayList<>();

            if (c1 < clusterTree.size() && c2 < clusterTree.size() && c1 != c2) {
                // copy all from cluster c1 to cluster c2 and then delete c1

                ArrayList<Integer> pointsToMerge = clusterTree.get(c1).getPointsIndices();

                for (int i = 0; i < pointsToMerge.size(); i++) {
                    this.clusterTree.get(c2).addPointIndex(pointsToMerge.get(i));
                }

                // build new cluster tree with everything except c2
                for (int j = 0; j < this.clusterTree.size(); j++) {
                    if (j != c1) {
                        Cluster c = this.clusterTree.get(j);
                        newClusterTree.add(c);
                    }
                }
                this.clusterTree = newClusterTree;
            }
        }

        public double calcSingleLinkDistance(Cluster c1, Cluster c2) {
                double shortestLink = bigNumber;
                if (c1 == c2) {
                    shortestLink = 0.0;
                } else {
                    for (int i: c1.getPointsIndices()) {
                        for (int j: c2.getPointsIndices()) {
                            double distance = distanceMatrix.getDistance(i, j);

                           // System.out.println("Cluster1 idx:" + i + " Cluster2 idx:" + j + " distance: " + distance);
                            if (distance < shortestLink) {
                                shortestLink = distance;
                            }
                        }
                    }
                }
                return shortestLink;
        }

        public double calcAverageLinkDistance(Cluster c1, Cluster c2) {
            double averageLink = 0.0; // initalize to 0.0
            int count = 0;
            double sum = 0.0;

            if (c1 == c2) {
                 averageLink = 0.0;
            } else {
                for (int i: c1.getPointsIndices()) {
                    for (int j: c2.getPointsIndices()) {
                        double distance = distanceMatrix.getDistance(i, j);

                        if (i != j) // not sure i need this
                        {
                            sum += distance;
                            count++;
                        }

                    }
                }
                averageLink = sum/count;
            }
            return averageLink;
        }

        public double calcCompleteLinkDistance(Cluster c1, Cluster c2) {
            double completeLink = 0.0; // initalize to 0.0
            if (c1 == c2) {
                completeLink = 0.0;
            } else {
                for (int i: c1.getPointsIndices()) {
                    for (int j: c2.getPointsIndices()) {
                        double distance = distanceMatrix.getDistance(i, j);

                        // System.out.println("Cluster1 idx:" + i + " Cluster2 idx:" + j + " distance: " + distance);
                        if (distance > completeLink) {
                            completeLink = distance;
                        }
                    }
                }
            }
            return completeLink;
        }

        public void printClusterTree() {
            System.out.println(this.clusterTree.size() + " total clusters");
            for (int i = 0; i < this.clusterTree.size(); i++) {
                System.out.println("Cluster #" + i);
                System.out.println("-------------");
                ArrayList<Integer> pointIndices = this.clusterTree.get(i).getPointsIndices();
                for (int j = 0; j < pointIndices.size(); j++) {
                    System.out.println("point index: " + pointIndices.get(j) +
                            " x:" + this.xyPoints.get(pointIndices.get(j)).X +
                            " y:" + this.xyPoints.get(pointIndices.get(j)).Y);
                }
            }
        }

    }

    public void readInput() {
       // readDataFromFile(inputFile);
        readDataFromStdIn();
    }

    public void initialize() {
        masterClusterTree = new ClusterTree(numberOfClustersGoal, dataPoints);
        for (int i = 0; i < dataPoints.size(); i++) {
            Cluster c = new Cluster();
            c.addPointIndex(i);
            masterClusterTree.addCluster(c);
        }

        distanceMatrix = new DistanceMatrix(dataPoints);
        distanceMatrix.calculateDistanceMatrix();

        clusterOutput = new int[dataPoints.size()];

    }

    public void findSingleLinkSolution() {
        int indexClosest1 = 0;
        int indexClosest2 = 0;
        Double closestDistance = bigNumber;

        while (masterClusterTree.getClusterTree().size() > numberOfClustersGoal) {
            // 1. find closest two clusters
            // 2. merge clusters
            closestDistance = bigNumber;  // reset to largest again!!
            for (int c1 = 0; c1 < masterClusterTree.getClusterTree().size(); c1++) {
                for (int c2 = 0; c2 < masterClusterTree.getClusterTree().size(); c2++) {
                    if (c1 != c2) {
                        Cluster cluster1 = masterClusterTree.getClusterTree().get(c1);
                        Cluster cluster2 = masterClusterTree.getClusterTree().get(c2);
                        double distance = masterClusterTree.calcSingleLinkDistance(cluster1, cluster2);
                        if (distance < closestDistance) {
                            indexClosest1 = c1;
                            indexClosest2 = c2;
                            closestDistance = distance;
                        }
                    }
                }
            }
            //System.out.println("Shortest distance is: " + closestDistance + " indexClosest1:" + indexClosest1 + " indexClosest2:" + indexClosest2);
            masterClusterTree.mergeClusters(indexClosest1, indexClosest2);
        }
    }

    public void findCompleteLinkSolution() {
        int indexClosest1 = 0;
        int indexClosest2 = 0;
        Double closestDistance = bigNumber;

        while (masterClusterTree.getClusterTree().size() > numberOfClustersGoal) {
            // 1. find closest two clusters
            // 2. merge clusters
            closestDistance = bigNumber;  // reset to largest again!!
            for (int c1 = 0; c1 < masterClusterTree.getClusterTree().size(); c1++) {
                for (int c2 = 0; c2 < masterClusterTree.getClusterTree().size(); c2++) {
                    if (c1 != c2) {
                        Cluster cluster1 = masterClusterTree.getClusterTree().get(c1);
                        Cluster cluster2 = masterClusterTree.getClusterTree().get(c2);
                        double distance = masterClusterTree.calcCompleteLinkDistance(cluster1, cluster2);
                        if (distance < closestDistance) {
                            indexClosest1 = c1;
                            indexClosest2 = c2;
                            closestDistance = distance;
                        }
                    }
                }
            }
            //System.out.println("Shortest distance is: " + closestDistance + " indexClosest1:" + indexClosest1 + " indexClosest2:" + indexClosest2);
            masterClusterTree.mergeClusters(indexClosest1, indexClosest2);
        }
    }

    public void findAverageLinkSolution() {
        int indexClosest1 = 0;
        int indexClosest2 = 0;
        Double closestDistance = bigNumber;

        while (masterClusterTree.getClusterTree().size() > numberOfClustersGoal) {
            // 1. find closest two clusters
            // 2. merge clusters
            closestDistance = bigNumber;  // reset to largest again!!
            for (int c1 = 0; c1 < masterClusterTree.getClusterTree().size(); c1++) {
                for (int c2 = 0; c2 < masterClusterTree.getClusterTree().size(); c2++) {
                    if (c1 != c2) {
                        Cluster cluster1 = masterClusterTree.getClusterTree().get(c1);
                        Cluster cluster2 = masterClusterTree.getClusterTree().get(c2);
                        double distance = masterClusterTree.calcAverageLinkDistance(cluster1, cluster2);
                        if (distance < closestDistance) {
                            indexClosest1 = c1;
                            indexClosest2 = c2;
                            closestDistance = distance;
                        }
                    }
                }
            }
            //System.out.println("Shortest distance is: " + closestDistance + " indexClosest1:" + indexClosest1 + " indexClosest2:" + indexClosest2);
            masterClusterTree.mergeClusters(indexClosest1, indexClosest2);
        }
    }

    public void findSolution()
    {
      //  this.numberOfClustersGoal = 0;
      //  this.similariyMeasure = SINGLE_LINK;
        if (similariyMeasure == SINGLE_LINK) {
            findSingleLinkSolution();
        } else if (similariyMeasure == COMPLETE_LINK) {
            findCompleteLinkSolution();
        } else if (similariyMeasure == AVERAGE_LINK) {
            findAverageLinkSolution();
        }
    }

    public void convertOutput() {
        // clusterOutput
        for (int clusterIndex = 0; clusterIndex < masterClusterTree.getClusterTree().size(); clusterIndex++) {
            for (int dataIndex: masterClusterTree.getClusterTree().get(clusterIndex).getPointsIndices()) {
                clusterOutput[dataIndex] = clusterIndex;
            }
        }
    }

    public void writeOutput() {
     //   distanceMatrix.printDistanceMatrix();
        // masterClusterTree.printClusterTree();
        for (int i=0; i < clusterOutput.length; i++)
        {
            System.out.println(clusterOutput[i]);
        }
    }

    public static void main(String[] args) {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT. Your class should be named Solution. */
        Solution s = new Solution();

        s.readInput();

        s.initialize();

        s.findSolution();

        s.convertOutput();

        s.writeOutput();
    }
}
