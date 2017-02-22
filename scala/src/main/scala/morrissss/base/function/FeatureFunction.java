package morrissss.base.function;

import morrissss.base.feature.FeatureKey;
import morrissss.base.feature.ModelKey;

import java.util.Map;
import java.util.Set;

public interface FeatureFunction {

    void extract(Map<String, Object> input, Map<ModelKey, Double> output);
    FeatureKey featureKey();
    Set<ModelKey> possibleKeys();
}
