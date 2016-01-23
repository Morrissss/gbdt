package impurity;

import java.util.List;

public class MseCriterion extends AbstractRegressionCriterion {

    @Override
    public double calc(List<Double> values) {
        double m = mean(values);
        double se = 0;
        for (double value : values) {
            se += (value-m) * (value-m);
        }
        return se / values.size();
    }
}
