package loss;

import java.util.List;

public interface Loss {

    double instanceLoss(double estimate, int label);
    double batchLoss(List<Double> estimates, List<Integer> labels);
    List<Double> negativeGradient(List<Double> estimates, List<Integer> labels);
    double optimalEstimate(Iterable<Integer> labels);
}
