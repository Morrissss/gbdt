package splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;
import instance.Instance;
import model.tree.GbdtNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortSplitter extends AbstractSplitter {

    public SortSplitter(GbdtNode node, SplitCriterion criterion, FeatureIndex featureIndex, int minNum) {
        super(node, criterion, featureIndex, minNum);
        featureComparators = new ArrayList<>(featureIndex.size());
        for (int i = 0; i < featureIndex.size(); i++) {
            final int idx = i;
            featureComparators.add(((o1, o2) -> Double.compare(o1.x[idx], o2.x[idx])));
        }
    }

    private List<Comparator<Instance>> featureComparators;

    @Override
    public boolean split() {
        List<Instance> instances = node.includedInstances;
        int bestSplitFeatureIdx = -1;
        int bestSplitInstanceIdx = -1;
        double greatestImprovement = 0;
        for (int i = 0; i < featureIndex.size(); i++) {
            // thread-safe
            Collections.sort(instances, featureComparators.get(i));
            double lastLoss = criterion.reset(instances);
            int firstRightIdx = 0;
            while (criterion.moveLeft(1)) {
                firstRightIdx++;
                if (firstRightIdx >= minNum || instances.size()-firstRightIdx >= minNum) {
                    double curLoss = criterion.loss();
                    if (lastLoss - curLoss > greatestImprovement) {
                        greatestImprovement = lastLoss - curLoss;
                        bestSplitFeatureIdx = i;
                        bestSplitInstanceIdx = firstRightIdx;
                    }
                }
            }
        }

        if (bestSplitFeatureIdx < 0) {
            return false;
        }

        Collections.sort(instances, featureComparators.get(bestSplitFeatureIdx));
        node.featureIdx = bestSplitFeatureIdx;
        node.featureKey = featureIndex.name(bestSplitFeatureIdx);
        node.threshold = instances.get(bestSplitInstanceIdx-1).x[featureIndex.idx(node.featureKey)];

        node.lessEqual = new GbdtNode(instances.subList(0, bestSplitInstanceIdx));
        node.greater = new GbdtNode(instances.subList(bestSplitInstanceIdx, instances.size()));

        return true;
    }

}
