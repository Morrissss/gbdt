package criterion;

import instance.Instance;

import java.util.List;

public interface SplitCriterion {

    void reset(List<Instance> samples);

    /**
     * move first num samples from right to left
     * @param num
     * @return is anything moved
     */
    boolean moveLeft(int num);

    /**
     * @return total loss
     */
    double loss();

}
