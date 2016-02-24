package criterion;

import instance.Instance;

import java.util.List;

public abstract class AbstractAdditiveCriterion implements SplitCriterion {

    protected AbstractAdditiveCriterion() {
        // empty
    }

    protected List<Instance> samples;
    protected int rightBeg;

    protected int leftNum;
    protected int rightNum;
    protected double leftSum;
    protected double rightSum;
    protected double leftSquaredSum;
    protected double rightSquaredSum;
    protected double leftImpurity;
    protected double rightImpurity;

    @Override
    public double reset(List<Instance> samples) {
        this.samples = samples;
        this.rightBeg = 0;

        leftNum = 0;
        leftSum = 0;
        leftSquaredSum = 0;

        rightNum = 0;
        rightSum = 0;
        rightSquaredSum = 0;

        for (Instance sample : samples) {
            rightNum++;
            double t = sample.target;
            rightSum += t;
            rightSquaredSum += t * t;
        }
        updateImpurity();
        return impurity();
    }

    @Override
    public void splitWithThreshold(int feature, double threshold) {
        calcStatics(feature, threshold);
        updateImpurity();
    }

    @Override
    public int rightBegIdx() {
        return rightBeg;
    }

    @Override
    public boolean moveLeft(int num) {
        int batchEnd = Math.min(samples.size(), rightBeg+num);
        if (batchEnd == rightBeg) {
            return false;
        }
        for (int i = rightBeg; i < batchEnd; i++) {
            leftNum++;
            rightNum--;
            double t = samples.get(i).target;
            leftSum += t;
            rightSum -= t;
            leftSquaredSum += t * t;
            rightSquaredSum -= t * t;
        }
        updateImpurity();
        rightBeg = batchEnd;
        return true;
    }

    @Override
    public double impurity() {
        return (leftNum * leftImpurity + rightNum * rightImpurity) / (leftNum + rightNum);
    }

    @Override
    public int leftSize() {
        return leftNum;
    }

    @Override
    public int rightSize() {
        return rightNum;
    }

    protected abstract void updateImpurity();
    protected abstract void calcStatics(int feature, double threshold);
}
