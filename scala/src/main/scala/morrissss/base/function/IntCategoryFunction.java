package morrissss.base.function;

import morrissss.base.feature.FeatureKey;
import morrissss.base.feature.ModelKey;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntCategoryFunction implements FeatureFunction, Serializable {

    public static final String FUNC_NAME = "ICATE";

    public IntCategoryFunction(String[] columns, String[] params) {
        if (columns == null || columns.length != 1 || params == null || params.length != 1) {
            throw new IllegalArgumentException();
        }
        this.column = columns[0];
        this.categoryNum = Integer.parseInt(params[0]);
        this.featureKey = FeatureKey.of(this.column, FUNC_NAME, new String[]{"" + this.categoryNum});
    }

    @Override
    public void extract(Map<String, Object> input, Map<ModelKey, Double> output) {
        if (input.containsKey(column)) {
            int category = ((Number) input.get(column)).intValue();
            for (int c = 0; c < categoryNum; c++) {
                ModelKey key = ModelKey.of(this.featureKey, "" + c);
                if (c == category) {
                    output.put(key, 1.0);
                } else {
                    output.put(key, 0.0);
                }
            }
        }
    }

    private final FeatureKey featureKey;
    @Override
    public FeatureKey featureKey() {
        return featureKey;
    }

    @Override
    public Set<ModelKey> possibleKeys() {
        return IntStream.range(0, categoryNum).mapToObj(i -> ModelKey.of(this.featureKey, "" + i)).collect(Collectors.toSet());
    }

    public IntCategoryFunction(String column, int categoryNum) {
        this.column = column;
        this.categoryNum = categoryNum;
        this.featureKey = FeatureKey.of(column, FUNC_NAME, new String[]{"" + categoryNum});
    }
    private final String column;
    private final int categoryNum;
}
