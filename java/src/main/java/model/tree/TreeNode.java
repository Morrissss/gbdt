package model.tree;

import instance.Instance;
import utils.Pair;

public interface TreeNode {

    boolean isLeaf();
    Pair<TreeNode, Integer> next(Instance instance, int currPath);
}
