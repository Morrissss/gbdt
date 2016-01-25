package model.splitter;

import impurity.SplitCriterion;

public abstract class AbstractSplitter<T> implements Splitter {

    protected final SplitCriterion<T> criterion;

    protected AbstractSplitter(SplitCriterion<T> criterion) {
        this.criterion = criterion;
    }
}
