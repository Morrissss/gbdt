package loss;

import instance.Instance;

import java.util.List;

public interface Loss {
    /**
     * put one single init value for all samples
     */
    double initEstimate(List<Instance> samples);
    double instanceLoss(double estimate, int label);
    double batchLoss(List<Double> estimates, List<Integer> labels);
    double instanceNegGradient(double estimates, int label);
    double optimalEstimate(List<Instance> samples);
    double estimateToProb(double estimate);
}
