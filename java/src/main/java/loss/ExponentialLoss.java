package loss;

public class ExponentialLoss extends AbstractLoss {

    public static ExponentialLoss getInstance() {
        return INSTANCE;
    }

    private static final ExponentialLoss INSTANCE = new ExponentialLoss();

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
     * @param labels suppose only contains 0 and 1
     */
    @Override
    public double optimalEstimate(Iterable<Integer> labels) {
        int posNum = 0;
        int negNum = 0;
        for (int y : labels) {
            if (y == 1) {
                posNum++;
            } else {
                negNum++;
            }
        }
        return Math.log(posNum * 1.0 / negNum) / 2;
    }
}
