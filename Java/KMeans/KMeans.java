import java.io.*;
import java.util.*;
import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;

public class KMeans {
    private static String inputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//KMeans//data//places.txt";
    // private static String inputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//KMeans//data//test.txt";
    private static String outputFile = "//Users//johnmoran//Documents//GitHub//CS412//Programming//KMeans//data//clusters.txt";

    private ArrayList<DataPoint> dataSet = new ArrayList<>();
    private ArrayList<DataPoint> centroids = new ArrayList<>();

    private ArrayList<DataPoint> optimalDataSet = new ArrayList<>();
    private ArrayList<DataPoint> optimalCentroids = new ArrayList<>();
    private double optimalSSE;

    private static final int NUM_CLUSTERS = 3;
    private static final long RANDOM_SEED = 300;
    private static final int MAX_ITERATIONS = 10000;
    private Random rand = new Random();
    private static final double stoppingTolerance = 0.1;
    private static final double bigNumber = Math.pow(10, 10);

    private void initializeRandomNumbers() {
        rand.setSeed(RANDOM_SEED);
    }

    private void readDataFromFile(String pathname) {

        try {
            File file = new File(pathname);

            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] doublesAsStrings = line.split(",");
                double x = Double.parseDouble(doublesAsStrings[0]);
                double y = Double.parseDouble(doublesAsStrings[1]);
                DataPoint d = new DataPoint(x, y);

                dataSet.add(d);
                // System.out.println("x:" + x + " y:" + y);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void writeDataFile(String pathname) {
        try {
            File file = new File(pathname);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            ArrayList<DataPoint> op = getOptimalDataSet();
            for (int k = 0; k < op.size(); k++) {
                writer.write(k + " " + op.get(k).getCluster());
                writer.newLine();
            }

            writer.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void initializeCentroids(int nClusters) {
        // nextInt is exclusive of the top value,
        int max = dataSet.size();
        centroids.clear();

        // TO DO: need to make sure not using same centroid! random gen could pick next one as
        // same as current

        for (int i = 0; i < nClusters; i++) {
            int randomNum = rand.nextInt(max);

            DataPoint d = new DataPoint(dataSet.get(randomNum).getX(), dataSet.get(randomNum).getY());
            d.setCluster(i);
            centroids.add(d);
        }
    }

    private double calculateEuclideanDistance(DataPoint p, DataPoint q) {
        double distance = Math.sqrt(Math.pow((p.getX() - q.getX()), 2) + Math.pow((p.getY() - q.getY()), 2));

        return distance;
    }

    private void assignClusters() {


        for (int i = 0; i < dataSet.size(); i++) {
            double smallest = bigNumber;

            for (int j = 0; j < centroids.size(); j++) {
                double dist = calculateEuclideanDistance(dataSet.get(i), centroids.get(j));
                if (dist < smallest) {
                    smallest = dist;
                    dataSet.get(i).setCluster(centroids.get(j).getCluster());
                }
            }
        }
    }

    private void calculateCentroids() {
        for (int i = 0; i < centroids.size(); i++) {
            int sumX = 0;
            int sumY = 0;
            int nPointsInCluster = 0;

            for (int j = 0; j < dataSet.size(); j++) {
                if (dataSet.get(j).getCluster() == i) {
                    sumX += dataSet.get(j).getX();
                    sumY += dataSet.get(j).getY();
                    nPointsInCluster++;
                }
            }

            if (nPointsInCluster > 0) {
                centroids.get(i).setX(sumX / nPointsInCluster);
                centroids.get(i).setY(sumY / nPointsInCluster);
            }
        }
    }

    private double calculateSSE() {
        // calculate Sum of Square Errors
        double sum = 0.0;

        for (int i = 0; i < centroids.size(); i++) {
            for (int j = 0; j < dataSet.size(); j++) {
                sum += Math.pow(calculateEuclideanDistance(dataSet.get(j), centroids.get(i)), 2);
            }
        }

        return sum;
    }

    private boolean stoppingCriteriaMet(double prevSSE, double currentSSE) {
        double difference = Math.abs(prevSSE - currentSSE);
        System.out.println("Diff:" + difference);
        if (difference < stoppingTolerance) {
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<DataPoint> getOptimalDataSet() {
        return optimalDataSet;
    }

    private ArrayList<DataPoint> getOptimalCentroids() {
        return optimalCentroids;
    }

    private void assignOptimalDataSet() {
        optimalDataSet.clear();
        for (int i=0; i < dataSet.size(); i++)
        {
            DataPoint d = new DataPoint(dataSet.get(i).getX(), dataSet.get(i).getY());
            d.setCluster(dataSet.get(i).getCluster());
            optimalDataSet.add(d);
        }
    }

    private void assignOptimalCentroids() {
        optimalCentroids.clear();
        for (int i=0; i < centroids.size(); i++)
        {
            DataPoint d = new DataPoint(centroids.get(i).getX(), centroids.get(i).getY());
            d.setCluster(centroids.get(i).getCluster());
            optimalCentroids.add(d);
        }
    }

    private void assignOptimalSSE(double sse) {
        optimalSSE = sse;
    }

    private double getOptimalSSE() {
        return this.optimalSSE;
    }

    public static void main(String[] args) {
        double sse, prevSSE;

        double lowestSSE = bigNumber;

        KMeans kMeans = new KMeans();

        kMeans.initializeRandomNumbers();
        kMeans.readDataFromFile(inputFile);

        int i = 0;
        while (i < MAX_ITERATIONS) {

            System.out.println("Iteration: " + i);
            kMeans.initializeCentroids(NUM_CLUSTERS);
            kMeans.assignClusters();

            sse = kMeans.calculateSSE();
            //System.out.println("SSE: " + sse);

            do {
                prevSSE = sse;
                kMeans.calculateCentroids();
                kMeans.assignClusters();

                sse = kMeans.calculateSSE();
              //  System.out.println("SSE: " + sse);
            } while (!kMeans.stoppingCriteriaMet(prevSSE, sse));

            if (sse < lowestSSE ) {
                kMeans.assignOptimalCentroids();
                kMeans.assignOptimalDataSet();
                kMeans.assignOptimalSSE(sse);
            }

            i++;
        }

        System.out.println("Optimal SSE was: " + kMeans.getOptimalSSE());
        kMeans.writeDataFile(outputFile);

    }
}
