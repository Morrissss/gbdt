package loss;

import java.util.ArrayList;
import java.util.List;

public class ExponentialLoss extends AbstractLoss {

    public static ExponentialLoss getInstance() {
        return INSTANCE;
    }

    private static final ExponentialLoss INSTANCE = new ExponentialLoss();

    @Override
    public double instanceLoss(double estimate, int groundTruth) {
        return Math.exp(-estimate * (1 - 2*groundTruth));
    }

    @Override
    public List<Double> negativeGradient(List<Double> estimates, List<Integer> groundTruths) {
        List<Double> result = new ArrayList<>(estimates.size());
        for (int i = 0; i < estimates.size(); i++) {
            result.add(Math.exp((1-2*groundTruths.get(i)) * estimates.get(i)));
        }
        return result;
    }

    /**
     * @param groundTruths suppose only contains 0 and 1
     */
    @Override
    public double optimalEstimate(Iterable<Integer> groundTruths) {
        double posNum = 0;
        double negNum = 0;
        for (double y : groundTruths) {
            if (y == 1) {
                posNum++;
            } else {
                negNum++;
            }
        }
        return Math.log(posNum / negNum) / 2;
    }
}
