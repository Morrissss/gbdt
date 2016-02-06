package instance;

import java.util.Arrays;

public class Instance {

    public final double[] x;
    public final int label; // 0, 1

    public Instance(double[] x, int label) {
        this.x = x;
        this.label = label;
    }

    public double target;
    public double estimate;

    @Override
    public String toString() {
        return "{" + label + ": " + Arrays.toString(x) + " with " + estimate + "=>" + target + "}";
    }
}
