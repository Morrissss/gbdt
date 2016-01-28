package loss;

import java.util.ArrayList;
import java.util.List;

public class SquaredLoss extends AbstractLoss {

    public static SquaredLoss getInstance() {
        return INSTANCE;
    }

    private static final SquaredLoss INSTANCE = new SquaredLoss();

    @Override
    public double instanceLoss(double estimate, int label) {
        double diff = estimate - label;
        return .5 * diff * diff;
    }

    @Override
    public List<Double> negativeGradient(List<Double> estimates, List<Integer> labels) {
        List<Double> result = new ArrayList<>(estimates.size());
        for (int i = 0; i < estimates.size(); i++) {
            result.add(labels.get(i) - estimates.get(i));
        }
        return result;
    }

    @Override
    public double optimalEstimate(Iterable<Integer> labels) {
        double result = 0;
        for (double val : labels) {
            result += val;
        }
        return result;
    }
}
