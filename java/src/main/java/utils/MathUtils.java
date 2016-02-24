package utils;

import instance.Instance;
import model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MathUtils {

    public static double sigmoid(double val) {
        return 1 / (1 + Math.exp(-val));
    }

    public static double inverseSigmoid(double val) {
        if (val >= 1) {
            return Double.POSITIVE_INFINITY;
        } else if (val <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return Math.log(val / (1 - val));
    }

    public static double auc(List<Instance> samples, final Model model) {
        List<Instance> shallowCopy = new ArrayList<>(samples);
        Collections.sort(shallowCopy,
                         (s1, s2) -> Double.compare(model.predict(s1), model.predict(s2))
        );
        int truePairNum = 0;
        int falsePairNum = 0;
        int posNum = 0;
        int negNum = 0;
        for (Instance sample : shallowCopy) {
            if (sample.label == 0) {
                falsePairNum += posNum;
                negNum++;
            } else {
                truePairNum += negNum;
                posNum++;
            }
        }
        return truePairNum * 1.0 / (truePairNum + falsePairNum);
    }

}
