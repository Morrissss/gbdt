package model;

import criterion.CriterionFactory;
import criterion.MseCriterion;
import criterion.SplitCriterion;
import instance.FeatureIndex;
import loss.LogLoss;
import loss.Loss;
import loss.LossFactory;

public class GbdtParams {

    public final FeatureIndex featureIndex;
    public final int maxDepth;
    public final int leafMinNum;
    public final int treeNum;
    public final int threadNum;
    public final String splitter;
    public final SplitCriterion criterion;
    public final Loss loss;
    public final double learningRate;

    public GbdtParams(FeatureIndex featureIndex, int maxDepth, int leafMinNum, int treeNum, int threadNum,
                      String splitter, SplitCriterion criterion, Loss loss, double learningRate) {
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
        private SplitCriterion criterion;
        private Loss loss;
        private double learningRate;

        public GbdtParamsBuilder(FeatureIndex featureIndex) {
            this.featureIndex = featureIndex;
            maxDepth = 3;
            leafMinNum = 5;
            treeNum = 20;
            threadNum = 4;
            splitter = "sort";
            criterion = MseCriterion.getInstance();
            loss = LogLoss.getInstance();
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
            this.criterion = CriterionFactory.getInstance().fetch(name);
            return this;
        }
        public GbdtParamsBuilder setSplitter(String name) {
            this.splitter = name;
            return this;
        }
        public GbdtParamsBuilder setLoss(String name) {
            this.loss = LossFactory.getInstance().fetch(name);
            return this;
        }
        public GbdtParamsBuilder setLearningRate(double learningRate) {
            this.learningRate = learningRate;
            return this;
        }
    }
}
