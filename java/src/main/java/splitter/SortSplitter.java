package splitter;

import instance.Instance;
import model.GbdtParams;
import model.tree.GbdtNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortSplitter extends AbstractSplitter {

    public static Splitter getInstance() {
        return new SortSplitter();
    }

    private SortSplitter() {
        // empty
    }

    @Override
    public void init(GbdtParams params, GbdtNode node) {
        super.init(params, node);
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
            while (criterion.moveLeft(1)) {
                if (criterion.rightBegIdx() >= minNum && instances.size()-criterion.rightBegIdx() >= minNum) {
                    double curLoss = criterion.impurity();
                    if (lastLoss - curLoss > greatestImprovement) {
                        greatestImprovement = lastLoss - curLoss;
                        bestSplitFeatureIdx = i;
                        bestSplitInstanceIdx = criterion.rightBegIdx();
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
