package morrissss.online.util;

import java.io.Serializable;

public class Sampler implements Serializable {

    public Sampler(double globalRatio, double negRatio) {
        this.globalRatio = globalRatio;
        this.negRatio = negRatio;
    }
    public final double globalRatio;
    public final double negRatio;

    public boolean keep(Object key, int label) {
        double rand = (key.hashCode() - (double) Integer.MIN_VALUE) / (Integer.MAX_VALUE - (double) Integer.MIN_VALUE);
        if (label == 0) {
            return rand < negRatio * globalRatio;
        } else if (label == 1) {
            return rand < globalRatio;
        } else {
            throw new RuntimeException("Illegal label " + label);
        }
    }

    public double distort(double ratio) {
        return ratio / (negRatio + (1-negRatio) * ratio);
    }

    public double calibrate(double ratio) {
        return negRatio * ratio / (1 - ratio + negRatio * ratio);
    }
}
