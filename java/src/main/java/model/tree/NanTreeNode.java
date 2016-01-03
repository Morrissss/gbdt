package model.tree;

import instance.Instance;
import utils.Pair;

public class NanTreeNode implements TreeNode {

    public String featureKey;
    public double estimate;
    public TreeNode nan, normal;

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
            return Pair.of(normal, Path.toNormal(currPath));
        } else {
            return Pair.of(nan, Path.toNan(currPath));
        }
    }
}
