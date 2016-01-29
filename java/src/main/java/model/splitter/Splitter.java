package model.splitter;

import model.tree.TreeNode;

public interface Splitter {

    /**
     * @param node will be modified
     * @return split or not
     */
    boolean split(TreeNode node);
}
