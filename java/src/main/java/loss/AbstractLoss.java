package loss;

import java.util.List;

public abstract class AbstractLoss implements Loss {

    protected AbstractLoss() {
        // empty
    }

    @Override
    public double batchLoss(List<Double> estimates, List<Integer> labels) {
        assert estimates.size() == labels.size();
        double result = 0;
        for (int i = 0; i < estimates.size(); i++) {
            result += instanceLoss(estimates.get(i), labels.get(i));
        }
        return result;
    }

}
