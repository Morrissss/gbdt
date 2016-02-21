package splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;
import instance.Instance;
import model.GbdtParams;
import model.tree.GbdtNode;
import utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortSplitter implements Splitter {

    public static Splitter getInstance() {
        return INSTANCE;
    }

    private static Splitter INSTANCE = new SortSplitter();
    private SortSplitter() {
        // empty
    }

    @Override
    public boolean split(GbdtParams params, GbdtNode node) {
        FeatureIndex featureIndex = params.getFeatureIndex();
        List<Comparator<Instance>> featureComparators = initComparators(featureIndex);

        List<Instance> instances = node.includedInstances;
        Pair<Integer, Integer> bestSplitFeatureAndInstance = findSplit(params, instances, featureComparators);

        if (bestSplitFeatureAndInstance.first < 0) {
            return false;
        }

        int bestSplitFeatureIdx = bestSplitFeatureAndInstance.first;
        int bestSplitInstanceIdx = bestSplitFeatureAndInstance.second;
        Collections.sort(instances, featureComparators.get(bestSplitFeatureIdx));
        node.featureIdx = bestSplitFeatureIdx;
        node.featureKey = featureIndex.name(bestSplitFeatureIdx);
        node.threshold = instances.get(bestSplitInstanceIdx-1).x[featureIndex.idx(node.featureKey)];

        node.lessEqual = new GbdtNode(instances.subList(0, bestSplitInstanceIdx));
        node.greater = new GbdtNode(instances.subList(bestSplitInstanceIdx, instances.size()));

        return true;
    }

    private List<Comparator<Instance>> initComparators(FeatureIndex featureIndex) {
        List<Comparator<Instance>> featureComparators = new ArrayList<>(featureIndex.size());
        for (int i = 0; i < featureIndex.size(); i++) {
            final int idx = i;
            featureComparators.add(((o1, o2) -> Double.compare(o1.x[idx], o2.x[idx])));
        }
        return featureComparators;
    }

    private Pair<Integer, Integer> findSplit(GbdtParams params, List<Instance> instances,
                                             List<Comparator<Instance>> featureComparators) {
        SplitCriterion criterion = params.getCriterion();
        int leafMinNum = params.getLeafMinNum();
        int bestSplitFeatureIdx = -1;
        int bestSplitInstanceIdx = -1;
        double greatestImprovement = 0;
        for (int i = 0; i < params.getFeatureIndex().size(); i++) {
            // thread-safe
            Collections.sort(instances, featureComparators.get(i));
            double lastLoss = criterion.reset(instances);
            while (criterion.moveLeft(1)) {
                if (criterion.rightBegIdx() >= leafMinNum && instances.size()-criterion.rightBegIdx() >= leafMinNum) {
                    double curLoss = criterion.impurity();
                    if (lastLoss - curLoss > greatestImprovement) {
                        greatestImprovement = lastLoss - curLoss;
                        bestSplitFeatureIdx = i;
                        bestSplitInstanceIdx = criterion.rightBegIdx();
                    }
                }
            }
        }
        return Pair.of(bestSplitFeatureIdx, bestSplitInstanceIdx);
    }

}
