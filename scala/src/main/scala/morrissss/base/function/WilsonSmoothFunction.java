package morrissss.base.function;

import com.google.common.collect.Sets;
import morrissss.base.feature.FeatureKey;
import morrissss.base.feature.ModelKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class WilsonSmoothFunction implements FeatureFunction, Serializable {

    public static final String FUNC_NAME = "WCTR";

    private static final Set<String> POSSIBLE_CIS = Sets.newHashSet("0.999", "0.99", "0.95", "0.9", "0.8", "0.6");

    public WilsonSmoothFunction(String[] columns, String[] params) {
        if (columns == null || columns.length != 2) {
            throw new IllegalArgumentException();
        }
        if (params != null && params.length == 1 && POSSIBLE_CIS.contains(params[0])) {
            this.numeratorColumn = columns[0];
            this.denominatorColumn = columns[1];
            this.confidenceInterval = params[0];
            this.featureKey = FeatureKey.of(new String[]{this.numeratorColumn, this.denominatorColumn}, FUNC_NAME,
                                            new String[]{this.confidenceInterval});
            this.modelKey = ModelKey.of(this.featureKey, "");
        } else {
            throw new IllegalArgumentException(Arrays.toString(columns) + " " + Arrays.toString(params));
        }
    }

    @Override
    public void extract(Map<String, Object> input, Map<ModelKey, Double> output) {
        double numerator = 0.0;
        if (input.containsKey(numeratorColumn)) {
            numerator = ((Number) input.get(numeratorColumn)).doubleValue();
        }
        double denominator = 0.0;
        if (input.containsKey(denominatorColumn)) {
            denominator = ((Number) input.get(denominatorColumn)).doubleValue();
        }
        output.put(modelKey, evaluate(numerator, denominator));
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

    public WilsonSmoothFunction(String numeratorColumn, String denominatorColumn, String confidenceInterval) {
        if (!POSSIBLE_CIS.contains(confidenceInterval)) {
            throw new IllegalArgumentException("Illegal CI " + confidenceInterval);
        }
        this.numeratorColumn = numeratorColumn;
        this.denominatorColumn = denominatorColumn;
        this.confidenceInterval = confidenceInterval;
        this.featureKey = FeatureKey.of(new String[]{numeratorColumn, denominatorColumn}, FUNC_NAME,
                                        new String[]{confidenceInterval});
        this.modelKey = ModelKey.of(this.featureKey, "");
    }
    private final String numeratorColumn;
    private final String denominatorColumn;
    private final String confidenceInterval;

    private double evaluate(double m, double n) {
        if (m > n) {
            throw new IllegalArgumentException(m + ">" + n);
        }
        if (n == 0) {
            return 0.0;
        }
        double p = m * 1.0 / n;
        double z = 0;
        if (confidenceInterval.equals("0.999")) {
            z = 3.32;
        } else if (confidenceInterval.equals("0.99")) {
            z = 2.58;
        } else if (confidenceInterval.equals("0.95")) {
            z = 1.96;
        } else if (confidenceInterval.equals("0.9")) {
            z = 1.64;
        } else if (confidenceInterval.equals("0.8")) {
            z = 1.28;
        } else if (confidenceInterval.equals("0.6")) {
            z = 0.84;
        }
        double tmp = z * Math.sqrt(p*(1-p)/n + z*z / (4.0 * n * n));
        return (p + z * z / (2 * n) - tmp) / (1 + z * z / n);
    }
}
