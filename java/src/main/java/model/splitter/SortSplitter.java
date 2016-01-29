package model.splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;
import instance.Instance;
import model.tree.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortSplitter extends AbstractSplitter {

    private final List<Comparator<Instance>> featureComparators;

    public SortSplitter(FeatureIndex featureIndex, SplitCriterion criterion, int minNum) {
        super(featureIndex, criterion, minNum);
        featureComparators = new ArrayList<>(featureIndex.size());
        for (int i = 0; i < featureIndex.size(); i++) {
            final int idx = i;
            featureComparators.add(((o1, o2) -> Double.compare(o1.getFeature(idx), o2.getFeature(idx))));
        }
    }


    @Override
    public boolean split(TreeNode node) {
        List<Instance> instances = node.includedInstances;
        int bestSplitFeatureIdx = -1;
        int bestSplitInstanceIdx = -1;
        double greatestImprovement = -1;
        double lastLoss = criterion.reset(instances);
        for (int i = 0; i < featureIndex.size(); i++) {
            Collections.sort(instances, featureComparators.get(i));
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
        node.featureKey = featureIndex.getFeatureNames()[bestSplitFeatureIdx];
        node.threshold = instances.get(bestSplitInstanceIdx).getFeature(node.featureKey);

        node.lessEqual = new TreeNode(instances.subList(0, bestSplitInstanceIdx));
        node.greater = new TreeNode(instances.subList(bestSplitInstanceIdx, instances.size()));

        return true;
    }

}
