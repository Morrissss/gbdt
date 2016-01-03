package instance;

public class Instance {

    public FeatureIndex index;
    public double[] x;

    public double estimate;

    public double getFeature(int idx) {
        return x[idx];
    }

    public double getFeature(String featureKey) {
        return getFeature(index.idx(featureKey));
    }
}
