package loss;

import java.util.ArrayList;
import java.util.List;

public class ExponentialLoss extends AbstractLoss {

    public static ExponentialLoss getInstance() {
        return INSTANCE;
    }

    private static final ExponentialLoss INSTANCE = new ExponentialLoss();

    @Override
    public double instanceLoss(double estimate, int label) {
        return Math.exp(estimate * (1 - 2 * label));
    }

    @Override
    public List<Double> negativeGradient(List<Double> estimates, List<Integer> labels) {
        List<Double> result = new ArrayList<>(estimates.size());
        for (int i = 0; i < estimates.size(); i++) {
            int t = 1 - 2 * labels.get(i);
            result.add(t * Math.exp(t * estimates.get(i)));
        }
        return result;
    }

    /**
     * @param labels suppose only contains 0 and 1
     */
    @Override
    public double optimalEstimate(Iterable<Integer> labels) {
        int posNum = 0;
        int negNum = 0;
        for (int y : labels) {
            if (y == 1) {
                posNum++;
            } else {
                negNum++;
            }
        }
        return Math.log(posNum * 1.0 / negNum) / 2;
    }
}
