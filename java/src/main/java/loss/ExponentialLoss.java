package loss;

import instance.Instance;
import utils.MathUtils;

import java.util.List;

public class ExponentialLoss extends AbstractLoss {

    public static Loss getInstance() {
        return INSTANCE;
    }

    private static final Loss INSTANCE = new ExponentialLoss();
    protected ExponentialLoss() {
        super();
    }

    @Override
    public double initEstimate(List<Instance> samples) {
        int posNum = 0;
        int negNum = 0;
        for (Instance sample : samples) {
            if (sample.label == 0) {
                negNum++;
            } else {
                posNum++;
            }
        }
        return 0.5 * Math.log(posNum * 1.0 / negNum);
    }

    @Override
    public double instanceLoss(double estimate, int label) {
        return Math.exp(estimate * (1 - 2 * label));
    }

    @Override
    public double instanceNegGradient(double estimate, int label) {
        int t = 1 - 2 * label;
        return t * Math.exp(t * estimate);
    }

    /**
     * @param samples suppose label only contains 0 and 1
     */
    @Override
    public double optimalEstimate(List<Instance> samples) {
        double numerator = 0;
        double denominator = 0;
        for (Instance sample : samples) {
            int t = 2 * sample.label - 1;
            numerator += t * Math.exp(-t * sample.estimate);
            denominator += Math.exp(-t * sample.estimate);
        }
        if (denominator == 0) {
            return 0;
        }
        return numerator / denominator;
    }

    @Override
    public double estimateToProb(double estimate) {
        return MathUtils.sigmoid(2 * estimate);
    }
}
