package model.splitter;

import instance.Instance;
import model.tree.TreeNode;
import utils.Pair;

import java.util.List;

public interface Splitter {

    /**
     * @return (threshold, loss)
     */
    Pair<TreeNode, TreeNode> split(List<Instance> instances);
}
