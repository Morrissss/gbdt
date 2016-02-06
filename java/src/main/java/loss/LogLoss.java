package loss;

import instance.Instance;
import utils.MathUtils;

import java.util.List;

public class LogLoss extends AbstractLoss {

    public static Loss getInstance() {
        return INSTANCE;
    }

    private static final Loss INSTANCE = new LogLoss();
    protected LogLoss() {
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
        return Math.log(posNum * 1.0 / negNum);
    }

    @Override
    public double instanceLoss(double estimate, int label) {
        double prediction = MathUtils.sigmoid(estimate);
        return - label * Math.log(prediction) - (1 - label) * Math.log(1 - prediction);
    }

    @Override
    public double instanceNegGradient(double estimate, int label) {
        return label - MathUtils.sigmoid(estimate);
    }

    /**
     * @param samples suppose label only contains 0 and 1
     */
    @Override
    public double optimalEstimate(List<Instance> samples) {
        double numerator = 0;
        double denominator = 0;
        for (Instance sample : samples) {
            numerator += sample.target;
            denominator += (sample.label - sample.target) * (1 - sample.label + sample.target);
        }
        if (denominator == 0) {
            return 0;
        }
        return numerator / denominator;
    }

    @Override
    public double estimateToProb(double estimate) {
        return MathUtils.sigmoid(estimate);
    }
}
