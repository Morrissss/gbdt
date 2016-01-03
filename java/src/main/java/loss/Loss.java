package loss;

import java.util.List;

public interface Loss {

    double instanceLoss(double estimate, double groundTruth);
    double batchLoss(List<Double> estimates, List<Double> groundTruths);
    double optimalEstimate(Iterable<Double> groundTruths);
}
