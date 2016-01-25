package model.tree;

import instance.Instance;
import utils.Pair;

import java.util.List;

public class NormalTreeNode implements TreeNode {

    public NormalTreeNode(List<Instance> trainSet) {
        this.includedInstances = trainSet;
    }

    public String featureKey;
    public double threshold;
    public double estimate;
    public TreeNode lessEqual, greater;

    public List<Instance> includedInstances;

    @Override
    public boolean isLeaf() {
        return lessEqual == null && greater == null;
    }

    @Override
    public Pair<TreeNode, Integer> next(Instance instance, int currPath) {
        double feature = instance.getFeature(featureKey);
        TreeNode son = null;
        if (isLeaf()) {
            return Pair.of(son, currPath);
        } else if (feature > threshold) {
            return Pair.of(greater, PathUtils.toRight(currPath));
        } else {    // NaN in lessEqual
            return Pair.of(lessEqual, PathUtils.toLeft(currPath));
        }
    }
}
