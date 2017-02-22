package morrissss.base.function;

import com.google.common.collect.Sets;
import morrissss.base.feature.FeatureKey;
import morrissss.base.feature.ModelKey;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class ValueFunction implements FeatureFunction, Serializable {

    /**
     * the function featureKey is bind to {@link morrissss.base.feature.FeatureKey#of(String)}
     */
    public static final String FUNC_NAME = "VAL";

    public ValueFunction(String[] columns, String[] params) {
        if (columns == null || columns.length != 1) {
            throw new IllegalArgumentException();
        }
        this.column = columns[0];
        this.featureKey = FeatureKey.of(this.column, FUNC_NAME);
        this.modelKey = ModelKey.of(this.featureKey, "");
    }

    @Override
    public void extract(Map<String, Object> input, Map<ModelKey, Double> output) {
        if (input.containsKey(column)) {
            Double value = ((Number) input.get(column)).doubleValue();
            output.put(modelKey, value);
        } else {
            output.put(modelKey, 0.0);
        }
    }

    private final FeatureKey featureKey;
    @Override
    public FeatureKey featureKey() {
        return featureKey;
    }

    private final ModelKey modelKey;
    @Override
    public Set<ModelKey> possibleKeys() {
        return Sets.newHashSet(modelKey);
    }

    public ValueFunction(String column) {
        this.column = column;
        this.featureKey = FeatureKey.of(column, FUNC_NAME);
        this.modelKey = ModelKey.of(this.featureKey, "");
    }
    private final String column;
}
