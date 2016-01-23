package loss;

import java.util.List;

public interface Loss {

    double instanceLoss(double estimate, int groundTruth);
    double batchLoss(List<Double> estimates, List<Integer> groundTruths);
    List<Double> negativeGradient(List<Double> estimates, List<Integer> groundTruths);
    double optimalEstimate(Iterable<Integer> groundTruths);
}
