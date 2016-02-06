package splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;
import model.tree.GbdtNode;

public abstract class AbstractSplitter implements Splitter {

    protected AbstractSplitter(GbdtNode node, SplitCriterion criterion, FeatureIndex featureIndex, int minNum) {
        this.node = node;
        this.criterion = criterion;
        this.featureIndex = featureIndex;
        this.minNum = minNum;
    }

    protected GbdtNode node;
    protected SplitCriterion criterion;
    protected FeatureIndex featureIndex;
    protected int minNum;

}
