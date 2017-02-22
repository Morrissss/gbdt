package morrissss.base.feature;

import morrissss.base.function.ValueFunction;

import java.io.Serializable;

/**
 * col1&col2&col3$func&param1&param2
 * Note that params CAN be EMPTY strings
 */
public class FeatureKey implements Comparable<FeatureKey>, Serializable {

    private static final long serialVersionUID = -394758649881038472L;

    public static FeatureKey parse(String str) {
        String[] parts = str.split("\\$"); // cannot be empty, so simply split is fine
        if (parts.length != 2) {
            throw new IllegalArgumentException("Illegal key " + str);
        }
        String[] columns = parts[0].split("\\|"); // cannot be empty, so simply split is fine
        String func = parts[1];
        return new FeatureKey(columns, func);
    }

    /**
     * with ValueFunction
     */
    public static FeatureKey of(String column) {
        String[] columns = new String[]{column};
        return new FeatureKey(columns, ValueFunction.FUNC_NAME);
    }

    public static FeatureKey of(String column, String functionName, String... params) {
        StringBuilder sb = new StringBuilder(params.length*10);
        for (String param : params) {
            sb.append("|").append(param);
        }
        return new FeatureKey(new String[]{column}, functionName + sb);
    }

    public static FeatureKey of(String[] columns, String functionName, String... params) {
        StringBuilder sb = new StringBuilder(params.length*10);
        for (String param : params) {
            sb.append("|").append(param);
        }
        return new FeatureKey(columns, functionName + sb);
    }

    public final String[] columns;
    public final String functionKey;
    private final String str;

    private FeatureKey(String[] columns, String functionKey) {
        this.columns = columns;
        this.functionKey = functionKey;
        this.str = (String.join("|", columns) + "$" + functionKey).intern();
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureKey that = (FeatureKey) o;
        return this.str.equals(that.str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public int compareTo(FeatureKey o) {
        return this.str.compareTo(o.str);
    }
}
