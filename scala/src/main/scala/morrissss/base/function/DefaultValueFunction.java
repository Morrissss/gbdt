package morrissss.base.function;

import com.google.common.collect.Sets;
import morrissss.base.feature.FeatureKey;
import morrissss.base.feature.ModelKey;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class DefaultValueFunction implements FeatureFunction, Serializable {

    public static final String FUNC_NAME = "DEF";

    public DefaultValueFunction(String[] columns, String[] params) {
        if (columns == null || columns.length != 1) {
            throw new IllegalArgumentException();
        }
        if (params != null && params.length == 1 && params[0].matches("-?\\d+\\.\\d{6}")) {
            this.column = columns[0];
            this.defaultValue = Double.parseDouble(params[0]);
            this.featureKey = FeatureKey.of(this.column, FUNC_NAME, String.format("%.6f", this.defaultValue));
            this.modelKey = ModelKey.of(this.featureKey, "");
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void extract(Map<String, Object> input, Map<ModelKey, Double> output) {
        if (input.containsKey(column)) {
            Double value = ((Number) input.get(column)).doubleValue();
            output.put(modelKey, value);
        } else {
            output.put(modelKey, defaultValue);
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

    public DefaultValueFunction(String column, double defaultValue) {
        this.column = column;
        this.defaultValue = defaultValue;
        this.featureKey = FeatureKey.of(column, FUNC_NAME, String.format("%.6f", defaultValue));
        this.modelKey = ModelKey.of(this.featureKey, "");
    }
    private final String column;
    private final double defaultValue;
}
