package loss;

import java.util.ArrayList;
import java.util.List;

public class SquaredLoss extends AbstractLoss {

    public static SquaredLoss getInstance() {
        return INSTANCE;
    }

    private static final SquaredLoss INSTANCE = new SquaredLoss();

    @Override
    public double instanceLoss(double estimate, int groundTruth) {
        double diff = estimate - groundTruth;
        return .5 * diff * diff;
    }

    @Override
    public List<Double> negativeGradient(List<Double> estimates, List<Integer> groundTruths) {
        List<Double> result = new ArrayList<>(estimates.size());
        for (int i = 0; i < estimates.size(); i++) {
            result.add(groundTruths.get(i) - estimates.get(i));
        }
        return result;
    }

    @Override
    public double optimalEstimate(Iterable<Integer> groundTruths) {
        double result = 0;
        for (double val : groundTruths) {
            result += val;
        }
        return result;
    }
}
