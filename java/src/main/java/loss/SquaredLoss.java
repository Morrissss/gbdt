package loss;

public class SquaredLoss extends AbstractLoss {

    public static SquaredLoss getInstance() {
        return INSTANCE;
    }

    private static final SquaredLoss INSTANCE = new SquaredLoss();

    @Override
    public double instanceLoss(double estimate, int label) {
        double diff = estimate - label;
        return .5 * diff * diff;
    }

    @Override
    public double instanceNegGradient(double estimate, int label) {
        return label - estimate;
    }

    @Override
    public double optimalEstimate(Iterable<Integer> labels) {
        double result = 0;
        for (double val : labels) {
            result += val;
        }
        return result;
    }
}
