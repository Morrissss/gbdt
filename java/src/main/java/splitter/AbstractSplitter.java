package splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;
import model.GbdtParams;
import model.tree.GbdtNode;

public abstract class AbstractSplitter implements Splitter {

    protected AbstractSplitter() {
        // empty
    }

    @Override
    public void init(GbdtParams params, GbdtNode node) {
        this.node = node;
        this.criterion = params.getCriterion();
        this.featureIndex = params.getFeatureIndex();
        this.minNum = params.getLeafMinNum();
    }

    protected GbdtNode node;
    protected SplitCriterion criterion;
    protected FeatureIndex featureIndex;
    protected int minNum;

}
