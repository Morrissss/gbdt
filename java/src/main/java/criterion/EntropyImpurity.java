package criterion;

import java.util.List;

public class EntropyImpurity extends AbstractClassificationImpurity {

    @Override
    public double calc(List<Integer> labels) {
        double posRatio = posRatio(labels);
        double negRatio = negRatio(labels);
        return - posRatio*Math.log(posRatio) - negRatio*Math.log(negRatio);
    }
}
