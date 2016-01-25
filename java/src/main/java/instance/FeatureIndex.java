package instance;

import java.util.HashMap;
import java.util.Map;

public class FeatureIndex {

    private final String[] featureNames;
    private final Map<String, Integer> featureIdx;

    public FeatureIndex(String[] featureNames) {
        this.featureNames = featureNames;
        featureIdx = new HashMap<>();
        for (int i = 0; i < featureNames.length; i++) {
            featureIdx.put(featureNames[i], i);
        }
    }

    public int idx(String feature) {
        return featureIdx.get(feature);
    }

    public String[] getFeatureNames() {
        return featureNames;
    }

    public int size() {
        return featureNames.length;
    }
}
