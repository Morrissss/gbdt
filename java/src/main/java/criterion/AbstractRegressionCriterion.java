package criterion;

import java.util.List;

public abstract class AbstractRegressionCriterion implements SplitCriterion<Double> {

    protected double mean(List<Double> values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }
}
