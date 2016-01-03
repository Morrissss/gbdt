package model.tree;

import instance.Instance;
import utils.Pair;

public class NormalTreeNode implements TreeNode {

    public String featureKey;
    public double threshold;
    public double estimate;
    public TreeNode lessEqual, greater;

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
            return Pair.of(greater, Path.toRight(currPath));
        } else {    // NaN in lessEqual
            return Pair.of(lessEqual, Path.toLeft(currPath));
        }
    }
}
