package model;

import criterion.CriterionFactory;
import criterion.SplitCriterion;
import instance.FeatureIndex;
import loss.Loss;
import loss.LossFactory;
import splitter.Splitter;
import splitter.SplitterFactory;

public class GbdtParams {

    public FeatureIndex getFeatureIndex() {
        return featureIndex;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getLeafMinNum() {
        return leafMinNum;
    }

    public int getTreeNum() {
        return treeNum;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public Splitter getSplitter() {
        return SplitterFactory.fetchSplitter(splitter);
    }

    public SplitCriterion getCriterion() {
        return CriterionFactory.fetchCriterion(criterion);
    }

    public Loss getLoss() {
        return LossFactory.fetchLoss(loss);
    }

    public double getLearningRate() {
        return learningRate;
    }

    private final FeatureIndex featureIndex;
    private final int maxDepth;
    private final int leafMinNum;
    private final int treeNum;
    private final int threadNum;
    private final String splitter;
    private final String criterion;
    private final String loss;
    private final double learningRate;

    public GbdtParams(FeatureIndex featureIndex, int maxDepth, int leafMinNum, int treeNum, int threadNum,
                      String splitter, String criterion, String loss, double learningRate) {
        this.featureIndex = featureIndex;
        this.maxDepth = maxDepth;
        this.leafMinNum = leafMinNum;
        this.treeNum = treeNum;
        this.threadNum = threadNum;
        this.splitter = splitter;
        this.criterion = criterion;
        this.loss = loss;
        this.learningRate = learningRate;
    }

    public static class GbdtParamsBuilder {

        public GbdtParams build() {
            return new GbdtParams(featureIndex, maxDepth, leafMinNum, treeNum, threadNum,
                                  splitter, criterion, loss, learningRate);
        }

        private final FeatureIndex featureIndex;
        private int maxDepth;
        private int leafMinNum;
        private int treeNum;
        private int threadNum;
        private String splitter;
        private String criterion;
        private String loss;
        private double learningRate;

        public GbdtParamsBuilder(FeatureIndex featureIndex) {
            this.featureIndex = featureIndex;
            maxDepth = 3;
            leafMinNum = 5;
            treeNum = 20;
            threadNum = 4;
            splitter = "sort";
            criterion = "mse";
            loss = "log";
            learningRate = 1;
        }

        public GbdtParamsBuilder setDepth(int maxDepth) {
            if (maxDepth > 10) {
                throw new IllegalArgumentException("maxDepth should less than 11");
            }
            this.maxDepth = maxDepth;
            return this;
        }
        public GbdtParamsBuilder setLeafMinNum(int leafMinNum) {
            this.leafMinNum = leafMinNum;
            return this;
        }
        public GbdtParamsBuilder setTreeNum(int treeNum) {
            this.treeNum = treeNum;
            return this;
        }
        public GbdtParamsBuilder setThreadNum(int threadNum) {
            this.threadNum = threadNum;
            return this;
        }
        public GbdtParamsBuilder setCriterion(String name) {
            this.criterion = name;
            return this;
        }
        public GbdtParamsBuilder setSplitter(String name) {
            this.splitter = name;
            return this;
        }
        public GbdtParamsBuilder setLoss(String name) {
            this.loss = name;
            return this;
        }
        public GbdtParamsBuilder setLearningRate(double learningRate) {
            this.learningRate = learningRate;
            return this;
        }
    }
}
