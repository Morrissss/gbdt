package model.splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;

public abstract class AbstractSplitter implements Splitter {

    protected final FeatureIndex featureIndex;
    protected final SplitCriterion criterion;
    protected final int minNum;
    protected AbstractSplitter(FeatureIndex featureIndex, SplitCriterion criterion, int minNum) {
        this.featureIndex = featureIndex;
        this.criterion = criterion;
        this.minNum = minNum;
    }
}
