package loss;

import utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class LogLoss extends AbstractLoss {

    @Override
    public double instanceLoss(double estimate, int groundTruth) {
        double prediction = MathUtils.sigmoid(estimate);
        return - groundTruth * Math.log(prediction) - (1 - groundTruth) * Math.log(1 - prediction);
    }

    @Override
    public List<Double> negativeGradient(List<Double> estimates, List<Integer> groundTruths) {
        List<Double> result = new ArrayList<>(estimates.size());
        for (int i = 0; i < estimates.size(); i++) {
            double gt = groundTruths.get(i);
            double y = estimates.get(i);
            result.add((gt-y) / (y - y * y));
        }
        return result;
    }

    @Override
    public double optimalEstimate(Iterable<Integer> groundTruths) {
        double sum = 0;
        long num = 0;
        for (int groundTruth : groundTruths) {
            sum += groundTruth;
            num++;
        }
        return MathUtils.inverseSigmoid(1 - sum / num);
    }
}
