package criterion;

import java.util.List;

public class GiniSplitCriterion implements SplitCriterion {

    @Override
    public double calc(List<Integer> labels) {
        int num0 = 0;
        int num1 = 0;
        for (int y : labels) {
            if (y == 0) {
                num0++;
            } else {
                num1++;
            }
        }
        double p0 = num0 * 1.0 / (num0 + num1);
        double p1 = num1 * 1.0 / (num0 + num1);
        return 1 - p0*p0 - p1*p1;
    }
}
