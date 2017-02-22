package morrissss.base.feature;

import morrissss.base.util.KeyCompPair;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * col1&col2&col3$func&param1&param2$signature
 * Note that params and signature CAN be EMPTY strings
 */
public class ModelKey implements Comparable<ModelKey>, Serializable {

    private static final long serialVersionUID = 5942313208411592836L;

    public static ModelKey of(String column, String functionKey, String signature) {
        return new ModelKey(new String[]{column}, functionKey, signature);
    }
    public static ModelKey of(String[] columns, String functionKey, String signature) {
        return new ModelKey(columns, functionKey, signature);
    }
    public static ModelKey of(String column, String functionName, String[] params, String signature) {
        String functionKey = functionName + "|" + String.join("|", params);
        return new ModelKey(new String[]{column}, functionKey, signature);
    }
    public static ModelKey of(String[] columns, String functionName, String[] params, String signature) {
        String functionKey = functionName + "|" + String.join("|", params);
        return new ModelKey(columns, functionKey, signature);
    }
    public static ModelKey of(FeatureKey featureKey, String signature) {
        return new ModelKey(featureKey, signature);
    }

    public static ModelKey parse(String str) {
        String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(str, "$");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Illegal model key " + str);
        }
        String[] columns = parts[0].split("\\|");
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].intern();
        }
        return new ModelKey(columns, parts[1].intern(), parts[2].intern());
    }

    public static ModelKey lowerBound(String[] columns) {
        return new ModelKey(columns, " ", "");
    }
    public static ModelKey upperBound(String[] columns) {
        return new ModelKey(columns, "~", "");
    }

    /**
     * @return include
     */
    public static ModelKey minValue(String[] columns, String functionKey) {
        return new ModelKey(columns, functionKey, "");
    }
    /**
     * @return include
     */
    public static ModelKey maxValue(String[] columns, String functionKey) {
        return new ModelKey(columns, functionKey, "");
    }

    public static List<KeyCompPair<ModelKey, Double>> findRange(List<KeyCompPair<ModelKey, Double>> sortedFeatures,
                                                                ModelKey lowerInclude,
                                                                ModelKey upperInclude) {
        KeyCompPair<ModelKey, Double> lowerBound = KeyCompPair.of(lowerInclude, Double.NEGATIVE_INFINITY);
        KeyCompPair<ModelKey, Double> upperBound = KeyCompPair.of(upperInclude, Double.POSITIVE_INFINITY);

        int lowerIdx = Collections.binarySearch(sortedFeatures, lowerBound);
        if (lowerIdx < 0) {
            lowerIdx = -lowerIdx - 1;
        }
        int upperIdx = Collections.binarySearch(sortedFeatures, upperBound);
        if (upperIdx < 0) {
            upperIdx = -upperIdx - 1;
        }
        return sortedFeatures.subList(lowerIdx, upperIdx);
    }

    public final FeatureKey featureKey;
    public final String signature;
    private final String str;

    private ModelKey(String[] columns, String func, String signature) {
        this(FeatureKey.of(columns, func), signature);
    }
    private ModelKey(FeatureKey featureKey, String signature) {
        this.featureKey = featureKey;
        this.signature = signature;
        this.str = (featureKey + "$" + signature).intern();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelKey that = (ModelKey) o;
        return this.str.equals(that.str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public int compareTo(ModelKey o) {
        return this.str.compareTo(o.str);
    }

    @Override
    public String toString() {
        return str;
    }
}
