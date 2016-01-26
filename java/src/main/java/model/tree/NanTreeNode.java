package model.tree;

import instance.Instance;
import utils.Pair;

import java.util.List;

public class NanTreeNode implements TreeNode {

    public NanTreeNode(List<Instance> trainSet) {
        this.includedInstances = trainSet;
    }

    public String featureKey;
    public double estimate;
    public TreeNode nan, normal;

    public List<Instance> includedInstances;

    @Override
    public boolean isLeaf() {
        return nan == null && normal == null;
    }

    @Override
    public Pair<TreeNode, Integer> next(Instance instance, int currPath) {
        double feature = instance.getFeature(featureKey);
        TreeNode son = null;
        if (isLeaf()) {
            return Pair.of(son, currPath);
        } else if (!Double.isNaN(feature)) {
            return Pair.of(normal, PathUtils.toNormal(currPath));
        } else {
            return Pair.of(nan, PathUtils.toNan(currPath));
        }
    }
}
