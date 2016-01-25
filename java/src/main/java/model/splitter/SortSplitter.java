package model.splitter;

import impurity.SplitCriterion;
import instance.FeatureIndex;
import instance.Instance;
import model.tree.TreeNode;
import utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortSplitter<T> extends AbstractSplitter<T> {

    private final FeatureIndex featureIndex;
    private final List<Comparator<Instance>> featureComparators;

    public SortSplitter(FeatureIndex featureIndex, SplitCriterion<T> criterion) {
        super(criterion);
        this.featureIndex = featureIndex;
        featureComparators = new ArrayList<>(featureIndex.size());
        for (int i = 0; i < featureIndex.size(); i++) {
            final int idx = i;
            featureComparators.add(((o1, o2) -> Double.compare(o1.getFeature(idx), o2.getFeature(idx))));
        }
    }


    @Override
    public Pair<TreeNode, TreeNode> split(List<Instance> instances) {
    }

    /**
     * @return (index, loss)
     */
    private Pair<Integer, Double> findSplitIdx(List<Instance> instances, int featureIdx) {
        List<Instance> shallowCopy = new ArrayList<>(instances);
        Collections.sort(shallowCopy, featureComparators.get(featureIdx));

        for (int i = 0; i <= instances.size(); i++) {
            List<Instance> left = instances.subList(0, i);
            List<Instance> right = instances.subList(i, instances.size());
        }
    }
}
