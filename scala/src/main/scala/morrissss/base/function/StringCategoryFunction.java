package morrissss.base.function;

import com.google.common.collect.Sets;
import morrissss.base.feature.FeatureKey;
import morrissss.base.feature.ModelKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StringCategoryFunction implements FeatureFunction, Serializable {

    public static final String FUNC_NAME = "SCATE";

    public StringCategoryFunction(String[] columns, String[] params) {
        if (columns == null || columns.length != 1 || params == null) {
            throw new IllegalArgumentException();
        }
        String[] copy = Arrays.copyOf(params, params.length);
        Arrays.sort(copy);
        if (Arrays.equals(copy, params)) {
            throw new IllegalArgumentException("params not in order");
        }
        this.column = columns[0];
        this.categories = Sets.newHashSet(params);
        featureKey = FeatureKey.of(this.column, FUNC_NAME, params);
    }

    @Override
    public void extract(Map<String, Object> input, Map<ModelKey, Double> output) {
        if (input.containsKey(column)) {
            int category = (Integer) input.get(column);
            for (String c : categories) {
                ModelKey key = ModelKey.of(this.featureKey, c);
                if (c.equals(category)) {
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
        return categories.stream().map(c -> ModelKey.of(this.featureKey, c)).collect(Collectors.toSet());
    }

    public StringCategoryFunction(String column, Set<String> categories) {
        this.column = column;
        this.categories = categories;
        String[] categoriesArr = categories.toArray(new String[categories.size()]);
        Arrays.sort(categoriesArr);
        featureKey = FeatureKey.of(column, FUNC_NAME, categoriesArr);
    }
    private final String column;
    private final Set<String> categories;
}
