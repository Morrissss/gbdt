package splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;
import instance.Instance;
import model.GbdtParams;
import model.tree.GbdtNode;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomSplitter implements Splitter {

    public static Splitter getInstance() {
        return INSTANCE;
    }

    private static Splitter INSTANCE = new RandomSplitter();
    private RandomSplitter() {
        // empty
    }

    private static final int SAMPLE_PER_FEATURE = 100;

    @Override
    public boolean split(GbdtParams params, GbdtNode node) {
        FeatureIndex featureIndex = params.getFeatureIndex();
        List<Instance> samples = node.includedInstances;

        int bestSplitFeatureIdx = -1;
        double bestSplitThreshold = Double.NaN;
        double greatestImprovement = 0;

        SplitCriterion criterion = params.getCriterion();
        double lastLoss = criterion.reset(samples);
        Random random = new Random();
        for (int feature = 0; feature < featureIndex.size(); feature++) {
            for (int i = 0; i < SAMPLE_PER_FEATURE; i++) {
                int idx = random.nextInt(samples.size());
                double curThreshold = samples.get(idx).x[feature];
                criterion.splitWithThreshold(feature, curThreshold);
                if (criterion.leftSize() >= params.getLeafMinNum() &&
                    criterion.rightSize() >= params.getLeafMinNum()) {
                    double curLoss = criterion.impurity();
                    if (lastLoss - curLoss > greatestImprovement) {
                        greatestImprovement = lastLoss - curLoss;
                        bestSplitFeatureIdx = feature;
                        bestSplitThreshold = curThreshold;
                    }
                }
            }
        }

        if (bestSplitFeatureIdx < 0) {
            return false;
        }
        int firstRightIdx = 0;
        for (int i = firstRightIdx; i < samples.size(); i++) {
            if (samples.get(i).x[bestSplitFeatureIdx] <= bestSplitThreshold) {
                Collections.swap(samples, firstRightIdx++, i);
            }
        }
        node.featureIdx = bestSplitFeatureIdx;
        node.featureKey = featureIndex.name(bestSplitFeatureIdx);
        node.threshold = bestSplitThreshold;

        node.lessEqual = new GbdtNode(samples.subList(0, firstRightIdx));
        node.greater = new GbdtNode(samples.subList(firstRightIdx, samples.size()));

        return true;
    }
}
