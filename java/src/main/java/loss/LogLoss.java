package loss;

import utils.MathUtils;

public class LogLoss extends AbstractLoss {

    @Override
    public double instanceLoss(double estimate, double groundTruth) {
        double prediction = MathUtils.sigmoid(estimate);
        return - groundTruth * Math.log(prediction) - (1 - groundTruth) * Math.log(1 - prediction);
    }

    @Override
    public double optimalEstimate(Iterable<Double> groundTruths) {
        double sum = 0;
        long num = 0;
        for (double groundTruth : groundTruths) {
            sum += groundTruth;
            num++;
        }
        return MathUtils.inverseSigmoid(1 - sum / num);
    }
}
