package impurity;

import java.util.List;

public abstract class AbstractClassificationImpurity implements SplitCriterion<Integer> {

    protected double posRatio(List<Integer> labels) {
        int num = 0;
        for (int y : labels) {
            if (y == 1) {
                num++;
            }
        }
        return num * 1.0 / labels.size();
    }

    protected double negRatio(List<Integer> labels) {
        int num = 0;
        for (int y : labels) {
            if (y == 0) {
                num++;
            }
        }
        return num * 1.0 / labels.size();
    }
}
