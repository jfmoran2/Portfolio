public class DataPoint {
    private double X = 0;
    private double Y = 0;
    private int Cluster = 0;

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

    public void setCluster(int clusterNumber) {
        this.Cluster = clusterNumber;
    }

    public int getCluster() {
        return this.Cluster;
    }
}
