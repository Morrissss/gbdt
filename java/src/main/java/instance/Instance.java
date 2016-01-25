package instance;

public class Instance {

    public final FeatureIndex index;
    public final double[] x;
    public final double y;

    public double estimate;

    public double getFeature(int idx) {
        return x[idx];
    }

    public double getFeature(String featureKey) {
        return getFeature(index.idx(featureKey));
    }
}
