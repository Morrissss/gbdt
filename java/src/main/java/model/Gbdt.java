package model;

import instance.Instance;
import loss.ExponentialLoss;
import loss.Loss;
import model.splitter.Splitter;
import model.tree.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class Gbdt implements Model {

    private final int maxDepth;
    private final int treeNum;
    private final Splitter splitter;
    private final Loss loss;

    private Gbdt(int maxDepth, int treeNum, Splitter splitter, Loss loss) {
        this.maxDepth = maxDepth;
        this.treeNum = treeNum;
        this.splitter = splitter;
        this.loss = loss;
    }

    private List<TreeNode> trees;

    public void train(List<Instance> dataset) {
        trees = new ArrayList<>(treeNum);
        for (int n = 0; n < treeNum; n++) {
            for (Instance instance : dataset) {
                instance.y = loss.instanceNegGradient(instance.estimate, instance.label);
            }
        }
    }

    public static class GbdtBuilder {

        public Gbdt build() {
            return new Gbdt(maxDepth, treeNum, splitter, loss);
        }

        private int maxDepth;
        private int treeNum;
        private Splitter splitter;
        private Loss loss;

        public GbdtBuilder() {
            maxDepth = 3;
            treeNum = 20;
            splitter = ;
            loss = ExponentialLoss.getInstance();
        }

        public GbdtBuilder setSize(int maxDepth, int treeNum) throws IllegalArgumentException {
            if (maxDepth > 10) {
                throw new IllegalArgumentException("maxDepth should less than 11");
            }
            this.maxDepth = maxDepth;
            this.treeNum = treeNum;
            return this;
        }
        public GbdtBuilder setSplitter(Splitter splitter) throws NullPointerException {
            if (splitter == null) {
                throw new NullPointerException();
            }
            this.splitter = splitter;
            return this;
        }
        public GbdtBuilder setLoss(Loss loss) throws NullPointerException {
            if (splitter == null) {
                throw new NullPointerException();
            }
            this.loss = loss;
            return this;
        }
    }
}
