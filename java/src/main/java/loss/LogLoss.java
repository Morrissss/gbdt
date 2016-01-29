package loss;

import utils.MathUtils;

public class LogLoss extends AbstractLoss {

    public static LogLoss getInstance() {
        return INSTANCE;
    }

    private static final LogLoss INSTANCE = new LogLoss();

    @Override
    public double instanceLoss(double estimate, int label) {
        double prediction = MathUtils.sigmoid(estimate);
        return - label * Math.log(prediction) - (1 - label) * Math.log(1 - prediction);
    }

    @Override
    public double instanceNegGradient(double estimate, int label) {
        return label - MathUtils.sigmoid(estimate);
    }

    @Override
    public double optimalEstimate(Iterable<Integer> labels) {
        int sum = 0;
        int num = 0;
        for (int label : labels) {
            sum += label;
            num++;
        }
        return MathUtils.inverseSigmoid(sum * 1.0 / num);
    }
}
