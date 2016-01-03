package model;

import instance.Instance;
import model.tree.NormalTreeNode;

import java.util.ArrayList;
import java.util.List;

public class GbdtForest {

    private final int maxDepth;
    private final int treeNum;

    public GbdtForest(int maxDepth, int treeNum) {
        this.maxDepth = maxDepth;
        this.treeNum = treeNum;
    }

    private List<NormalTreeNode> trees;

    public void train(Iterable<Instance> dataset) {
        trees = new ArrayList<NormalTreeNode>(treeNum);
    }
}
