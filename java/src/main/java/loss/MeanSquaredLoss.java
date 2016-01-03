package loss;

public class MeanSquaredLoss extends AbstractLoss {

    @Override
    public double instanceLoss(double estimate, double groundTruth) {
        double diff = estimate - groundTruth;
        return .5 * diff * diff;
    }

    @Override
    public double optimalEstimate(Iterable<Double> groundTruths) {
        double result = 0;
        int num = 0;
        for (double val : groundTruths) {
            result += val;
            num++;
        }
        return result / num;
    }
}
