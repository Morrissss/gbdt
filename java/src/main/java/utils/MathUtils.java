package utils;

public class MathUtils {

    public static double sigmoid(double val) {
        return 1 / (1 + Math.exp(-val));
    }

    public static double inverseSigmoid(double val) {
        return Math.log(val / (1 - val));
    }
}
