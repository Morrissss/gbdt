package morrissss.base.function;

import com.google.common.collect.Sets;
import morrissss.base.feature.FeatureKey;
import morrissss.base.feature.ModelKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class CtrSmoothFunction implements FeatureFunction, Serializable {

    public static final String FUNC_NAME = "SCTR";

    public CtrSmoothFunction(String[] columns, String[] params) {
        if (columns == null || columns.length != 2) {
            throw new IllegalArgumentException();
        }
        if (params != null && params.length == 2 &&
            params[0].matches("-?\\d+\\.\\d{3}") && params[1].matches("-?\\d+\\.\\d{3}")) {
            this.numeratorColumn = columns[0];
            this.denominatorColumn = columns[1];
            this.numeratorSmooth = Double.parseDouble(params[0]);
            this.denominatorSmooth = Double.parseDouble(params[1]);
            this.featureKey = FeatureKey.of(new String[]{this.numeratorColumn, this.denominatorColumn}, FUNC_NAME,
                                            new String[]{String.format("%.3f", this.numeratorSmooth),
                                                  String.format("%.3f", this.denominatorSmooth)});
            this.modelKey = ModelKey.of(this.featureKey, "");
        } else {
            throw new IllegalArgumentException(Arrays.toString(columns) + " " + Arrays.toString(params));
        }
    }

    @Override
    public void extract(Map<String, Object> input, Map<ModelKey, Double> output) {
        double numerator = 0.0;
        Object numeratorObj = input.get(numeratorColumn);
        if (numeratorObj != null) {
            numerator = ((Number) numeratorObj).doubleValue();
        }
        double denominator = 0.0;
        if (input.containsKey(denominatorColumn)) {
            denominator = ((Number) input.get(denominatorColumn)).doubleValue();
        }
        output.put(modelKey, (numerator + numeratorSmooth) / (denominator + denominatorSmooth));
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

    public CtrSmoothFunction(String numeratorColumn, String denominatorColumn,
                             double numeratorSmooth, double denominatorSmooth) {
        this.numeratorColumn = numeratorColumn;
        this.denominatorColumn = denominatorColumn;
        this.numeratorSmooth = numeratorSmooth;
        this.denominatorSmooth = denominatorSmooth;
        this.featureKey = FeatureKey.of(new String[]{numeratorColumn, denominatorColumn}, FUNC_NAME,
                                        new String[]{String.format("%.3f", numeratorSmooth),
                                              String.format("%.3f", denominatorSmooth)});
        this.modelKey = ModelKey.of(this.featureKey, "");
    }
    private final String numeratorColumn;
    private final String denominatorColumn;
    private final double numeratorSmooth;
    private final double denominatorSmooth;
}
